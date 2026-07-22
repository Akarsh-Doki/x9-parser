package com.fcrm.fraud.x9parser.service;

import com.fcrm.fraud.x9parser.exception.X9ParseException;
import com.fcrm.fraud.x9parser.model.CheckImage;
import com.fcrm.fraud.x9parser.model.CheckRecord;
import com.fcrm.fraud.x9parser.model.ParseResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class X9ParserService {

    private static final Logger log = LoggerFactory.getLogger(X9ParserService.class);

    /**
     * Parse the given X9 file bytes into checks and images.
     */
    public ParseResult parse(byte[] fileBytes) {
        if (fileBytes == null || fileBytes.length == 0) {
            throw new X9ParseException("The uploaded file is empty.");
        }

        // read every record: keep its raw bytes (for images) and its text (for fields)
        ArrayList<byte[]> recordBytesList = new ArrayList<>();
        ArrayList<String> recordTextList = new ArrayList<>();
        readRecords(fileBytes, recordBytesList, recordTextList);

        if (recordTextList.isEmpty()) {
            throw new X9ParseException("This does not look like an X9 file - no records could be read.");
        }

        // every X9 file starts with a File Header record (type 01)
        if (!recordTextList.get(0).startsWith("01")) {
            throw new X9ParseException("This does not look like an X9 file - it does not start with a File Header record.");
        }
        List<CheckRecord> checks = extractChecks(recordTextList);
        List<CheckImage> images = extractImages(recordBytesList, recordTextList);

        log.info("Parsed X9 file: {} records, {} checks, {} images",
                 recordTextList.size(), checks.size(), images.size());
        return new ParseResult(checks, images);
    }

    // format: 4-byte length, then that many bytes = one record (repeat until the bytes run out)
    private void readRecords(byte[] fileBytes, ArrayList<byte[]> bytesOut, ArrayList<String> textOut) {
        try {
            DataInputStream in = new DataInputStream(new ByteArrayInputStream(fileBytes));
            while (true) {
                byte[] lengthBytes = new byte[4];
                int got = in.read(lengthBytes);
                if (got < 4) {
                    break;
                }
                int recordLength = bytesToInt(lengthBytes);
                if (recordLength <= 0) {
                    break;
                }
                byte[] recordBytes = new byte[recordLength];
                in.readFully(recordBytes);

                bytesOut.add(recordBytes);
                textOut.add(new String(recordBytes, "Cp1047")); // Cp1047 = EBCDIC (for reading text fields)
            }
            in.close();
        } 
        catch (Exception e) {
            throw new X9ParseException("Could not read the X9 file. It may be corrupt or not an X9 file.", e);
        }
    }
    // pull each check (type 25 record) out into a CheckRecord, one field at a time
    private List<CheckRecord> extractChecks(ArrayList<String> records) {
        String[][] checkFields = {
            {"Record Type", "1", "2"},
            {"Auxiliary On Us", "3", "17"},
            {"External Processing Code", "18", "18"},
            {"Payor Bank Routing Number", "19", "26"},
            {"Payor Bank Routing Number Check Digit", "27", "27"},
            {"On Us", "28", "47"},
            {"Item Amount", "48", "57"},
            {"ECE Institution Item Sequence Number", "58", "72"},
            {"Documentation Type Indicator", "73", "73"},
            {"Return Acceptance Indicator", "74", "74"},
            {"MICR Valid Indicator", "75", "75"},
            {"BFD Indicator", "76", "76"},
            {"Check Detail Record Addendum Count", "77", "78"},
            {"Correction Indicator", "79", "79"},
            {"Archive Type Indicator", "80", "80"}
        };

        List<CheckRecord> checks = new ArrayList<>();
        for (String record : records) {
            if (record.startsWith("25")) {
                // keep the fields in order, so the CSV columns come out in the same order
                Map<String, String> fields = new LinkedHashMap<>();
                for (String[] field : checkFields) {
                    int start = Integer.parseInt(field[1]);
                    int end = Integer.parseInt(field[2]);
                    fields.put(field[0], slice(record, start, end));
                }
                checks.add(new CheckRecord(fields));
            }
        }
        return checks;
    }

    // images: pull the raw TIFF out of each type-52 record.
    private List<CheckImage> extractImages(ArrayList<byte[]> recordBytesList, ArrayList<String> recordTextList) {
        List<CheckImage> images = new ArrayList<>();

        int checkNumber = 0;  // which check we're currently inside
        int imageInCheck = 0; // how many images seen for this check (0 = front, 1 = rear)
        for (int i = 0; i < recordTextList.size(); i++) {
            String text = recordTextList.get(i);

            if (text.startsWith("25")) {
                checkNumber++;
                imageInCheck = 0;
                continue;
            }

            if (!text.startsWith("52")) {
                continue;
            }

            // an image record before any check has appeared - skip it defensively
            if (checkNumber == 0) {
                continue;
            }

            byte[] recordBytes = recordBytesList.get(i);
            CheckImage.Side side = (imageInCheck == 0) ? CheckImage.Side.FRONT : CheckImage.Side.REAR;
            imageInCheck++;

            try {
                // X = length of image reference key (positions 102-105)
                int x = Integer.parseInt(slice(text, 102, 105));
                // Y = length of digital signature (5 digits, right after the key)
                int yStart = 106 + x;
                int y = Integer.parseInt(slice(text, yStart, yStart + 4).trim());
                // Z = length of image data (7 digits, after the signature)
                int zStart = 111 + x + y;
                int z = Integer.parseInt(slice(text, zStart, zStart + 6).trim());

                // the image data itself starts at 118 + X + Y and runs Z bytes (RAW bytes)
                int imageStart = 118 + x + y; // 1-based position
                int from = imageStart - 1; // 0-based index into the byte array
                int length = z;

                if (from < 0 || length <= 0 || from + length > recordBytes.length) {
                    log.warn("Skipped an image (unexpected size) for check {}{}", checkNumber, side.getCode());
                    continue;
                }

                byte[] imageBytes = new byte[length];
                System.arraycopy(recordBytes, from, imageBytes, 0, length);

                images.add(new CheckImage(checkNumber, side, imageBytes));
            } catch (NumberFormatException e) {
                // one bad image record shouldn't sink the whole file - log it and move on
                log.warn("Skipped an image (malformed lengths) for check {}{}", checkNumber, side.getCode());
            }
        }
        return images;
    }

    private String slice(String record, int start, int end) {
        if (start > record.length()) {
            return "";
        }
        if (end > record.length()) {
            end = record.length();
        }
        return record.substring(start - 1, end).trim();
    }

    // turn 4 bytes into a number, most significant byte first (big-endian)
    private int bytesToInt(byte[] b) {
        int value = 0;
        for (int i = 0; i < 4; i++) {
            int shift = (4 - 1 - i) * 8;
            value += (b[i] & 0xFF) << shift;
        }
        return value;
    }
}
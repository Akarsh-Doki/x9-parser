package com.fcrm.fraud.x9parser.service;

import com.fcrm.fraud.x9parser.config.X9Config;
import com.fcrm.fraud.x9parser.exception.X9ParseException;
import com.fcrm.fraud.x9parser.model.CheckRecord;
import com.fcrm.fraud.x9parser.model.ProcessSummary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// Reads an X9 file one record at a time and writes the two CSVs and the images as it goes
@Service
public class X9StreamProcessor {
    private static final Logger log = LoggerFactory.getLogger(X9StreamProcessor.class);
    private static final Charset EBCDIC = Charset.forName("Cp1047");
    private final X9Config config;

    public X9StreamProcessor(X9Config config) {
        this.config = config;
    }

    public ProcessSummary process(String inputPath) {
        File file = findFile(inputPath);
        if (!file.isFile()) {
            throw new X9ParseException("File not found: " + inputPath);
        }

        String baseName = stripExtension(file.getName());
        Path outputDir = Path.of(config.getOutputDir());
        Path shortCsv = outputDir.resolve(baseName + "_short.csv");
        Path bigCsv = outputDir.resolve(baseName + "_big.csv");
        Path imagesDir = outputDir.resolve(baseName + "_images");
        List<String> bigColumns = loadBigColumns();
        try {
            Files.createDirectories(outputDir);
        } 
        catch (IOException e) {
            throw new X9ParseException("Could not create the output folder: " + outputDir, e);
        }
        long startTime = System.currentTimeMillis();
        
        int checkCount = 0;
        int imageCount = 0;

        try (DataInputStream in = new DataInputStream(new BufferedInputStream(new FileInputStream(file)));
            CheckCsvWriter csvWriter = new CheckCsvWriter(shortCsv, bigCsv, bigColumns, config, file.getPath())) {
            ImageWriter imageWriter = new ImageWriter(imagesDir);
            boolean isFirstRecord = true;
            int currentCheck = 0;
            int imageInCheck = 0;

            byte[] recordBytes = readRecord(in);
            while (recordBytes != null) {
                String text = new String(recordBytes, EBCDIC);
                String recordType = text.substring(0, 2);

                if (isFirstRecord) {
                    if (!recordType.equals("01")) {
                        throw new X9ParseException("This does not look like an X9 file. It should start with a File Header record.");
                    }
                    isFirstRecord = false;
                }

                if (recordType.equals("25")) {
                    currentCheck++;
                    imageInCheck = 0;
                    CheckRecord check = readCheck(text);
                    csvWriter.write(check);
                    checkCount++;
                } 
                else if (recordType.equals("52")) {
                    boolean isFront = (imageInCheck == 0);
                    imageInCheck++;
                    byte[] image = extractImage(recordBytes, text);
                    if (image!=null) {
                        String name = "check" + currentCheck + "_" + (isFront ? "F" : "R") + ".tif";
                        imageWriter.write(name, image);
                        imageCount++;
                    }
                }
                recordBytes = readRecord(in);
            }
        } 
        catch (X9ParseException e) {
            throw e;
        } 
        catch (Exception e) {
            throw new X9ParseException("Could not process the X9 file: " + e.getMessage(), e);
        }

        long elapsed = System.currentTimeMillis() - startTime;
        log.info("Processed {}: {} checks, {} images in {} ms", file.getName(), checkCount, imageCount, elapsed);

        ProcessSummary summary = new ProcessSummary(checkCount, checkCount, imageCount, elapsed);
        summary.setShortCsvPath(shortCsv.toString());
        summary.setBigCsvPath(bigCsv.toString());
        summary.setImagesDir(imagesDir.toString());
        return summary;
    }

    // Reads the next record
    private byte[] readRecord(DataInputStream in) throws IOException {
        byte[] lengthBytes = new byte[4];
        try {
            in.readFully(lengthBytes);
        } 
        catch (EOFException endOfFile) {
            return null;
        }

        int length = bytesToInt(lengthBytes);
        if (length <= 0) {
            return null;
        }

        byte[] recordBytes = new byte[length];
        in.readFully(recordBytes);
        return recordBytes;
    }

    // Pulls the check's fields out by their positions from the config file.
    private CheckRecord readCheck(String record) {
        Map<String, String> fields = new LinkedHashMap<>();
        for (X9Config.FieldDef field:config.getCheckFields()) {
            fields.put(field.getName(), slice(record, field.getStart(), field.getEnd()));
        }
        return new CheckRecord(fields);
    }

    // Pulls the raw TIFF bytes out of an image record. Returns null if it's malformed.
    private byte[] extractImage(byte[] recordBytes, String text) {
        try {
            int keyLength = Integer.parseInt(slice(text, 102, 105));
            int signatureLengthStart = 106 + keyLength;
            int signatureLength = Integer.parseInt(slice(text, signatureLengthStart, signatureLengthStart + 4).trim());

            int imageLengthStart = 111 + keyLength + signatureLength;
            int imageLength = Integer.parseInt(slice(text, imageLengthStart, imageLengthStart + 6).trim());

            // the image bytes start right after the key and signature
            int imageStart = (118 + keyLength + signatureLength)-1;

            // if these point past the end of the record, it's corrupt, so skip it
            if (imageStart < 0 || imageLength <= 0 || imageStart + imageLength > recordBytes.length) {
                log.warn("Skipping an image with an unexpected size");
                return null;
            }
            byte[] image = new byte[imageLength];
            System.arraycopy(recordBytes, imageStart, image, 0, imageLength);
            return image;
        }
        catch (NumberFormatException e) {
            log.warn("Skipping a malformed image record");
            return null;
        }
    }

    // If the user typed a full path, use it. If they typed just a file name, look in the input folder.
    private File findFile(String inputPath) {
        File file = new File(inputPath);
        if (file.isAbsolute() || file.exists()) {
            return file;
        }
        String inputDir = config.getInputDir();
        if (inputDir != null && !inputDir.isBlank()) {
            return new File(inputDir, inputPath);
        }
        return file;
    }

    private String stripExtension(String name) {
        int dot = name.lastIndexOf('.');
        return (dot > 0) ? name.substring(0, dot) : name;
    }

    // Loads the big CSV column names from the resource file named in the config.
    private List<String> loadBigColumns() {
        String resource = config.getBigFormatColumnsResource();
        try (InputStream in = getClass().getClassLoader().getResourceAsStream(resource)) {
            if (in == null) {
                throw new X9ParseException("Missing big-format columns file: " + resource);
            }

            String header = new String(in.readAllBytes(), StandardCharsets.UTF_8).trim();
            return Arrays.asList(header.split(","));
        } 
        catch (IOException e) {
            throw new X9ParseException("Could not read big-format columns file: " + resource, e);
        }
    }

    // 1-based, inclusive substring, trimmed, with bounds safety.
    private String slice(String record, int start, int end) {
        if (start > record.length()) {
            return "";
        }
        if (end > record.length()) {
            end = record.length();
        }
        return record.substring(start - 1, end).trim();
    }

    // The 4 length bytes are big-endian
    private int bytesToInt(byte[] b) {
        return ((b[0] & 0xFF) << 24) | ((b[1] & 0xFF) << 16) | ((b[2] & 0xFF) << 8) | (b[3] & 0xFF);
    }
}
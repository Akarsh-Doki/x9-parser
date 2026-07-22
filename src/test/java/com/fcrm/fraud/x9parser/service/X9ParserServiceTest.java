package com.fcrm.fraud.x9parser.service;

import com.fcrm.fraud.x9parser.exception.X9ParseException;
import com.fcrm.fraud.x9parser.model.CheckImage;
import com.fcrm.fraud.x9parser.model.ParseResult;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests the parser on its own, with no web layer
 */
class X9ParserServiceTest {

    private final X9ParserService parser = new X9ParserService();

    @Test
    void parsesAllTenChecksFromTheSampleFile() throws IOException {
        ParseResult result = parser.parse(sampleFileBytes());

        assertEquals(10, result.getCheckCount());
        // spot-check real values from the first check
        assertEquals("05777093", result.getChecks().get(0).get("Payor Bank Routing Number"));
        assertEquals("0000010000", result.getChecks().get(0).get("Item Amount"));
    }

    @Test
    void extractsAFrontAndRearImageForEveryCheck() throws IOException {
        ParseResult result = parser.parse(sampleFileBytes());

        assertEquals(20, result.getImageCount());

        CheckImage first = result.getImages().get(0);
        assertEquals(1, first.getCheckNumber());
        assertEquals(CheckImage.Side.FRONT, first.getSide());
        assertEquals("check1_F.tif", first.getFileName());

        CheckImage last = result.getImages().get(19);
        assertEquals("check10_R.tif", last.getFileName());

        // every image should hold actual data
        for (CheckImage image : result.getImages()) {
            assertTrue(image.getData().length > 0);
        }
    }

    @Test
    void rejectsAnEmptyFile() {
        X9ParseException e = assertThrows(X9ParseException.class, () -> parser.parse(new byte[0]));
        assertTrue(e.getMessage().contains("empty"));
    }

    @Test
    void rejectsAFileThatIsNotX9() {
        byte[] notX9 = "just a plain text file".getBytes();
        assertThrows(X9ParseException.class, () -> parser.parse(notX9));
    }

    @Test
    void rejectsAFileThatDoesNotStartWithAFileHeader() {
        // a correctly framed record (4-byte length, then the bytes) whose type is
        byte[] wrongHeader = {0, 0, 0, 2, (byte) 0xF2, (byte) 0xF5};

        X9ParseException e = assertThrows(X9ParseException.class, () -> parser.parse(wrongHeader));
        assertTrue(e.getMessage().contains("File Header"));
    }

    // the sample X9 file, loaded from src/test/resources
    private byte[] sampleFileBytes() throws IOException {
        try (InputStream in = getClass().getResourceAsStream("/sample.x9")) {
            if (in == null) {
                throw new IllegalStateException("sample.x9 is missing from src/test/resources");
            }
            return in.readAllBytes();
        }
    }
}
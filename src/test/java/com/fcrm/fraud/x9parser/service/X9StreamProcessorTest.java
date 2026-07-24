package com.fcrm.fraud.x9parser.service;
import com.fcrm.fraud.x9parser.config.X9Config;
import com.fcrm.fraud.x9parser.exception.X9ParseException;
import com.fcrm.fraud.x9parser.model.ProcessSummary;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

// Runs the processor on the sample file and checks the counts and the output files.
@SpringBootTest
class X9StreamProcessorTest {
    @Autowired
    private X9StreamProcessor processor;
    @Autowired
    private X9Config config;

    @Test
    void parsesTheSampleFileAndWritesOutput(@TempDir Path outputDir) throws Exception {
        config.setOutputDir(outputDir.toString());
        String samplePath = Path.of(getClass().getResource("/sample.x9").toURI()).toString();

        ProcessSummary summary = processor.process(samplePath);

        assertEquals(10, summary.getChecksParsed());
        assertEquals(20, summary.getImagesWritten());

        // both CSVs exist
        assertTrue(Files.exists(Path.of(summary.getShortCsvPath())));
        assertTrue(Files.exists(Path.of(summary.getBigCsvPath())));

        List<String> shortLines = Files.readAllLines(Path.of(summary.getShortCsvPath()));
        assertEquals(11, shortLines.size());

        List<String> bigLines = Files.readAllLines(Path.of(summary.getBigCsvPath()));
        assertEquals(229, bigLines.get(0).split(",").length);

        // 20 image files written
        try (Stream<Path> files = Files.list(Path.of(summary.getImagesDir()))) {
            long tifCount = files.filter(p -> p.toString().endsWith(".tif")).count();
            assertEquals(20, tifCount);
        }
    }

    @Test
    void rejectsAMissingFile() {
        assertThrows(X9ParseException.class, () -> processor.process("/no/such/file.x9"));
    }
}
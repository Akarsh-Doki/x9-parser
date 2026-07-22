package com.fcrm.fraud.x9parser.service;

import com.fcrm.fraud.x9parser.model.CheckRecord;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests the CSV formatting on its own, with small hand-made checks.
 */
class X9CsvWriterTest {

    private final X9CsvWriter writer = new X9CsvWriter();

    @Test
    void writesAHeaderRowPlusOneRowPerCheck() {
        List<CheckRecord> checks = List.of(
            check("Record Type", "25", "Item Amount", "0000010000"),
            check("Record Type", "25", "Item Amount", "0000010002")
        );

        String csv = writer.toCsv(checks);
        String[] lines = csv.trim().split("\n");

        assertEquals(3, lines.length); // header + 2 checks
        assertEquals("\"Record Type\",\"Item Amount\"", lines[0]);
        assertEquals("\"25\",\"0000010000\"", lines[1]);
    }

    @Test
    void returnsAnEmptyStringWhenThereAreNoChecks() {
        assertEquals("", writer.toCsv(List.of()));
    }

    @Test
    void quotesValuesSoCommasAndQuotesCannotBreakTheCsv() {
        List<CheckRecord> checks = List.of(check("On Us", "209 153 53/111, extra", "Note", "say \"hi\""));

        String csv = writer.toCsv(checks);

        // the comma stays inside one quoted value, and quotes are doubled
        assertTrue(csv.contains("\"209 153 53/111, extra\""));
        assertTrue(csv.contains("\"say \"\"hi\"\"\""));
    }

    // small helper to build a check from name/value pairs
    private CheckRecord check(String... nameValuePairs) {
        Map<String, String> fields = new LinkedHashMap<>();
        for (int i = 0; i < nameValuePairs.length; i += 2) {
            fields.put(nameValuePairs[i], nameValuePairs[i + 1]);
        }
        return new CheckRecord(fields);
    }
}
package com.fcrm.fraud.x9parser.service;

import com.fcrm.fraud.x9parser.model.CheckRecord;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Turns the parsed checks into CSV text: a header row, then one row per check.
 * Kept separate from the parser so the parser reads the file and this just formats it.
 */
@Component
public class X9CsvWriter {

    public String toCsv(List<CheckRecord> checks) {
        if (checks.isEmpty()) {
            return "";
        }

        StringBuilder csv = new StringBuilder();

        // The columns are the field names of the first check, already in order
        List<String> columns = new ArrayList<>(checks.get(0).getFields().keySet());

        ArrayList<String> header = new ArrayList<>();
        for (String column : columns) {
            header.add(quote(column));
        }
        csv.append(String.join(",", header)).append("\n");

        for (CheckRecord check : checks) {
            ArrayList<String> values = new ArrayList<>();
            for (String column : columns) {
                values.add(quote(check.get(column)));
            }
            csv.append(String.join(",", values)).append("\n");
        }

        return csv.toString();
    }

    // wrap a value in quotes so a comma inside it does not break the CSV
    private String quote(String value) {
        if (value == null) {
            value = "";
        }
        value = value.replace("\"", "\"\"");
        return "\"" + value + "\"";
    }
}
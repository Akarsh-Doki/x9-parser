package com.fcrm.fraud.x9parser.service;
 
import com.fcrm.fraud.x9parser.config.X9Config;
import com.fcrm.fraud.x9parser.model.CheckRecord;
 
import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
 
// Writes each check to the two CSV files, one row at a time. Kept separate from
public class CheckCsvWriter implements Closeable {
    private final BufferedWriter shortWriter;
    private final BufferedWriter bigWriter;
    private final List<String> bigColumns;
    private final X9Config config;
    private final String sourceFile;
    private final String today;

    public CheckCsvWriter(Path shortPath, Path bigPath, List<String> bigColumns,
                          X9Config config, String sourceFile) throws IOException {
        this.shortWriter = Files.newBufferedWriter(shortPath);
        this.bigWriter = Files.newBufferedWriter(bigPath);
        this.bigColumns = bigColumns;
        this.config = config;
        this.sourceFile = sourceFile;
        this.today = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        writeHeaders();
    }

    private void writeHeaders() throws IOException {
        List<String> shortColumns = new ArrayList<>();
        for (X9Config.FieldDef field:config.getCheckFields()) {
            shortColumns.add(field.getName());
        }
        shortWriter.write(csvLine(shortColumns));
        shortWriter.newLine();
        bigWriter.write(csvLine(bigColumns));
        bigWriter.newLine();
    }

    public void write(CheckRecord check) throws IOException {
        shortWriter.write(csvLine(shortValues(check)));
        shortWriter.newLine();
        bigWriter.write(csvLine(bigValues(check)));
        bigWriter.newLine();
    }

    private List<String> shortValues(CheckRecord check) {
        List<String> values = new ArrayList<>();
        for (X9Config.FieldDef field : config.getCheckFields()) {
            values.add(check.get(field.getName()));
        }
        return values;
    }

    private List<String> bigValues(CheckRecord check) {
        List<String> values = new ArrayList<>();
        for (String column : bigColumns) {
            values.add(bigValue(column, check));
        }
        return values;
    }

    // Decides what goes in one big-format column. Most columns are Orbograph
    private String bigValue(String column, CheckRecord check) {
        String mappedField = config.getBigFormatMapping().get(column);
        if (mappedField != null) {
            return check.get(mappedField);
        }
        if (column.equals("RT")) {
            return check.get("Payor Bank Routing Number") + check.get("Payor Bank Routing Number Check Digit");
        }
        if (column.equals("CaptDate") || column.equals("ProcDate")) {
            return today;
        }
        if (column.equals("CreditDebit")) {
            return config.getCreditDebit();
        }
        if (column.equals("Onus")) {
            return config.getOnus();
        }
        if (column.equals("FBW File") || column.equals("BBW File")) {
            return sourceFile;
        }
        return "";
    }

    private String csvLine(List<String> values) {
        StringBuilder line = new StringBuilder();
        for (int i = 0; i < values.size(); i++) {
            if (i > 0) {
                line.append(",");
            }
            line.append(quote(values.get(i)));
        }
        return line.toString();
    }

    // Wrap a value in quotes so a comma inside it doesn't break the CSV.
    private String quote(String value) {
        if (value == null) {
            value = "";
        }
        return "\"" + value.replace("\"", "\"\"") + "\"";
    }

    @Override
    public void close() throws IOException {
        shortWriter.close();
        bigWriter.close();
    }
}
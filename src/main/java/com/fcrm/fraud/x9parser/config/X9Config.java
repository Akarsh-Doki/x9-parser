package com.fcrm.fraud.x9parser.config;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
@ConfigurationProperties(prefix = "x9")
public class X9Config {
    private String outputDir = "./output";
    private String inputDir = "";
    private String creditDebit = "Y";
    private String onus = "Y";

    private List<FieldDef> checkFields = new ArrayList<>();

    private String bigFormatColumnsResource = "big-format-columns.csv";
    private Map<String, String> bigFormatMapping = new LinkedHashMap<>();

    public String getOutputDir() {
        return outputDir;
    }

    public void setOutputDir(String outputDir) {
        this.outputDir = outputDir;
    }

    public String getInputDir() {
        return inputDir;
    }

    public void setInputDir(String inputDir) {
        this.inputDir = inputDir;
    }

    public String getCreditDebit() {
        return creditDebit;
    }

    public void setCreditDebit(String creditDebit) {
        this.creditDebit = creditDebit;
    }

    public String getOnus() {
        return onus;
    }

    public void setOnus(String onus) {
        this.onus = onus;
    }

    public List<FieldDef> getCheckFields() {
        return checkFields;
    }

    public void setCheckFields(List<FieldDef> checkFields) {
        this.checkFields = checkFields;
    }

    public String getBigFormatColumnsResource() {
        return bigFormatColumnsResource;
    }

    public void setBigFormatColumnsResource(String bigFormatColumnsResource) {
        this.bigFormatColumnsResource = bigFormatColumnsResource;
    }

    public Map<String, String> getBigFormatMapping() {
        return bigFormatMapping;
    }

    public void setBigFormatMapping(Map<String, String> bigFormatMapping) {
        this.bigFormatMapping = bigFormatMapping;
    }

    // One field's name and its 1-based start/end position in the check record.
    public static class FieldDef {
        private String name;
        private int start;
        private int end;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getStart() {
            return start;
        }

        public void setStart(int start) {
            this.start = start;
        }

        public int getEnd() {
            return end;
        }

        public void setEnd(int end) {
            this.end = end;
        }
    }
}
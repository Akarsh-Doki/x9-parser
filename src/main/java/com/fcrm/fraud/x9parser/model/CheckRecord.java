package com.fcrm.fraud.x9parser.model;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * One check from a Check Detail record (type 25). Holds its fields by name, in the
 * order they appear in the file, which is also the order the CSV columns come out.
 */
public class CheckRecord {

    private final Map<String, String> fields;

    public CheckRecord(Map<String, String> fields) {
        // copy it so the check can't be changed after it's parsed
        this.fields = new LinkedHashMap<>(fields);
    }

    // all fields for this check, in order (read-only)
    public Map<String, String> getFields() {
        return Collections.unmodifiableMap(fields);
    }

    // one field by name, or "" if this check doesn't have it
    public String get(String fieldName) {
        return fields.getOrDefault(fieldName, "");
    }

    @Override
    public String toString() {
        return "CheckRecord" + fields;
    }
}
package com.fcrm.fraud.x9parser.model;

import java.util.List;

/**
 * What the parser returns for one X9 file: the checks (for the table and CSV)
 * and the images (for the TIF downloads).
 */
public class ParseResult {

    private final List<CheckRecord> checks;
    private final List<CheckImage> images;

    public ParseResult(List<CheckRecord> checks, List<CheckImage> images) {
        this.checks = List.copyOf(checks);
        this.images = List.copyOf(images);
    }

    public List<CheckRecord> getChecks() {
        return checks;
    }

    public List<CheckImage> getImages() {
        return images;
    }

    public int getCheckCount() {
        return checks.size();
    }

    public int getImageCount() {
        return images.size();
    }
}
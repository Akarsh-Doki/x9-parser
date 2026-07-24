package com.fcrm.fraud.x9parser.model;

/**
 * The small report shown after a file is processed: how much was parsed, where
 * the output went, and how long it took. 
 */
public class ProcessSummary {

    private final int checksParsed;
    private final int rowsWritten;
    private final int imagesWritten;
    private final long millis;
    private String bigCsvPath;
    private String shortCsvPath;
    private String imagesDir;

    public ProcessSummary(int checksParsed, int rowsWritten, int imagesWritten, long millis) {
        this.checksParsed = checksParsed;
        this.rowsWritten = rowsWritten;
        this.imagesWritten = imagesWritten;
        this.millis = millis;
    }

    public int getChecksParsed() {
        return checksParsed;
    }

    public int getRowsWritten() {
        return rowsWritten;
    }

    public int getImagesWritten() {
        return imagesWritten;
    }

    public long getMillis() {
        return millis;
    }

    // checks per second, which is used to show the tool handles big files quickly
    public long getChecksPerSecond() {
        if (millis <= 0) {
            return checksParsed;
        }
        return checksParsed * 1000L / millis;
    }

    public String getBigCsvPath() {
        return bigCsvPath;
    }

    public void setBigCsvPath(String bigCsvPath) {
        this.bigCsvPath = bigCsvPath;
    }

    public String getShortCsvPath() {
        return shortCsvPath;
    }

    public void setShortCsvPath(String shortCsvPath) {
        this.shortCsvPath = shortCsvPath;
    }

    public String getImagesDir() {
        return imagesDir;
    }

    public void setImagesDir(String imagesDir) {
        this.imagesDir = imagesDir;
    }
}
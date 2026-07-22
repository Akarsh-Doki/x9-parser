package com.fcrm.fraud.x9parser.model;

public class CheckImage {

    // which side of the check the image is; the code (F/R) is used in the file name
    public enum Side {
        FRONT("F", "Front"),
        REAR("R", "Rear");

        private final String code;
        private final String label;

        Side(String code, String label) {
            this.code = code;
            this.label = label;
        }

        public String getCode() {
            return code;
        }

        public String getLabel() {
            return label;
        }
    }

    private final int checkNumber; // 1-based: the Nth check in the file
    private final Side side;
    private final byte[] data;

    public CheckImage(int checkNumber, Side side, byte[] data) {
        this.checkNumber = checkNumber;
        this.side = side;
        this.data = data;
    }

    public int getCheckNumber() {
        return checkNumber;
    }

    public Side getSide() {
        return side;
    }

    public byte[] getData() {
        return data;
    }
    public String getFileName() {
        return "check" + checkNumber + "_" + side.getCode() + ".tif";
    }

    // a human-readable size, e.g. "12.3 KB"
    public String getSizeLabel() {
        if (data.length < 1024) {
            return data.length + " B";
        }
        return String.format("%.1f KB", data.length / 1024.0);
    }
}
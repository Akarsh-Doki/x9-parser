package com.fcrm.fraud.x9parser.exception;

/**
 * Thrown when an X9 file can't be parsed (empty file, not an X9 file, or a bad record).
 * Unchecked so it bubbles up to the web layer to become a friendly message.
 */
public class X9ParseException extends RuntimeException {

    public X9ParseException(String message) {
        super(message);
    }

    public X9ParseException(String message, Throwable cause) {
        super(message, cause);
    }
}
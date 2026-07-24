package com.fcrm.fraud.x9parser.controller;

import com.fcrm.fraud.x9parser.exception.X9ParseException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.support.RequestContextUtils;

// Turns a bad file into a friendly message on the form, in one place, instead of each controller handling it
@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(X9ParseException.class)
    public String handleParseError(X9ParseException e, HttpServletRequest request) {
        log.warn("Rejected a file: {}", e.getMessage());
        RequestContextUtils.getOutputFlashMap(request).put("error", e.getMessage());
        return "redirect:/";
    }
}
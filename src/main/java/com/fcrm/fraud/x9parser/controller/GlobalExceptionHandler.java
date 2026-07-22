package com.fcrm.fraud.x9parser.controller;

import com.fcrm.fraud.x9parser.exception.X9ParseException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.servlet.support.RequestContextUtils;

/**
 * One place that turns known errors into a friendly message on the upload form,
 * instead of each controller handling them separately. Anything not handled
 * here falls through to the error page (templates/error.html).
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // the parser rejected the file (empty, not an X9 file, corrupt)
    @ExceptionHandler(X9ParseException.class)
    public String handleParseError(X9ParseException e, HttpServletRequest request) {
        log.warn("Rejected an upload: {}", e.getMessage());
        return redirectWithError(request, e.getMessage());
    }

    // the upload is over the size limit set in application.properties
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public String handleTooLarge(MaxUploadSizeExceededException e, HttpServletRequest request) {
        log.warn("Rejected an upload: file too large");
        return redirectWithError(request, "That file is too large. The limit is 25 MB.");
    }

    // the request wasn't a proper file upload (e.g. a bare POST with no form)
    @ExceptionHandler(MultipartException.class)
    public String handleNotMultipart(MultipartException e, HttpServletRequest request) {
        log.warn("Rejected a request that was not a file upload");
        return redirectWithError(request, "Please upload the file using the form.");
    }

    private String redirectWithError(HttpServletRequest request, String message) {
        RequestContextUtils.getOutputFlashMap(request).put("error", message);
        return "redirect:/";
    }
}
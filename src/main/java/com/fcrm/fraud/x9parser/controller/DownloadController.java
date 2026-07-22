package com.fcrm.fraud.x9parser.controller;

import com.fcrm.fraud.x9parser.model.CheckImage;
import com.fcrm.fraud.x9parser.model.ParseResult;
import com.fcrm.fraud.x9parser.service.X9CsvWriter;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.net.URI;
import java.nio.charset.StandardCharsets;

/**
 * Serves the parsed results as downloads: the CSV of check data and the
 * individual TIF images. Both read the ParseResult that the upload put in
 * the session, so nothing is parsed twice and nothing touches the disk.
 */
@Controller
public class DownloadController {

    private static final MediaType TIFF = MediaType.parseMediaType("image/tiff");
    private static final MediaType CSV = MediaType.parseMediaType("text/csv");

    private final X9CsvWriter csvWriter;

    public DownloadController(X9CsvWriter csvWriter) {
        this.csvWriter = csvWriter;
    }

    // the CSV of all check data
    @GetMapping("/download/csv")
    public ResponseEntity<byte[]> downloadCsv(HttpSession session) {
        ParseResult result = (ParseResult) session.getAttribute(UploadController.RESULT_ATTRIBUTE);
        if (result == null) {
            return redirectHome();
        }

        String csv = csvWriter.toCsv(result.getChecks());
        return asDownload(csv.getBytes(StandardCharsets.UTF_8), "checks.csv", CSV);
    }

    // one TIF image, looked up by its file name (e.g. check1_F.tif)
    @GetMapping("/download/image/{fileName}")
    public ResponseEntity<byte[]> downloadImage(@PathVariable String fileName, HttpSession session) {
        ParseResult result = (ParseResult) session.getAttribute(UploadController.RESULT_ATTRIBUTE);
        if (result == null) {
            return redirectHome();
        }

        // the name is only compared against the parsed images held in memory,
        // so a crafted name can never reach the file system
        for (CheckImage image : result.getImages()) {
            if (image.getFileName().equals(fileName)) {
                return asDownload(image.getData(), image.getFileName(), TIFF);
            }
        }
        return ResponseEntity.notFound().build();
    }

    // wrap bytes as a file download with the right name and type
    private ResponseEntity<byte[]> asDownload(byte[] data, String fileName, MediaType type) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(type);
        headers.setContentDisposition(ContentDisposition.attachment().filename(fileName).build());
        return new ResponseEntity<>(data, headers, HttpStatus.OK);
    }

    // no parsed result in the session - send the user back to the upload form
    private ResponseEntity<byte[]> redirectHome() {
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(URI.create("/"));
        return new ResponseEntity<>(headers, HttpStatus.FOUND);
    }
}
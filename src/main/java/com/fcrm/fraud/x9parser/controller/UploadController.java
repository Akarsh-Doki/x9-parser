package com.fcrm.fraud.x9parser.controller;

import com.fcrm.fraud.x9parser.model.ParseResult;
import com.fcrm.fraud.x9parser.service.X9ParserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Controller
public class UploadController {

    // session attribute names, shared with DownloadController
    public static final String RESULT_ATTRIBUTE = "result";
    public static final String FILE_NAME_ATTRIBUTE = "fileName";

    private final X9ParserService parserService;

    public UploadController(X9ParserService parserService) {
        this.parserService = parserService;
    }

    // show the upload form
    @GetMapping("/")
    public String showUploadForm() {
        return "upload";
    }

    // take the uploaded file, parse it, then send the user to the results page
    @PostMapping("/upload")
    public String handleUpload(@RequestParam("file") MultipartFile file,
                               HttpSession session,
                               RedirectAttributes redirectAttributes) {
        if (file.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Please choose a file to upload.");
            return "redirect:/";
        }

        try {
            ParseResult result = parserService.parse(file.getBytes());
            session.setAttribute(RESULT_ATTRIBUTE, result);
            session.setAttribute(FILE_NAME_ATTRIBUTE, file.getOriginalFilename());
            return "redirect:/result";
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("error", "Could not read the uploaded file.");
            return "redirect:/";
        }
        // an X9ParseException from the service is handled by GlobalExceptionHandler
    }

    // show the results that are held in the session
    @GetMapping("/result")
    public String showResult(HttpSession session, Model model) {
        ParseResult result = (ParseResult) session.getAttribute(RESULT_ATTRIBUTE);
        if (result == null) {
            // nothing to show (e.g. the page was opened directly) - go back to the form
            return "redirect:/";
        }

        // the table columns are the field names, taken from the first check
        List<String> columns = new ArrayList<>();
        if (!result.getChecks().isEmpty()) {
            columns.addAll(result.getChecks().get(0).getFields().keySet());
        }

        model.addAttribute("result", result);
        model.addAttribute("columns", columns);
        model.addAttribute("fileName", session.getAttribute(FILE_NAME_ATTRIBUTE));
        return "result";
    }
}
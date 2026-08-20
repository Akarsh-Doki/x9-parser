package com.fcrm.fraud.x9parser.controller;

import com.fcrm.fraud.x9parser.model.ProcessSummary;
import com.fcrm.fraud.x9parser.service.X9StreamProcessor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

@Controller
public class ParseController {

    private final X9StreamProcessor processor;

    public ParseController(X9StreamProcessor processor) {
        this.processor = processor;
    }

    @GetMapping("/")
    public String showForm(Principal principal, Model model) {
        addUsername(principal, model);
        System.out.println("=== HOME PAGE HIT ===");
        return "index";
    }

    @PostMapping("/parse")
    public String parse(@RequestParam("filePath") String filePath, Principal principal,
                        Model model, RedirectAttributes redirectAttributes) {
        if (filePath == null || filePath.isBlank()) {
            redirectAttributes.addFlashAttribute("error", "Please enter a file path.");
            return "redirect:/";
        }

        ProcessSummary summary = processor.process(filePath.trim());
        addUsername(principal, model);
        model.addAttribute("summary", summary);
        return "result";
    }

    private void addUsername(Principal principal, Model model) {
        if (principal != null) {
            model.addAttribute("username", principal.getName());
        }
    }
}
package com.fcrm.fraud.x9parser.controller;

import com.fcrm.fraud.x9parser.model.ProcessSummary;
import com.fcrm.fraud.x9parser.service.X9StreamProcessor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class ParseController {

    private final X9StreamProcessor processor;

    public ParseController(X9StreamProcessor processor) {
        this.processor = processor;
    }

    @GetMapping("/")
    public String showForm(@AuthenticationPrincipal OidcUser user, Model model) {
        addUsername(user, model);
        return "index";
    }

    @PostMapping("/parse")
    public String parse(@RequestParam("filePath") String filePath, @AuthenticationPrincipal OidcUser user,
                        Model model, RedirectAttributes redirectAttributes) {
        if (filePath == null || filePath.isBlank()) {
            redirectAttributes.addFlashAttribute("error", "Please enter a file path.");
            return "redirect:/";
        }

        ProcessSummary summary = processor.process(filePath.trim());
        addUsername(user, model);
        model.addAttribute("summary", summary);
        return "result";
    }

    private void addUsername(OidcUser user, Model model) {
        if (user != null) {
            model.addAttribute("username", user.getPreferredUsername());
        }
    }
}
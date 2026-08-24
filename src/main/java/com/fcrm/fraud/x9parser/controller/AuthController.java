package com.fcrm.fraud.x9parser.controller;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.security.Principal;

@Controller
public class AuthController {
    
    @GetMapping("/login")
    public String showLogin(){
        return "login";
    }

    @GetMapping("/no-permission")
    public String showNoPermission(@AuthenticationPrincipal OidcUser user, Model model) {
        if (user != null) {
            model.addAttribute("username", user.getPreferredUsername());
        }
        return "no-permission";
    }
}

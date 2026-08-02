package com.fcrm.fraud.x9parser.controller;

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
    public String showNoPermission(Principal principal, Model model){
        if (principal != null){
            model.addAttribute("username", principal.getName());
        }
        return "no-permission";
    }
}

package com.paymybuddy.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class LoginController {

    @GetMapping("/login")
    public String loginPage(
            @RequestParam(value = "error",   required = false) String error,
            @RequestParam(value = "logout",  required = false) String logout,
            Model model) {
        if (error != null) {
            model.addAttribute("loginError", "Mail ou mot de passe invalide");
        }
        if (logout != null) {
            model.addAttribute("logoutMsg", "Vous avez bien été déconnecté");
        }
        return "login";
    }
}

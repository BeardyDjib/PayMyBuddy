package com.paymybuddy.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Contrôleur pour la gestion de la page de connexion (form‑login).
 * <p>
 * Lorsque Spring Security redirige sur /login, cette méthode renvoie
 * simplement le template Thymeleaf "login.html".
 * Le formulaire POST est automatiquement géré par Spring Security.
 * </p>
 */
@Controller
public class LoginController {

    /**
     * Affiche le formulaire de connexion.
     * <p>
     * GET /login → renvoie src/main/resources/templates/login.html
     * </p>
     *
     * @return le nom du template Thymeleaf "login"
     */
    @GetMapping("/login")
    public String showLoginForm() {
        return "login";
    }
}

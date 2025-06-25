package com.paymybuddy.web;

import com.paymybuddy.exception.UserAlreadyExistsException;
import com.paymybuddy.model.AppUser;
import com.paymybuddy.service.AppUserService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

/**
 * Contrôleur pour gérer l’inscription.
 */
@Controller
@RequestMapping("/users")
public class UserController {

    private final AppUserService userService;

    public UserController(AppUserService userService) {
        this.userService = userService;
    }

    /** Affiche le formulaire d’inscription */
    @GetMapping("/new")
    public String showForm(Model model) {
        model.addAttribute("appUser", new AppUser());
        return "user-form";
    }

    /** Traite l’envoi du formulaire d’inscription */
    @PostMapping
    public String register(
            @Valid @ModelAttribute("appUser") AppUser appUser,
            BindingResult bindingResult,
            Model model) {

        if (bindingResult.hasErrors()) {
            return "user-form"; // retourne le formulaire avec les erreurs
        }

        try {
            userService.register(appUser);
            return "redirect:/login?registered";
        } catch (UserAlreadyExistsException e) {
            model.addAttribute("error", e.getMessage());
            return "user-form";
        }
    }
}

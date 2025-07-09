package com.paymybuddy.web;

import com.paymybuddy.dto.AppUserDto;
import com.paymybuddy.exception.UserAlreadyExistsException;
import com.paymybuddy.model.AppUser;
import com.paymybuddy.service.AppUserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import jakarta.validation.Valid;

@Controller
public class RegisterController {

    private final AppUserService userService;

    public RegisterController(AppUserService userService) {
        this.userService = userService;
    }

    @GetMapping("/users/new")
    public String showRegistrationForm(Model model) {
        model.addAttribute("appUserDto", new AppUserDto());
        return "register";
    }

    @PostMapping("/users")
    public String processRegistration(
            @Valid @ModelAttribute("appUserDto") AppUserDto dto,
            BindingResult bindingResult,
            Model model) {

        if (bindingResult.hasErrors()) {
            return "register";
        }

        try {
            // Convertit en entité et enregistre
            AppUser toSave = dto.toEntity();
            userService.register(toSave);
        } catch (UserAlreadyExistsException ex) {
            model.addAttribute("registrationError", ex.getMessage());
            return "register";
        }

        // Redirige vers login avec message optionnel
        return "redirect:/login?registered";
    }
}

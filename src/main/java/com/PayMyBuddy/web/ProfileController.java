package com.paymybuddy.web;

import com.paymybuddy.dto.AppUserDto;
import com.paymybuddy.model.AppUser;
import com.paymybuddy.service.AppUserService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class ProfileController {

    private final AppUserService userService;

    public ProfileController(AppUserService userService) {
        this.userService = userService;
    }

    @GetMapping({"/", "/profile"})
    public String showProfile(Authentication auth, Model model) {
        String email = auth.getName();
        AppUser user = userService.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("Utilisateur introuvable : " + email));
        AppUserDto dto = AppUserDto.fromEntity(user);
        model.addAttribute("user", dto);
        // Les flash attributes (passwordUpdated / passwordError) seront ajoutés si on redirige ici
        return "profile";
    }

    @PostMapping("/profile/change-password")
    public String changePassword(
            Authentication auth,
            String currentPassword,
            String newPassword,
            String confirmPassword,
            RedirectAttributes flash
    ) {
        String email = auth.getName();
        try {
            userService.updatePassword(email, currentPassword, newPassword, confirmPassword);
            flash.addFlashAttribute("passwordUpdated", "Mot de passe modifié avec succès !");
        } catch (IllegalArgumentException ex) {
            flash.addFlashAttribute("passwordError", ex.getMessage());
        }
        // On redirige vers GET /profile, qui réinjecte 'user' dans le modèle
        return "redirect:/profile";
    }
}

package com.paymybuddy.web;

import com.paymybuddy.dto.TransactionDto;
import com.paymybuddy.dto.UserConnectionDto;
import com.paymybuddy.exception.ResourceNotFoundException;
import com.paymybuddy.model.AppUser;
import com.paymybuddy.service.AppUserService;
import com.paymybuddy.service.TransactionService;
import com.paymybuddy.service.UserConnectionService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

/**
 * Contrôleur Web pour l’interface Thymeleaf.
 * <p>
 * La page d’accueil affiche uniquement les données
 * (transactions, connexions) de l’utilisateur connecté.
 * </p>
 */
@Controller
@RequestMapping("/")
public class WebController {

    private final AppUserService userService;
    private final TransactionService txService;
    private final UserConnectionService connService;

    public WebController(AppUserService userService,
                         TransactionService txService,
                         UserConnectionService connService) {
        this.userService = userService;
        this.txService   = txService;
        this.connService = connService;
    }

    /**
     * Page d’accueil : affiche les transactions et connexions
     * de l’utilisateur authentifié.
     */
    @GetMapping
    public String home(Model model) {
        // 1. Récupère l’email du user connecté
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();

        // 2. Charge l’entité AppUser pour avoir son ID
        AppUser current = userService.findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Utilisateur introuvable : " + email)
                );

        // 3. Récupère ses transactions et ses connexions
        List<TransactionDto> txList = txService.findBySenderIdDto(current.getId().intValue());
        List<UserConnectionDto> conns = connService.getConnectionsForUser(current.getId().intValue());

        // 4. Ajoute au modèle pour Thymeleaf
        model.addAttribute("txList", txList);
        model.addAttribute("conns", conns);

        return "home";
    }
}

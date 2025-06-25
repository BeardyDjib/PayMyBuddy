package com.paymybuddy.web;

import com.paymybuddy.dto.UserConnectionDto;
import com.paymybuddy.exception.ResourceNotFoundException;
import com.paymybuddy.model.AppUser;
import com.paymybuddy.service.AppUserService;
import com.paymybuddy.service.UserConnectionService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Contrôleur Web Thymeleaf pour la gestion des connexions ("amis").
 */
@Controller
@RequestMapping("/connections")
public class ConnectionWebController {

    private final UserConnectionService connService;
    private final AppUserService        userService;

    public ConnectionWebController(UserConnectionService connService,
                                   AppUserService userService) {
        this.connService = connService;
        this.userService = userService;
    }

    /**
     * GET /connections
     * Affiche toutes les connexions de l’utilisateur authentifié.
     */
    @GetMapping
    public String listConn(Model model, Authentication auth) {
        // 1) Récupérer l’email et le pseudo du user connecté
        String myEmail = auth.getName();
        AppUser me = userService.findByEmail(myEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable : " + myEmail));

        // 2) Récupérer ses connexions (DTO simples)
        List<UserConnectionDto> conns = connService.getConnectionsForUser(me.getId().intValue());

        // 3) Pour chaque connexion, préparer un ConnectionView
        List<ConnectionView> rows = conns.stream().map(c -> {
            AppUser friend = userService.findById(c.getConnectionId().longValue())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Utilisateur introuvable (ID=" + c.getConnectionId() + ")"));
            return new ConnectionView(
                    me.getUsername(),
                    friend.getEmail(),
                    friend.getUsername()
            );
        }).collect(Collectors.toList());

        // 4) Passage au modèle
        model.addAttribute("rows", rows);

        return "connections";
    }

    /**
     * GET /connections/new
     * Affiche le formulaire d’ajout d’un ami (champ email uniquement).
     */
    @GetMapping("/new")
    public String newConnForm(Model model) {
        model.addAttribute("friendEmail", "");
        return "connection-form";
    }

    /**
     * POST /connections
     * Traite l’ajout d’une nouvelle connexion.
     */
    @PostMapping
    public String createConn(@RequestParam("friendEmail") String friendEmail,
                             Authentication auth) {
        String myEmail = auth.getName();
        Integer userId = userService.findByEmail(myEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable : " + myEmail))
                .getId().intValue();

        Integer connectionId = userService.findByEmail(friendEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable : " + friendEmail))
                .getId().intValue();

        connService.addConnection(new UserConnectionDto(userId, connectionId));
        return "redirect:/connections";
    }
}

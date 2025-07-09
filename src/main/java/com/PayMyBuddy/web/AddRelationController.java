package com.paymybuddy.web;

import com.paymybuddy.dto.UserConnectionDto;
import com.paymybuddy.model.AppUser;
import com.paymybuddy.service.AppUserService;
import com.paymybuddy.service.UserConnectionService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public class AddRelationController {

    private final UserConnectionService connectionService;
    private final AppUserService userService;

    public AddRelationController(UserConnectionService connectionService,
                                 AppUserService userService) {
        this.connectionService = connectionService;
        this.userService = userService;
    }

    @GetMapping("/add-relation")
    public String showAddRelationForm(Authentication auth, Model model) {
        String email = auth.getName();
        Integer userId = userService.findByEmail(email)
                .orElseThrow()
                .getId().intValue();

        UserConnectionDto formDto = new UserConnectionDto();
        formDto.setUserId(userId);

        model.addAttribute("connectionDto", formDto);
        return "add-relation";
    }

    @PostMapping("/add-relation")
    public String addRelation(@ModelAttribute("connectionDto") UserConnectionDto dto) {
        // 1) Recherche l'utilisateur cible via connectionEmail
        AppUser target = userService.findByEmail(dto.getConnectionEmail())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Utilisateur introuvable : " + dto.getConnectionEmail()));

        // 2) Injecte son ID dans le DTO
        dto.setConnectionId(target.getId().intValue());

        // 3) Appelle le service
        connectionService.addConnection(dto);

        return "redirect:/transfer";
    }
}

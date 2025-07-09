package com.paymybuddy.web;

import com.paymybuddy.dto.TransactionDto;
import com.paymybuddy.dto.UserConnectionDto;
import com.paymybuddy.service.AppUserService;
import com.paymybuddy.service.TransactionService;
import com.paymybuddy.service.UserConnectionService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

@Controller
public class TransferController {

    private final UserConnectionService connectionService;
    private final TransactionService transactionService;
    private final AppUserService appUserService;

    public TransferController(UserConnectionService connectionService,
                              TransactionService transactionService,
                              AppUserService appUserService) {
        this.connectionService = connectionService;
        this.transactionService = transactionService;
        this.appUserService = appUserService;
    }

    @GetMapping("/transfer")
    public String showTransferPage(Authentication auth, Model model) {
        // 1) Récupérer l'email et l'ID de l'utilisateur courant
        String email = auth.getName();
        Integer userId = appUserService.findByEmail(email)
                .orElseThrow()
                .getId()
                .intValue();

        // 2) Récupérer les connexions de l'utilisateur
        List<UserConnectionDto> connections =
                connectionService.getConnectionsForUser(userId);

        // 3) Récupérer ses transactions passées
        List<TransactionDto> txns =
                transactionService.findBySenderIdDto(userId);

        // 4) Préparer le DTO de formulaire
        TransactionDto formDto = new TransactionDto();
        formDto.setSenderId(userId);

        model.addAttribute("connections", connections);
        model.addAttribute("transactions", txns);
        model.addAttribute("transferDto", formDto);

        return "transfer";
    }

    @PostMapping("/transfer")
    public String doTransfer(@ModelAttribute("transferDto") TransactionDto dto,
                             Authentication auth) {
        // Récupérer et injecter l'ID de l'utilisateur connecté
        String email = auth.getName();
        Integer userId = appUserService.findByEmail(email)
                .orElseThrow()
                .getId()
                .intValue();
        dto.setSenderId(userId);

        // Créer la transaction
        transactionService.createTransaction(dto);

        return "redirect:/transfer";
    }
}

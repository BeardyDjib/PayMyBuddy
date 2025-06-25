package com.paymybuddy.web;

import com.paymybuddy.dto.TransactionDto;
import com.paymybuddy.exception.ResourceNotFoundException;
import com.paymybuddy.model.AppUser;
import com.paymybuddy.service.AppUserService;
import com.paymybuddy.service.TransactionService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Contrôleur Web Thymeleaf pour la gestion des transactions.
 * <p>
 * Routes :
 * <ul>
 *   <li>GET  /transactions     → liste les transactions de l’utilisateur connecté.</li>
 *   <li>GET  /transactions/new → affiche le formulaire d’envoi (destinataire par email).</li>
 *   <li>POST /transactions     → traite l’envoi d’argent.</li>
 * </ul>
 * </p>
 */
@Controller
@RequestMapping("/transactions")
public class TransactionWebController {

    private final TransactionService txService;
    private final AppUserService    userService;

    public TransactionWebController(TransactionService txService,
                                    AppUserService userService) {
        this.txService   = txService;
        this.userService = userService;
    }

    /**
     * Affiche la liste des transactions envoyées par l’utilisateur actuel.
     * <p>
     * - Récupère l’email du user connecté via Spring Security.<br>
     * - Charge l’entité AppUser pour obtenir son ID.<br>
     * - Appelle txService.findBySenderIdDto(...) pour récupérer ses transactions.<br>
     * - Ajoute la liste au modèle sous la clé "txList".<br>
     * - Rend le template "transactions.html".
     * </p>
     *
     * @param model Modèle Thymeleaf pour la vue.
     * @return le nom du template "transactions".
     */
    @GetMapping
    public String listTx(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();

        AppUser current = userService.findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Utilisateur introuvable : " + email)
                );

        List<TransactionDto> txs = txService.findBySenderIdDto(current.getId().intValue());
        model.addAttribute("txList", txs);
        return "transactions";
    }

    /**
     * Affiche le formulaire de création d’une transaction.
     * <p>
     * - On ajoute au modèle un TransactionDto vide sous la clé "tx".<br>
     * - Le formulaire ne contient que :
     *     • receiverEmail (email du destinataire)<br>
     *     • amount<br>
     *     • description
     * </p>
     *
     * @param model Modèle Thymeleaf pour la vue.
     * @return le nom du template "transaction-form".
     */
    @GetMapping("/new")
    public String newTxForm(Model model) {
        model.addAttribute("tx", new TransactionDto());
        return "transaction-form";
    }

    /**
     * Traite la soumission du formulaire d’envoi d’argent.
     * <p>
     * - Le DTO "tx" renvoyé contient receiverEmail, amount et description.<br>
     * - On récupère l’expéditeur depuis SecurityContext et on fixe senderId dans le DTO.<br>
     * - On appelle txService.createTransaction(dto).<br>
     * - On redirige vers la liste des transactions.
     * </p>
     *
     * @param dto TransactionDto lié par le formulaire (clé "tx").
     * @return redirection vers "/transactions".
     */
    @PostMapping
    public String createTx(@ModelAttribute("tx") TransactionDto dto) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();

        AppUser current = userService.findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Utilisateur introuvable : " + email)
                );

        // On fixe l’expéditeur dans le DTO
        dto.setSenderId(current.getId().intValue());

        // Création
        txService.createTransaction(dto);
        return "redirect:/transactions";
    }
}

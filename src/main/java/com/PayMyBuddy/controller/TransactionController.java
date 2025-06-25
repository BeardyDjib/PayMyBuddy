package com.paymybuddy.controller;

import com.paymybuddy.dto.TransactionDto;
import com.paymybuddy.model.Transaction;
import com.paymybuddy.service.TransactionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Contrôleur REST pour gérer les transactions financières entre utilisateurs.
 * <p>
 * Endpoints exposés :
 * <ul>
 *   <li>POST /api/transactions : créer une transaction</li>
 *   <li>GET  /api/transactions : lister toutes les transactions</li>
 * </ul>
 * </p>
 */
@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionService service;

    public TransactionController(TransactionService service) {
        this.service = service;
    }

    /**
     * Endpoint pour créer une nouvelle transaction.
     * <p>
     * Le corps de la requête doit contenir un JSON équivalent à TransactionDto (sans l’id).
     * Si tout se passe bien, renvoie la transaction créée (avec id) et HTTP 201.
     * </p>
     *
     * @param dto DTO de la transaction à créer (senderId, receiverId, description, amount).
     * @return ResponseEntity< Transaction > avec le statut HTTP CREATED et l’entité sauvegardée.
     */
    @PostMapping
    public ResponseEntity<Transaction> create(@RequestBody TransactionDto dto) {
        Transaction saved = service.createTransaction(dto);
        return new ResponseEntity<>(saved, HttpStatus.CREATED);
    }

    /**
     * Endpoint pour récupérer la liste de toutes les transactions existantes.
     * <p>
     * Renvoie une liste de TransactionDto (id, senderId, receiverId, description, amount).
     * </p>
     *
     * @return ResponseEntity< List<TransactionDto> > avec HTTP 200 et la liste des DTOs.
     */
    @GetMapping
    public ResponseEntity<List<TransactionDto>> listAll() {
        List<TransactionDto> list = service.findAllDto();
        return ResponseEntity.ok(list);
    }
}

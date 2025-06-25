package com.paymybuddy.service;

import com.paymybuddy.dto.TransactionDto;
import com.paymybuddy.exception.ResourceNotFoundException;
import com.paymybuddy.model.Transaction;
import com.paymybuddy.model.AppUser;
import com.paymybuddy.repository.TransactionRepository;
import com.paymybuddy.repository.AppUserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Service qui gère la logique métier des transactions.
 * <p>
 * Implémente les opérations :
 * - création d’une transaction (avec contrôle d’existence expéditeur et destinataire et montant > 0),
 * - liste de toutes les transactions,
 * - liste des transactions par expéditeur.
 * </p>
 */
@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final AppUserRepository     appUserRepository;

    public TransactionService(TransactionRepository transactionRepository,
                              AppUserRepository appUserRepository) {
        this.transactionRepository = transactionRepository;
        this.appUserRepository     = appUserRepository;
    }

    /**
     * Enregistre une nouvelle transaction.
     * <ol>
     *   <li>Vérifie que l’expéditeur existe (ID).</li>
     *   <li>Vérifie que le destinataire existe (email).</li>
     *   <li>Vérifie que le montant > 0.</li>
     *   <li>Calcule les frais (0,5%).</li>
     *   <li>Sauvegarde et renvoie l’entité.</li>
     * </ol>
     *
     * @param dto DTO contenant senderId, receiverEmail, description, amount.
     * @return Transaction JPA enregistrée.
     * @throws ResourceNotFoundException si expéditeur ou destinataire introuvable.
     * @throws IllegalArgumentException  si montant ≤ 0.
     */
    @Transactional
    public Transaction createTransaction(TransactionDto dto) {
        // 1) Expéditeur
        AppUser sender = appUserRepository.findById(dto.getSenderId().longValue())
                .orElseThrow(() ->
                        new ResourceNotFoundException("Expéditeur introuvable (ID = " + dto.getSenderId() + ")")
                );

        // 2) Destinataire par email
        AppUser receiver = appUserRepository.findByEmail(dto.getReceiverEmail())
                .orElseThrow(() ->
                        new ResourceNotFoundException("Destinataire introuvable (email = " + dto.getReceiverEmail() + ")")
                );

        // 3) Montant strictement positif
        if (dto.getAmount() == null || dto.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Le montant doit être strictement supérieur à 0");
        }

        // 4) Frais 0,5%
        BigDecimal feePercent = BigDecimal.valueOf(0.5);

        // 5) Construction de l’entité
        Transaction t = new Transaction();
        t.setSenderId(sender.getId().intValue());
        t.setReceiverId(receiver.getId().intValue());
        t.setDescription(dto.getDescription());
        t.setAmount(dto.getAmount());
        t.setFeePercent(feePercent);

        // Sauvegarde
        return transactionRepository.save(t);
    }

    /**
     * Récupère toutes les transactions et convertit en DTO.
     * @return liste de TransactionDto.
     */
    @Transactional(readOnly = true)
    public List<TransactionDto> findAllDto() {
        List<Transaction> all = transactionRepository.findAll();
        List<TransactionDto> dtos = new ArrayList<>();
        for (Transaction t : all) {
            dtos.add(toDto(t));
        }
        return dtos;
    }

    /**
     * Récupère les transactions envoyées par un expéditeur donné.
     * @param senderId ID de l’expéditeur.
     * @return liste de TransactionDto correspondantes.
     * @throws ResourceNotFoundException si l’expéditeur n’existe pas.
     */
    @Transactional(readOnly = true)
    public List<TransactionDto> findBySenderIdDto(Integer senderId) {
        // Vérifier existence expéditeur
        appUserRepository.findById(senderId.longValue())
                .orElseThrow(() ->
                        new ResourceNotFoundException("Expéditeur introuvable (ID = " + senderId + ")")
                );

        // Filtrer et convertir
        List<Transaction> sent = transactionRepository.findBySenderId(senderId);
        List<TransactionDto> dtos = new ArrayList<>();
        for (Transaction t : sent) {
            dtos.add(toDto(t));
        }
        return dtos;
    }

    /**
     * Convertit une entité Transaction en TransactionDto.
     * <p>
     * On expose l’email du destinataire (plus lisible côté Thymeleaf).
     * </p>
     */
    private TransactionDto toDto(Transaction t) {
        String receiverEmail = appUserRepository.findById(t.getReceiverId().longValue())
                .map(AppUser::getEmail)
                .orElse("inconnu");

        return new TransactionDto(
                t.getId(),
                t.getSenderId(),
                receiverEmail,
                t.getDescription(),
                t.getAmount()
        );
    }
}

package com.paymybuddy.service;

import com.paymybuddy.dto.TransactionDto;
import com.paymybuddy.exception.ResourceNotFoundException;
import com.paymybuddy.model.Transaction;
import com.paymybuddy.repository.TransactionRepository;
import com.paymybuddy.repository.AppUserRepository;
import com.paymybuddy.exception.UserAlreadyExistsException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Service qui gère la logique métier des transactions.
 * <p>
 * Implémente les opérations :
 * - enregistrer une nouvelle transaction (avec contrôle d'existence des utilisateurs et montant > 0),
 * - lister toutes les transactions,
 * - lister par expéditeur,
 * - lister par destinataire.
 * </p>
 */
@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final AppUserRepository appUserRepository;

    public TransactionService(TransactionRepository transactionRepository,
                              AppUserRepository appUserRepository) {
        this.transactionRepository = transactionRepository;
        this.appUserRepository = appUserRepository;
    }

    /**
     * Enregistre une nouvelle transaction.
     * <p>Étapes :</p>
     * <ul>
     *   <li>Vérifier que l’expéditeur existe (sinon ResourceNotFoundException).</li>
     *   <li>Vérifier que le destinataire existe (sinon ResourceNotFoundException).</li>
     *   <li>Vérifier que le montant est strictement positif (sinon IllegalArgumentException).</li>
     *   <li>Calculer les frais en BigDecimal (ex. fee = amount × feePercent / 100).</li>
     *   <li>Enregistrer la transaction en base.</li>
     * </ul>
     *
     * @param dto DTO contenant senderId, receiverId, description, amount (en clair).
     * @return La transaction enregistrée (entité JPA).
     * @throws ResourceNotFoundException si senderId ou receiverId n’existe pas.
     * @throws IllegalArgumentException  si le montant est ≤ 0.
     */
    @Transactional
    public Transaction createTransaction(TransactionDto dto) {
        // Convertir senderId (Integer) en Long car AppUserRepository.findById attend un Long
        Long senderLong = dto.getSenderId().longValue();
        Long receiverLong = dto.getReceiverId().longValue();

        // Vérifier existence de l’expéditeur
        appUserRepository.findById(senderLong)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Expéditeur introuvable (ID = " + dto.getSenderId() + ")"));

        // Vérifier existence du destinataire
        appUserRepository.findById(receiverLong)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Destinataire introuvable (ID = " + dto.getReceiverId() + ")"));

        // Vérifier montant strictement positif
        if (dto.getAmount() == null || dto.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Le montant doit être strictement supérieur à 0");
        }

        // Calculer le pourcentage de frais (0,5 % par défaut)
        BigDecimal feePercent = BigDecimal.valueOf(0.5);

        // Créer l’entité Transaction
        Transaction t = new Transaction(
                dto.getSenderId(),
                dto.getReceiverId(),
                dto.getDescription(),
                dto.getAmount(),
                feePercent
        );

        // Sauvegarder en base
        return transactionRepository.save(t);
    }

    /**
     * Récupère toutes les transactions existantes, puis les convertit en DTO (sans frais).
     *
     * @return Liste de TransactionDto.
     */
    @Transactional(readOnly = true)
    public List<TransactionDto> getAllTransactions() {
        List<Transaction> list = transactionRepository.findAll();
        List<TransactionDto> result = new ArrayList<>();
        for (Transaction t : list) {
            result.add(toDto(t));
        }
        return result;
    }

    /**
     * Récupère les transactions envoyées par un expéditeur donné.
     *
     * @param senderId ID de l’expéditeur.
     * @return Liste de TransactionDto correspondantes.
     * @throws ResourceNotFoundException si l’expéditeur n’existe pas.
     */
    @Transactional(readOnly = true)
    public List<TransactionDto> getTransactionsBySender(Integer senderId) {
        // Convertir en Long pour le repository
        Long senderLong = senderId.longValue();

        // Vérifier existence de l’expéditeur
        appUserRepository.findById(senderLong)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Expéditeur introuvable (ID = " + senderId + ")"));

        List<Transaction> list = transactionRepository.findBySenderId(senderId);
        List<TransactionDto> result = new ArrayList<>();
        for (Transaction t : list) {
            result.add(toDto(t));
        }
        return result;
    }


    /**
     * Récupère les transactions reçues par un destinataire donné.
     *
     * @param receiverId ID du destinataire.
     * @return Liste de TransactionDto correspondantes.
     * @throws ResourceNotFoundException si le destinataire n’existe pas.
     */
    @Transactional(readOnly = true)
    public List<TransactionDto> getTransactionsByReceiver(Integer receiverId) {
        // Convertir en Long pour le repository
        Long receiverLong = receiverId.longValue();

        // Vérifier existence du destinataire
        appUserRepository.findById(receiverLong)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Destinataire introuvable (ID = " + receiverId + ")"));

        List<Transaction> list = transactionRepository.findByReceiverId(receiverId);
        List<TransactionDto> result = new ArrayList<>();
        for (Transaction t : list) {
            result.add(toDto(t));
        }
        return result;
    }

    /**
     * Convertit une entité Transaction en TransactionDto (sans frais).
     *
     * @param t Entité Transaction à convertir.
     * @return TransactionDto contenant ID, senderId, receiverId, description, amount.
     */
    private TransactionDto toDto(Transaction t) {
        return new TransactionDto(
                t.getId(),
                t.getSenderId(),
                t.getReceiverId(),
                t.getDescription(),
                t.getAmount()
        );
    }
}

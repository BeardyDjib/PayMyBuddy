package com.paymybuddy.dto;

import java.math.BigDecimal;

/**
 * Data Transfer Object pour la classe Transaction.
 * Ne contient que les informations essentielles à exposer dans l’API REST.
 */
public class TransactionDto {

    /** Identifiant unique de la transaction. */
    private Integer id;

    /** Identifiant de l’émetteur (sender). */
    private Integer senderId;

    /** Identifiant du destinataire (receiver). */
    private Integer receiverId;

    /** Description de la transaction. */
    private String description;

    /** Montant de la transaction. */
    private BigDecimal amount;

    /**
     * Constructeur vide nécessaire pour Jackson/Spring.
     */
    public TransactionDto() { }

    /**
     * Constructeur minimal pour un mapping rapide.
     * @param id         Identifiant de la transaction.
     * @param senderId   ID de l’expéditeur.
     * @param receiverId ID du destinataire.
     * @param description Description.
     * @param amount      Montant.
     */
    public TransactionDto(Integer id, Integer senderId, Integer receiverId,
                          String description, BigDecimal amount) {
        this.id = id;
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.description = description;
        this.amount = amount;
    }

    // === Getters / Setters ===

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getSenderId() {
        return senderId;
    }

    public void setSenderId(Integer senderId) {
        this.senderId = senderId;
    }

    public Integer getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(Integer receiverId) {
        this.receiverId = receiverId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}

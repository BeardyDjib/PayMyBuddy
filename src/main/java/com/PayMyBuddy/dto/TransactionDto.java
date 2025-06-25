package com.paymybuddy.dto;

import com.paymybuddy.model.Transaction;
import java.math.BigDecimal;

/**
 * Data Transfer Object pour la classe Transaction.
 * Ne contient que les informations essentielles à exposer
 * dans l’API REST et à passer au template Thymeleaf.
 */
public class TransactionDto {

    private Integer id;
    private Integer senderId;
    /**
     * Désormais on stocke l’email du destinataire directement.
     */
    private String receiverEmail;
    private String description;
    private BigDecimal amount;

    public TransactionDto() { }

    public TransactionDto(Integer id,
                          Integer senderId,
                          String receiverEmail,
                          String description,
                          BigDecimal amount) {
        this.id            = id;
        this.senderId      = senderId;
        this.receiverEmail = receiverEmail;
        this.description   = description;
        this.amount        = amount;
    }

    // === Getters / Setters ===

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getSenderId() { return senderId; }
    public void setSenderId(Integer senderId) { this.senderId = senderId; }

    public String getReceiverEmail() { return receiverEmail; }
    public void setReceiverEmail(String receiverEmail) { this.receiverEmail = receiverEmail; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    // === Conversion vers l’entité ===

    /**
     * Crée une entité Transaction à partir de ce DTO.
     */
    public Transaction toEntity() {
        Transaction t = new Transaction();
        t.setSenderId(this.senderId);
        // on devra résoudre l’ID du destinataire à partir de l’email dans le service
        t.setDescription(this.description);
        t.setAmount(this.amount);
        return t;
    }
}

package com.paymybuddy.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

/**
 * Représente une transaction financière entre deux utilisateurs (sender et receiver).
 * <p>
 * Chaque transaction a :
 * <ul>
 *   <li>un identifiant unique {@code id},</li>
 *   <li>un {@code sender} (utilisateur qui envoie l'argent),</li>
 *   <li>un {@code receiver} (utilisateur qui reçoit l'argent),</li>
 *   <li>une {@code description} optionnelle,</li>
 *   <li>un {@code amount} (montant) strictement positif,</li>
 *   <li>un {@code feePercent} représentant le pourcentage de commission (par défaut 0,5 %).</li>
 * </ul>
 * </p>
 */
@Entity
@Table(name = "transaction")
public class Transaction {

    /** Identifiant unique de la transaction (auto‐incrémenté). */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /**
     * Clé étrangère vers l'utilisateur qui envoie l'argent.
     * Correspond à la colonne sender_id dans la base.
     */
    @Column(name = "sender_id", nullable = false)
    private Integer senderId;

    /**
     * Clé étrangère vers l'utilisateur qui reçoit l'argent.
     * Correspond à la colonne receiver_id dans la base.
     */
    @Column(name = "receiver_id", nullable = false)
    private Integer receiverId;

    /** Description libre de la transaction (facultatif, jusqu'à 255 caractères). */
    @Column(length = 255)
    private String description;

    /**
     * Montant de la transaction.
     * Doit être strictement supérieur à zéro (contrainte CHECK dans la base).
     */
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    /**
     * Pourcentage de frais appliqué à la transaction.
     * Par défaut sur la colonne en base : 0,5 (=> 0,5 %).
     */
    @Column(name = "fee_percent", nullable = false, precision = 5, scale = 2)
    private BigDecimal feePercent = BigDecimal.valueOf(0.5);

    // =======================
    // == Constructeurs ==
    // =======================

    /** Constructeur vide requis par JPA. */
    public Transaction() { }

    /**
     * Constructeur pratique pour créer une transaction manuellement (avant enregistrement).
     * @param senderId    Identifiant de l'utilisateur émetteur.
     * @param receiverId  Identifiant de l'utilisateur récepteur.
     * @param description Description libre (peut être null).
     * @param amount      Montant de la transaction (strictement positif).
     * @param feePercent  Pourcentage de frais (par exemple 0.5 pour 0,5 %).
     */
    public Transaction(Integer senderId, Integer receiverId, String description,
                       BigDecimal amount, BigDecimal feePercent) {
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.description = description;
        this.amount = amount;
        this.feePercent = feePercent != null ? feePercent : BigDecimal.valueOf(0.5);
    }

    // =======================
    // == Getters / Setters ==
    // =======================

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
        // On peut éventuellement tronquer à 255 si l’utilisateur dépasse…
        this.description = description != null && description.length() > 255
                ? description.substring(0, 255)
                : description;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public BigDecimal getFeePercent() {
        return feePercent;
    }

    public void setFeePercent(BigDecimal feePercent) {
        this.feePercent = feePercent != null ? feePercent : BigDecimal.valueOf(0.5);
    }

    // =======================
    // == equals() / hashCode() / toString() ==
    // =======================

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Transaction)) return false;
        Transaction that = (Transaction) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return (id != null ? id.hashCode() : 0);
    }

    @Override
    public String toString() {
        return "Transaction{" +
                "id=" + id +
                ", senderId=" + senderId +
                ", receiverId=" + receiverId +
                ", description='" + description + '\'' +
                ", amount=" + amount +
                ", feePercent=" + feePercent +
                '}';
    }
}

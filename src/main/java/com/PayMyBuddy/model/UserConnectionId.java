package com.paymybuddy.model;

import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

/**
 * Clé composite pour l'entité UserConnection.
 * <p>
 * Cette classe combine userId et connectionId pour former la clé primaire.
 * Elle doit implémenter Serializable et redéfinir equals() et hashCode().
 * </p>
 */
@Embeddable
public class UserConnectionId implements Serializable {

    private static final long serialVersionUID = 1L;

    /** ID de l'utilisateur (clé étrangère vers AppUser). */
    private Integer userId;

    /** ID de la connexion (clé étrangère vers AppUser). */
    private Integer connectionId;

    /** Constructeur vide nécessaire à JPA. */
    public UserConnectionId() { }

    /**
     * Constructeur pratique.
     * @param userId        ID de l'utilisateur.
     * @param connectionId  ID de l'utilisateur connecté.
     */
    public UserConnectionId(Integer userId, Integer connectionId) {
        this.userId = userId;
        this.connectionId = connectionId;
    }

    // ====================
    // == Getters/Setters ==
    // ====================

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Integer getConnectionId() {
        return connectionId;
    }

    public void setConnectionId(Integer connectionId) {
        this.connectionId = connectionId;
    }

    // ====================
    // == equals()/hashCode() ==
    // ====================

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserConnectionId)) return false;
        UserConnectionId that = (UserConnectionId) o;
        return Objects.equals(userId, that.userId)
                && Objects.equals(connectionId, that.connectionId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, connectionId);
    }
}
package com.paymybuddy.model;

import jakarta.persistence.*;

/**
 * Entité représentant la relation "amis" (liaison) entre deux utilisateurs.
 * <p>
 * Clé primaire composite {@code UserConnectionId} sur (userId, connectionId).
 * Chaque enregistrement signifie que "userId" est connecté (ami) à "connectionId".
 * </p>
 */
@Entity
@Table(name = "user_connection")
public class UserConnection {

    /**
     * Clé composite (userId + connectionId).
     */
    @EmbeddedId
    private UserConnectionId id;

    /**
     * Constructeur vide requis par JPA.
     */
    public UserConnection() { }

    /**
     * Constructeur pratique pour créer une connexion.
     * @param userId       ID de l'utilisateur.
     * @param connectionId ID de l'utilisateur avec qui on se connecte.
     */
    public UserConnection(Integer userId, Integer connectionId) {
        this.id = new UserConnectionId(userId, connectionId);
    }

    // ====================
    // == Getters/Setters ==
    // ====================

    public UserConnectionId getId() {
        return id;
    }

    public void setId(UserConnectionId id) {
        this.id = id;
    }

    /**
     * Renvoie l'ID de l'utilisateur.
     * @return userId.
     */
    public Integer getUserId() {
        return id != null ? id.getUserId() : null;
    }

    /**
     * Renvoie l'ID de la connexion (ami).
     * @return connectionId.
     */
    public Integer getConnectionId() {
        return id != null ? id.getConnectionId() : null;
    }

    // ====================
    // == equals()/hashCode()/toString() ==
    // ====================

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserConnection)) return false;
        UserConnection that = (UserConnection) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "UserConnection{" +
                "userId=" + getUserId() +
                ", connectionId=" + getConnectionId() +
                '}';
    }
}
package com.paymybuddy.dto;

import com.paymybuddy.model.UserConnection;
import com.paymybuddy.model.UserConnectionId;

/**
 * DTO pour afficher les connexions avec des infos utilisateur.
 */
public class UserConnectionDto {

    private Integer userId;
    private Integer connectionId;

    private String myUsername;        // Nom du propriétaire
    private String friendEmail;       // Email de la connexion
    private String friendUsername;    // Nom de la connexion

    public UserConnectionDto() { }

    // === Constructeur de base (IDs uniquement) ===
    public UserConnectionDto(Integer userId, Integer connectionId) {
        this.userId = userId;
        this.connectionId = connectionId;
    }

    // === Constructeur enrichi pour affichage HTML ===
    public UserConnectionDto(Integer userId, Integer connectionId,
                             String myUsername, String friendEmail, String friendUsername) {
        this.userId = userId;
        this.connectionId = connectionId;
        this.myUsername = myUsername;
        this.friendEmail = friendEmail;
        this.friendUsername = friendUsername;
    }

    // === Getters / Setters ===

    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }

    public Integer getConnectionId() { return connectionId; }
    public void setConnectionId(Integer connectionId) { this.connectionId = connectionId; }

    public String getMyUsername() { return myUsername; }
    public void setMyUsername(String myUsername) { this.myUsername = myUsername; }

    public String getFriendEmail() { return friendEmail; }
    public void setFriendEmail(String friendEmail) { this.friendEmail = friendEmail; }

    public String getFriendUsername() { return friendUsername; }
    public void setFriendUsername(String friendUsername) { this.friendUsername = friendUsername; }

    // === Conversion vers entité ===

    public UserConnection toEntity() {
        return new UserConnection(this.userId, this.connectionId);
    }

    public UserConnectionId toId() {
        return new UserConnectionId(this.userId, this.connectionId);
    }
}

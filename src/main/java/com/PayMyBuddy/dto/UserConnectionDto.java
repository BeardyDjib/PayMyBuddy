package com.paymybuddy.dto;

import com.paymybuddy.model.UserConnection;
import com.paymybuddy.model.UserConnectionId;

public class UserConnectionDto {

    private Integer userId;
    private Integer connectionId;

    /** Pour la saisie du nouvel utilisateur à ajouter */
    private String connectionEmail;

    private String myUsername;
    private String friendEmail;
    private String friendUsername;

    public UserConnectionDto() { }

    // === Constructeurs existants ===
    public UserConnectionDto(Integer userId, Integer connectionId) {
        this.userId = userId;
        this.connectionId = connectionId;
    }
    public UserConnectionDto(Integer userId, Integer connectionId,
                             String myUsername, String friendEmail, String friendUsername) {
        this.userId = userId;
        this.connectionId = connectionId;
        this.myUsername = myUsername;
        this.friendEmail = friendEmail;
        this.friendUsername = friendUsername;
    }

    // === Getters/Setters ===
    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }

    public Integer getConnectionId() { return connectionId; }
    public void setConnectionId(Integer connectionId) { this.connectionId = connectionId; }

    public String getConnectionEmail() { return connectionEmail; }
    public void setConnectionEmail(String connectionEmail) { this.connectionEmail = connectionEmail; }

    public String getMyUsername() { return myUsername; }
    public void setMyUsername(String myUsername) { this.myUsername = myUsername; }

    public String getFriendEmail() { return friendEmail; }
    public void setFriendEmail(String friendEmail) { this.friendEmail = friendEmail; }

    public String getFriendUsername() { return friendUsername; }
    public void setFriendUsername(String friendUsername) { this.friendUsername = friendUsername; }

    // === Conversion vers l’entité ===
    public UserConnection toEntity() {
        return new UserConnection(this.userId, this.connectionId);
    }
    public UserConnectionId toId() {
        return new UserConnectionId(this.userId, this.connectionId);
    }
}

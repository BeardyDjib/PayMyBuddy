package com.paymybuddy.web;

/**
 * DTO de vue pour afficher une connexion :
 * • meUsername    : pseudo de l’utilisateur courant
 * • friendEmail   : email de l’ami
 * • friendUsername: pseudo de l’ami
 */
public class ConnectionView {
    private final String meUsername;
    private final String friendEmail;
    private final String friendUsername;

    public ConnectionView(String meUsername, String friendEmail, String friendUsername) {
        this.meUsername     = meUsername;
        this.friendEmail    = friendEmail;
        this.friendUsername = friendUsername;
    }

    public String getMeUsername() { return meUsername; }
    public String getFriendEmail() { return friendEmail; }
    public String getFriendUsername() { return friendUsername; }
}

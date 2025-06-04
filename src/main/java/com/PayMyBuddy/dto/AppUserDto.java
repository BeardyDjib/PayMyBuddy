package com.paymybuddy.dto;

/**
 * Data Transfer Object pour AppUser.
 * Ne contient pas le mot de passe, afin de ne pas l’exposer dans les réponses.
 */
public class AppUserDto {

    private Long id;
    private String username;
    private String email;

    /**
     * Constructeur vide (nécessaire pour Jackson et Spring).
     */
    public AppUserDto() { }

    /**
     * Constructeur pour plus de simplicité lors du mapping.
     * @param id        Identifiant de l’utilisateur.
     * @param username  Nom d’utilisateur.
     * @param email     Adresse e-mail.
     */
    public AppUserDto(Long id, String username, String email) {
        this.id = id;
        this.username = username;
        this.email = email;
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}

package com.paymybuddy.dto;

import com.paymybuddy.model.AppUser;

/**
 * Data Transfer Object pour AppUser.
 * <p>
 * Utilisé pour transférer les données utilisateur entre la couche web (Thymeleaf),
 * la couche REST et le service métier sans exposer directement l'entité JPA.
 * </p>
 */
public class AppUserDto {

    /** Identifiant de l'utilisateur (auto‑incrémenté). */
    private Long id;

    /** Nom d'affichage (pseudo). */
    private String username;

    /** Adresse e-mail unique. */
    private String email;

    /** Mot de passe en clair (sera haché lors de l'enregistrement). */
    private String password;

    /** Constructeur vide requis pour Jackson et Thymeleaf. */
    public AppUserDto() { }

    /**
     * Constructeur complet.
     *
     * @param id        Identifiant (peut être null pour un nouvel utilisateur).
     * @param username  Pseudo de l'utilisateur.
     * @param email     Adresse e-mail.
     * @param password  Mot de passe en clair.
     */
    public AppUserDto(Long id, String username, String email, String password) {
        this.id       = id;
        this.username = username;
        this.email    = email;
        this.password = password;
    }

    // --- Getters & Setters ---

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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    // --- Conversion vers / depuis l'entité ---

    /**
     * Convertit ce DTO en entité AppUser.
     * <p>
     * Le mot de passe est conservé en clair ici ; il sera haché par le service métier.
     * </p>
     *
     * @return une nouvelle instance de {@link AppUser} initialisée.
     */
    public AppUser toEntity() {
        AppUser user = new AppUser();
        user.setUsername(this.username);
        user.setEmail(this.email);
        user.setPassword(this.password);
        return user;
    }

    /**
     * Crée un DTO à partir d'une entité AppUser.
     * <p>
     * Utile pour préremplir un formulaire de modification, sans exposer le mot de passe.
     * </p>
     *
     * @param user l'entité AppUser.
     * @return un {@link AppUserDto} avec id, username et email (password laissé null).
     */
    public static AppUserDto fromEntity(AppUser user) {
        return new AppUserDto(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                null  // on ne renvoie jamais le mot de passe
        );
    }
}

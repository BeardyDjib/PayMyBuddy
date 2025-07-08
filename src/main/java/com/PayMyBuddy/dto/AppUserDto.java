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

    /** Adresse e-mail masquée pour la confidentialité. */
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
     * @param email     Adresse e-mail (déjà masquée si nécessaire).
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

    /**
     * Convertit ce DTO en entité AppUser.
     * <p>
     * Le mot de passe est conservé en clair ici; il sera haché par le service métier.
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
     * Masque l’adresse e‑mail en ne gardant que le premier caractère
     * et le domaine, le reste passant en «*».
     *
     * @param rawEmail adresse brute à masquer
     * @return e‑mail masquée, ex. j***@domaine.com
     */
    private static String maskEmail(String rawEmail) {
        if (rawEmail == null || !rawEmail.contains("@")) {
            return rawEmail;
        }
        String[] parts = rawEmail.split("@", 2);
        String local = parts[0];
        String domain = parts[1];
        if (local.length() <= 1) {
            return "*" + "@" + domain;
        }
        StringBuilder sb = new StringBuilder();
        sb.append(local.charAt(0));
        for (int i = 1; i < local.length(); i++) {
            sb.append('*');
        }
        sb.append('@').append(domain);
        return sb.toString();
    }

    /**
     * Crée un DTO à partir d'une entité AppUser, en masquant l’e‑mail
     * et en ne renvoyant jamais le mot de passe.
     *
     * @param user l'entité AppUser.
     * @return un {@link AppUserDto} avec id, username, e‑mail masquée.
     */
    public static AppUserDto fromEntity(AppUser user) {
        return new AppUserDto(
                user.getId(),
                user.getUsername(),
                maskEmail(user.getEmail()),
                null  // on ne renvoie jamais le mot de passe
        );
    }
}

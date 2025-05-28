package com.paymybuddy.model;

import jakarta.persistence.*;
import lombok.Data;

/**
 * Représente un utilisateur de l'application.
 */
@Entity
@Data
@Table(name = "app_user")
public class AppUser {

    /** Identifiant unique de l'utilisateur. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Nom d'utilisateur (pseudo). */
    @Column(nullable = false, length = 100)
    private String username;

    /** Adresse e-mail unique. */
    @Column(nullable = false, length = 100, unique = true)
    private String email;

    /** Mot de passe haché. */
    @Column(nullable = false)
    private String password;
}
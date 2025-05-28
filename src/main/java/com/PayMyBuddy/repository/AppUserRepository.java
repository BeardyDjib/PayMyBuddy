package com.paymybuddy.repository;

import com.paymybuddy.model.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

/**
 * Interface de gestion des utilisateurs, basée sur JpaRepository.
 * Hérite automatiquement des méthodes CRUD (ex : save, findById, delete...).
 */
public interface AppUserRepository extends JpaRepository<AppUser, Long> {

    /**
     * Recherche un utilisateur par son email.
     * @param email adresse email unique
     * @return un Optional contenant l'utilisateur si trouvé
     */
    Optional<AppUser> findByEmail(String email);
}

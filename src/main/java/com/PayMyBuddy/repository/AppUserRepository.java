package com.paymybuddy.repository;

import com.paymybuddy.model.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repository JPA pour l'entité AppUser.
 */
public interface AppUserRepository extends JpaRepository<AppUser, Long> {

    /**
     * Recherche un utilisateur par son adresse e-mail.
     * @param email L'adresse e-mail de l'utilisateur.
     * @return Optional contenant l'utilisateur si trouvé.
     */
    Optional<AppUser> findByEmail(String email);
}

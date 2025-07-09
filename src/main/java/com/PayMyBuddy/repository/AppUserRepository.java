package com.paymybuddy.repository;

import com.paymybuddy.model.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repository JPA pour l'entité AppUser.
 * <p>
 * Fournit des méthodes de persistance et de recherche sur les utilisateurs.
 * Hérite des fonctionnalités standards de {@link JpaRepository}, dont :
 * <ul>
 *   <li>{@code save(AppUser)}</li>
 *   <li>{@code findAll()}</li>
 *   <li>{@code findById(Long)}</li>
 *   <li>{@code deleteById(Long)}</li>
 * </ul>
 * </p>
 */
public interface AppUserRepository extends JpaRepository<AppUser, Long> {

    /**
     * Recherche un utilisateur par son adresse e-mail.
     *
     * @param email l'adresse e-mail de l'utilisateur
     * @return un {@link Optional} contenant l'utilisateur s'il est trouvé
     */
    Optional<AppUser> findByEmail(String email);
}

package com.paymybuddy.service;

import com.paymybuddy.exception.ResourceNotFoundException;
import com.paymybuddy.exception.UserAlreadyExistsException;
import com.paymybuddy.model.AppUser;
import com.paymybuddy.repository.AppUserRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


/**
 * Service gérant la logique métier des utilisateurs et l'authentification.
 * Implémente UserDetailsService pour Spring Security.
 */
@Service
public class AppUserService implements UserDetailsService {

    private final AppUserRepository repository;

    public AppUserService(AppUserRepository repository) {
        this.repository = repository;
    }

    /**
     * Charger un utilisateur pour Spring Security.
     * @param email Identifiant (email).
     * @return UserDetails contenant username, mot de passe et roles.
     * @throws ResourceNotFoundException si l'utilisateur n'existe pas.
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) {
        AppUser user = repository.findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Utilisateur non trouvé : " + email));
        // On crée un UserDetails simple avec email+password et rôle USER
        return User.builder()
                .username(user.getEmail())
                .password(user.getPassword())
                .roles("USER")
                .build();
    }


    /**
     * Enregistre un nouvel utilisateur après avoir vérifié que l'email n'est pas déjà utilisé.
     * Étapes :
     * - Vérifie si un utilisateur avec le même email existe déjà
     * - Si oui, lance une exception (UserAlreadyExistsException)
     * - Sinon, hache le mot de passe, puis sauvegarde en base
     *
     * @param user L'utilisateur à enregistrer.
     * @return L'utilisateur enregistré.
     * @throws UserAlreadyExistsException si un utilisateur avec le même email existe déjà.
     */
    @Transactional
    public AppUser register(AppUser user) {
        // Vérifie si un utilisateur avec cet email existe déjà
        if (repository.findByEmail(user.getEmail()).isPresent()) {
            throw new UserAlreadyExistsException("L'email est déjà utilisé : " + user.getEmail());
        }

        // Hash du mot de passe avant sauvegarde
        String hashed = BCrypt.hashpw(user.getPassword(), BCrypt.gensalt());
        user.setPassword(hashed);

        // Sauvegarde dans la base
        return repository.save(user);
    }
    /**
     * Récupère tous les utilisateurs.
     * @return Liste d'AppUser.
     */
    @Transactional(readOnly = true)
    public List<AppUser> findAll() {
        return repository.findAll();
    }

}
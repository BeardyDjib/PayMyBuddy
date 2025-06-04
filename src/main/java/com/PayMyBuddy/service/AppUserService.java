package com.paymybuddy.service;

import com.paymybuddy.dto.AppUserDto;
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

import java.util.ArrayList;
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
     * Charge un utilisateur à partir de son email pour Spring Security.
     * @param email Identifiant de connexion (email).
     * @return UserDetails contenant l’email, le mot de passe haché et le rôle ROLE_USER.
     * @throws ResourceNotFoundException si l’utilisateur n’existe pas.
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) {
        AppUser user = repository.findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Utilisateur non trouvé : " + email));
        return User.builder()
                .username(user.getEmail())
                .password(user.getPassword())
                .roles("USER")
                .build();
    }

    /**
     * Enregistre un nouvel utilisateur après avoir vérifié que l'email n'est pas déjà utilisé.
     * Si l’email existe, lève UserAlreadyExistsException.
     * @param user Objet AppUser contenant username, email et mot de passe en clair.
     * @return L'objet AppUser enregistré (avec mot de passe haché).
     * @throws UserAlreadyExistsException si un utilisateur avec le même email existe déjà.
     */
    @Transactional
    public AppUser register(AppUser user) {
        // Vérifier si l'email existe déjà
        if (repository.findByEmail(user.getEmail()).isPresent()) {
            throw new UserAlreadyExistsException("L'email est déjà utilisé : " + user.getEmail());
        }

        // Hacher le mot de passe avant de sauvegarder
        String hashed = BCrypt.hashpw(user.getPassword(), BCrypt.gensalt());
        user.setPassword(hashed);

        // Sauvegarder en base de données
        return repository.save(user);
    }

    /**
     * Récupère tous les utilisateurs, convertit chaque entité AppUser en AppUserDto
     * pour ne pas exposer le mot de passe.
     * @return Liste de AppUserDto (id, username, email uniquement).
     */
    @Transactional(readOnly = true)
    public List<AppUserDto> findAllDto() {
        List<AppUser> users = repository.findAll();
        List<AppUserDto> dtoList = new ArrayList<>();
        for (AppUser u : users) {
            dtoList.add(toDto(u));
        }
        return dtoList;
    }

    /**
     * Convertit une entité AppUser en AppUserDto (sans mot de passe).
     * @param user Entité AppUser à convertir.
     * @return AppUserDto ne contenant que id, username et email.
     */
    private AppUserDto toDto(AppUser user) {
        return new AppUserDto(
                user.getId(),
                user.getUsername(),
                user.getEmail()
        );
    }
}

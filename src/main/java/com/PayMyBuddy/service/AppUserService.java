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

import java.util.List;
import java.util.Optional;

/**
 * Service gérant la logique métier liée aux utilisateurs et à l’authentification.
 * <p>
 * Implémente {@link UserDetailsService} pour l'intégration avec Spring Security.
 * </p>
 */
@Service
public class AppUserService implements UserDetailsService {

    private final AppUserRepository repository;

    /**
     * Constructeur avec injection du repository utilisateur.
     *
     * @param repository repository JPA des utilisateurs.
     */
    public AppUserService(AppUserRepository repository) {
        this.repository = repository;
    }

    /**
     * Recherche un utilisateur par son adresse email.
     *
     * @param email l’email de l’utilisateur.
     * @return un {@link Optional} contenant l’utilisateur s’il existe.
     */
    @Transactional(readOnly = true)
    public Optional<AppUser> findByEmail(String email) {
        return repository.findByEmail(email);
    }

    /**
     * Recherche un utilisateur par son identifiant.
     *
     * @param id l’identifiant de l’utilisateur.
     * @return un {@link Optional} contenant l’utilisateur s’il existe.
     */
    @Transactional(readOnly = true)
    public Optional<AppUser> findById(Long id) {
        return repository.findById(id);
    }

    /**
     * Charge les informations d’un utilisateur par email (nom d'utilisateur) pour Spring Security.
     *
     * @param email l’email de l’utilisateur (utilisé comme nom d’utilisateur).
     * @return un {@link UserDetails} pour l’authentification.
     * @throws ResourceNotFoundException si l’utilisateur n’est pas trouvé.
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) {
        AppUser user = repository.findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Utilisateur non trouvé : " + email)
                );
        return User.builder()
                .username(user.getEmail())
                .password(user.getPassword())
                .roles("USER") // rôle par défaut
                .build();
    }

    /**
     * Enregistre un nouvel utilisateur avec mot de passe haché.
     *
     * @param user l’utilisateur à enregistrer (mot de passe en clair).
     * @return l’utilisateur sauvegardé avec un mot de passe sécurisé.
     * @throws UserAlreadyExistsException si l’email est déjà pris.
     */
    @Transactional
    public AppUser register(AppUser user) {
        if (repository.findByEmail(user.getEmail()).isPresent()) {
            throw new UserAlreadyExistsException(
                    "L'email est déjà utilisé : " + user.getEmail()
            );
        }
        String hashed = BCrypt.hashpw(user.getPassword(), BCrypt.gensalt());
        user.setPassword(hashed);
        return repository.save(user);
    }

    /**
     * Récupère tous les utilisateurs sous forme de DTO (sans mot de passe).
     * Utilise une projection fonctionnelle avec {@link AppUserDto#fromEntity(AppUser)}.
     *
     * @return liste de {@link AppUserDto}.
     */
    @Transactional(readOnly = true)
    public List<AppUserDto> findAllDto() {
        return repository.findAll()
                .stream()
                .map(AppUserDto::fromEntity)
                .toList();
    }
    @Transactional
    public void updatePassword(String email, String current, String next, String confirm) {
        var opt = repository.findByEmail(email);
        if (opt.isEmpty()) {
            throw new IllegalArgumentException("Utilisateur introuvable.");
        }
        AppUser user = opt.get();

        // 1) Vérifier le mot de passe actuel
        if (!BCrypt.checkpw(current, user.getPassword())) {
            throw new IllegalArgumentException("Mot de passe actuel incorrect.");
        }
        // 2) Vérifier la confirmation
        if (!next.equals(confirm)) {
            throw new IllegalArgumentException("La confirmation ne correspond pas.");
        }
        // 3) Hacher et sauvegarder
        String hash = BCrypt.hashpw(next, BCrypt.gensalt());
        user.setPassword(hash);
        repository.save(user);
    }
}
package com.paymybuddy.service;

import com.paymybuddy.dto.AppUserDto;
import com.paymybuddy.exception.ResourceNotFoundException;
import com.paymybuddy.exception.UserAlreadyExistsException;
import com.paymybuddy.model.AppUser;
import com.paymybuddy.repository.AppUserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires pour la classe AppUserService.
 * Utilise Mockito pour simuler le comportement du repository.
 */
@ExtendWith(MockitoExtension.class)
class AppUserServiceTest {

    @Mock
    private AppUserRepository repository;

    @InjectMocks
    private AppUserService service;

    /**
     * Test de loadUserByUsername quand l'utilisateur est introuvable.
     * Vérifie que ResourceNotFoundException est bien levée avec le bon message.
     */
    @Test
    void shouldThrowExceptionWhenUserNotFound() {
        // GIVEN : un email inexistant dans le repository
        String email = "notfound@email.com";
        when(repository.findByEmail(email)).thenReturn(Optional.empty());

        // WHEN + THEN : on s'attend à une ResourceNotFoundException
        ResourceNotFoundException thrown = assertThrows(ResourceNotFoundException.class, () -> {
            service.loadUserByUsername(email);
        });

        assertTrue(thrown.getMessage().contains("Utilisateur non trouvé"));
        verify(repository).findByEmail(email);
    }

    /**
     * Test de register quand l'email est déjà utilisé.
     * Doit lancer une UserAlreadyExistsException.
     */
    @Test
    void shouldThrowExceptionWhenEmailAlreadyExists() {
        // GIVEN : un utilisateur déjà existant en base avec le même email
        AppUser existing = new AppUser();
        existing.setEmail("test@email.com");

        AppUser userToRegister = new AppUser();
        userToRegister.setEmail("test@email.com");

        when(repository.findByEmail("test@email.com")).thenReturn(Optional.of(existing));

        // WHEN + THEN : on s’attend à une UserAlreadyExistsException
        UserAlreadyExistsException thrown = assertThrows(UserAlreadyExistsException.class, () -> {
            service.register(userToRegister);
        });

        assertTrue(thrown.getMessage().contains("L'email est déjà utilisé"));
        verify(repository).findByEmail("test@email.com");
    }

    /**
     * Test de findAllDto quand aucun utilisateur n'est présent.
     * Vérifie que la liste retournée est vide.
     */
    @Test
    void shouldReturnEmptyListWhenNoUsersFound() {
        // GIVEN : le repository retourne une liste vide
        when(repository.findAll()).thenReturn(Collections.emptyList());

        // WHEN : appel de la méthode
        List<AppUserDto> result = service.findAllDto();

        // THEN : la liste doit être vide
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(repository).findAll();
    }
}

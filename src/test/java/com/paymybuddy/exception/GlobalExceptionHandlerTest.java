package com.paymybuddy.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Classe de test unitaire pour la classe GlobalExceptionHandler.
 * <p>
 * Ce test vérifie le bon comportement du gestionnaire global d'exceptions utilisé dans l'application.
 * Il s'assure que chaque type d'exception déclenche la bonne réponse HTTP avec le bon message.
 * </p>
 */
class GlobalExceptionHandlerTest {

    // Instance du gestionnaire d'exception à tester
    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    /**
     * Teste la gestion d'une exception de type ResourceNotFoundException.
     * <p>
     * Vérifie que le handler renvoie une réponse avec :
     * - le code HTTP 404 (NOT FOUND)
     * - le message exact de l'exception
     * </p>
     */
    @Test
    void handleNotFound_shouldReturn404() {
        // GIVEN : une exception de type ResourceNotFoundException avec un message spécifique
        ResourceNotFoundException ex = new ResourceNotFoundException("Ressource non trouvée");

        // WHEN : le gestionnaire d'exception est appelé avec cette exception
        ResponseEntity<String> response = handler.handleNotFound(ex);

        // THEN : la réponse HTTP doit contenir un code 404 et le bon message d'erreur
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode(), "Le code HTTP doit être 404 NOT FOUND");
        assertEquals("Ressource non trouvée", response.getBody(), "Le message doit correspondre à celui de l'exception");
    }

    /**
     * Teste la gestion d'une exception de type UserAlreadyExistsException.
     * <p>
     * Vérifie que le handler renvoie une réponse avec :
     * - le code HTTP 409 (CONFLICT)
     * - le message exact de l'exception
     * </p>
     */
    @Test
    void handleUserAlreadyExists_shouldReturn409() {
        // GIVEN : une exception de type UserAlreadyExistsException avec un message spécifique
        UserAlreadyExistsException ex = new UserAlreadyExistsException("Utilisateur déjà existant");

        // WHEN : le gestionnaire d'exception est appelé avec cette exception
        ResponseEntity<String> response = handler.handleUserAlreadyExists(ex);

        // THEN : la réponse HTTP doit contenir un code 409 et le bon message d'erreur
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode(), "Le code HTTP doit être 409 CONFLICT");
        assertEquals("Utilisateur déjà existant", response.getBody(), "Le message doit correspondre à celui de l'exception");
    }

    /**
     * Teste la gestion d'une exception générique de type RuntimeException.
     * <p>
     * Vérifie que le handler renvoie une réponse avec :
     * - le code HTTP 500 (INTERNAL SERVER ERROR)
     * - un message formaté incluant le message d'origine de l'exception
     * </p>
     */
    @Test
    void handleRuntime_shouldReturn500() {
        // GIVEN : une exception de type RuntimeException avec un message d'erreur
        RuntimeException ex = new RuntimeException("Erreur interne");

        // WHEN : le gestionnaire d'exception est appelé avec cette exception
        ResponseEntity<String> response = handler.handleRuntime(ex);

        // THEN : la réponse HTTP doit contenir un code 500 et un message incluant celui de l'exception
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode(), "Le code HTTP doit être 500 INTERNAL SERVER ERROR");
        assertEquals("Erreur interne : Erreur interne", response.getBody(), "Le message doit être préfixé correctement et contenir celui de l'exception");
    }
}

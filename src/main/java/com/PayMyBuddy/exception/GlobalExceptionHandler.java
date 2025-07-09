package com.paymybuddy.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Gestionnaire global des exceptions pour les API REST.
 * Utilise @RestControllerAdvice pour renvoyer directement le message dans le corps HTTP.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Gère les ResourceNotFoundException et renvoie un 404.
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<String> handleNotFound(ResourceNotFoundException ex) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ex.getMessage());
    }

    /**
     * Gère les UserAlreadyExistsException et renvoie un 409 Conflict.
     */
    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<String> handleUserAlreadyExists(UserAlreadyExistsException ex) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(ex.getMessage());
    }

    /**
     * Gère les IllegalArgumentException et renvoie un 400 Bad Request.
     * Utile pour les validations métier (ex : montant ≤ 0).
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleBadRequest(IllegalArgumentException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ex.getMessage());
    }

    /**
     * Gère toutes les autres RuntimeException et renvoie un 500.
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> handleRuntime(RuntimeException ex) {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Erreur interne : " + ex.getMessage());
    }
}

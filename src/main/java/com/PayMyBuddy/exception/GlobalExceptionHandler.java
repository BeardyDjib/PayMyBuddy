package com.paymybuddy.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Gestionnaire global des exceptions pour les API REST.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Gère les ResourceNotFoundException et renvoie un 404.
     * @param ex L'exception interceptée.
     * @return Réponse HTTP avec statut 404.
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseBody
    public ResponseEntity<String> handleNotFound(ResourceNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ex.getMessage());
    }

    /**
     * Gère les autres RuntimeException et renvoie un 500.
     * @param ex L'exception interceptée.
     * @return Réponse HTTP avec statut 500.
     */
    @ExceptionHandler(RuntimeException.class)
    @ResponseBody
    public ResponseEntity<String> handleRuntime(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Erreur interne : " + ex.getMessage());
    }
}
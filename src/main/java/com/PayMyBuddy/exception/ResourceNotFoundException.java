package com.paymybuddy.exception;

/**
 * Exception lancée lorsqu'une ressource n'est pas trouvée.
 */
public class ResourceNotFoundException extends RuntimeException {

    /**
     * Constructeur.
     * @param message Message d'erreur détaillé.
     */
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
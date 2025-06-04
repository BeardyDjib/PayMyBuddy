package com.paymybuddy.exception;

/**
 * Exception levée lorsqu'un utilisateur tente de s'enregistrer avec un email déjà existant.
 */
public class UserAlreadyExistsException extends RuntimeException {

    /**
     * Constructeur.
     * @param message Message d'erreur détaillé.
     */
    public UserAlreadyExistsException(String message) {
        super(message);
    }
}

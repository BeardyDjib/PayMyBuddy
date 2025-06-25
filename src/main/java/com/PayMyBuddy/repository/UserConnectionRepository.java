package com.paymybuddy.repository;

import com.paymybuddy.model.UserConnection;
import com.paymybuddy.model.UserConnectionId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository JPA pour l'entité UserConnection.
 * Permet de gérer CRUD et requêtes personnalisées sur la table user_connection.
 */
@Repository
public interface UserConnectionRepository
        extends JpaRepository<UserConnection, UserConnectionId> {

    /**
     * Récupère toutes les connexions (amis) d'un utilisateur donné.
     * @param id Composite key partielle : userId = ID de l'utilisateur.
     *           (Seul userId est utilisé pour la requête car UserConnectionId contient userId et connectionId.)
     * @return Liste de UserConnection (les enregistrements où userId correspond).
     */
    List<UserConnection> findByIdUserId(Integer id);

    /**
     * Vérifie si une connexion existe entre deux utilisateurs (userId, connectionId).
     * @param userId ID de l’utilisateur initiateur.
     * @param connectionId ID de l’utilisateur destinataire.
     * @return true si l’enregistrement existe.
     */
    default boolean existsByUserIdAndConnectionId(Integer userId, Integer connectionId) {
        return existsById(new UserConnectionId(userId, connectionId));
    }

    /**
     * Supprime une connexion entre deux utilisateurs.
     * @param id Composite key complète (userId, connectionId).
     */
    void deleteById(UserConnectionId id);
}

package com.paymybuddy.repository;

import com.paymybuddy.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository JPA pour l'entité Transaction.
 * Permet de faire des opérations CRUD et des requêtes personnalisées.
 */
@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Integer> {

    /**
     * Recherche toutes les transactions effectuées par un expéditeur donné.
     * @param senderId Identifiant de l’expéditeur.
     * @return Liste de transactions où sender_id = senderId.
     */
    List<Transaction> findBySenderId(Integer senderId);

    /**
     * Recherche toutes les transactions reçues par un destinataire donné.
     * @param receiverId Identifiant du destinataire.
     * @return Liste de transactions où receiver_id = receiverId.
     */
    List<Transaction> findByReceiverId(Integer receiverId);
}

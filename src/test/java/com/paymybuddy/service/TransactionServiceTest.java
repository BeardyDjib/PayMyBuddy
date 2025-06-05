package com.paymybuddy.service;

import com.paymybuddy.dto.TransactionDto;
import com.paymybuddy.exception.ResourceNotFoundException;
import com.paymybuddy.model.Transaction;
import com.paymybuddy.repository.AppUserRepository;
import com.paymybuddy.repository.TransactionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires pour la classe TransactionService.
 * <p>
 * On utilise Mockito pour simuler le comportement du repository de transactions
 * et du repository d'utilisateurs (AppUserRepository).
 * </p>
 */
@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private AppUserRepository appUserRepository;

    @InjectMocks
    private TransactionService service;

    /**
     * Test du cas où l'expéditeur (sender) n'existe pas dans la base de données.
     * <p>
     * GIVEN   : un TransactionDto avec senderId = 5 (inexistant) et receiverId = 10.
     * WHEN    : on appelle service.createTransaction(dto).
     * THEN    : on s'attend à une ResourceNotFoundException dont le message contient "Expéditeur introuvable".
     * </p>
     */
    @Test
    void createTransaction_shouldThrowWhenSenderNotFound() {
        // GIVEN : un senderId inexistant (5L)
        TransactionDto dto = new TransactionDto(null, 5, 10, "Test", BigDecimal.valueOf(100));

        when(appUserRepository.findById(5L)).thenReturn(Optional.empty());

        // WHEN + THEN : ResourceNotFoundException pour l’expéditeur
        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class, () -> {
            service.createTransaction(dto);
        });
        assertTrue(ex.getMessage().contains("Expéditeur introuvable"));

        verify(appUserRepository).findById(5L);
        verifyNoMoreInteractions(appUserRepository);
        verifyNoInteractions(transactionRepository);
    }

    /**
     * Test du cas où le destinataire (receiver) n'existe pas dans la base de données.
     * <p>
     * GIVEN   : senderId = 5 (existe), receiverId = 7 (inexistant).
     * WHEN    : on appelle service.createTransaction(dto).
     * THEN    : on s'attend à une ResourceNotFoundException dont le message contient "Destinataire introuvable".
     * </p>
     */
    @Test
    void createTransaction_shouldThrowWhenReceiverNotFound() {
        // GIVEN : sender existe, receiver n’existe pas
        TransactionDto dto = new TransactionDto(null, 5, 7, "Test", BigDecimal.valueOf(100));

        when(appUserRepository.findById(5L)).thenReturn(Optional.of(mock(com.paymybuddy.model.AppUser.class)));
        when(appUserRepository.findById(7L)).thenReturn(Optional.empty());

        // WHEN + THEN : ResourceNotFoundException pour le destinataire
        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class, () -> {
            service.createTransaction(dto);
        });
        assertTrue(ex.getMessage().contains("Destinataire introuvable"));

        verify(appUserRepository).findById(5L);
        verify(appUserRepository).findById(7L);
        verifyNoMoreInteractions(appUserRepository);
        verifyNoInteractions(transactionRepository);
    }

    /**
     * Test du cas où le montant (amount) est inférieur ou égal à zéro.
     * <p>
     * GIVEN   : senderId et receiverId existent, mais amount = 0.
     * WHEN    : on appelle service.createTransaction(dto).
     * THEN    : on s’attend à IllegalArgumentException dont le message contient "Le montant doit être strictement supérieur à 0".
     * </p>
     */
    @Test
    void createTransaction_shouldThrowWhenAmountNotPositive() {
        // GIVEN : sender et receiver existent, mais amount = 0
        TransactionDto dto = new TransactionDto(null, 1, 2, "Test", BigDecimal.ZERO);

        when(appUserRepository.findById(1L)).thenReturn(Optional.of(mock(com.paymybuddy.model.AppUser.class)));
        when(appUserRepository.findById(2L)).thenReturn(Optional.of(mock(com.paymybuddy.model.AppUser.class)));

        // WHEN + THEN : IllegalArgumentException sur amount
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            service.createTransaction(dto);
        });
        assertTrue(ex.getMessage().contains("Le montant doit être strictement supérieur à 0"));

        verify(appUserRepository, times(1)).findById(1L);
        verify(appUserRepository, times(1)).findById(2L);
        verifyNoInteractions(transactionRepository);
    }

    /**
     * Test de la création d'une transaction valide.
     * <p>
     * GIVEN   : senderId et receiverId existent, amount positif.
     * WHEN    : on appelle service.createTransaction(dto).
     * THEN    : on obtient une Transaction sauvegardée, et on vérifie que les champs passés
     *           vers transactionRepository.save(...) sont corrects.
     * </p>
     */
    @Test
    void createTransaction_shouldSaveValidTransaction() {
        // GIVEN : sender et receiver existent et amount positif
        TransactionDto dto = new TransactionDto(null, 1, 2, "Paiement", BigDecimal.valueOf(200));

        when(appUserRepository.findById(1L)).thenReturn(Optional.of(mock(com.paymybuddy.model.AppUser.class)));
        when(appUserRepository.findById(2L)).thenReturn(Optional.of(mock(com.paymybuddy.model.AppUser.class)));

        // On prépare un objet Transaction qui sera renvoyé par le repository
        Transaction savedTransaction = new Transaction(1, 2, "Paiement", BigDecimal.valueOf(200), BigDecimal.valueOf(0.5));
        savedTransaction.setId(10);

        when(transactionRepository.save(any(Transaction.class))).thenReturn(savedTransaction);

        // WHEN : on appelle createTransaction
        Transaction result = service.createTransaction(dto);

        // THEN : la transaction retournée doit correspondre à savedTransaction
        assertNotNull(result);
        assertEquals(10, result.getId());
        assertEquals(1, result.getSenderId());
        assertEquals(2, result.getReceiverId());
        assertEquals("Paiement", result.getDescription());
        assertEquals(BigDecimal.valueOf(200), result.getAmount());
        assertEquals(BigDecimal.valueOf(0.5), result.getFeePercent());

        // Vérifier que save() a bien été appelé avec un objet Transaction correct
        ArgumentCaptor<Transaction> captor = ArgumentCaptor.forClass(Transaction.class);
        verify(transactionRepository).save(captor.capture());
        Transaction toSave = captor.getValue();
        assertEquals(1, toSave.getSenderId());
        assertEquals(2, toSave.getReceiverId());
        assertEquals("Paiement", toSave.getDescription());
        assertEquals(BigDecimal.valueOf(200), toSave.getAmount());
        assertEquals(BigDecimal.valueOf(0.5), toSave.getFeePercent());
    }

    /**
     * Test de getAllTransactions lorsque la base est vide.
     * <p>
     * GIVEN   : transactionRepository.findAll() retourne une liste vide.
     * WHEN    : on appelle service.getAllTransactions().
     * THEN    : on s’attend à obtenir une liste vide de DTO.
     * </p>
     */
    @Test
    void getAllTransactions_shouldReturnEmptyListWhenNoData() {
        // GIVEN : le repository retourne une liste vide
        when(transactionRepository.findAll()).thenReturn(Collections.emptyList());

        // WHEN : appel de la méthode
        List<TransactionDto> result = service.getAllTransactions();

        // THEN : la liste doit être vide
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(transactionRepository).findAll();
    }

    /**
     * Test de getTransactionsBySender lorsque l’expéditeur n’existe pas.
     * <p>
     * GIVEN   : appUserRepository.findById(5L) retourne Optional.empty().
     * WHEN    : on appelle service.getTransactionsBySender(5).
     * THEN    : on s’attend à ResourceNotFoundException dont le message mentionne "Expéditeur introuvable".
     * </p>
     */
    @Test
    void getTransactionsBySender_shouldThrowWhenSenderNotExist() {
        // GIVEN : sender 5L inexistant
        when(appUserRepository.findById(5L)).thenReturn(Optional.empty());

        // WHEN + THEN : exception levée
        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class, () -> {
            service.getTransactionsBySender(5);
        });
        assertTrue(ex.getMessage().contains("Expéditeur introuvable"));
        verify(appUserRepository).findById(5L);
        verifyNoMoreInteractions(transactionRepository);
    }

    /**
     * Test de getTransactionsBySender avec un expéditeur valide mais sans transaction associée.
     * <p>
     * GIVEN   : appUserRepository.findById(5L) retourne un utilisateur,
     *           transactionRepository.findBySenderId(5) retourne liste vide.
     * WHEN    : on appelle service.getTransactionsBySender(5).
     * THEN    : on obtient une liste vide de DTO.
     * </p>
     */
    @Test
    void getTransactionsBySender_shouldReturnEmptyListWhenNoTransactions() {
        // GIVEN : expéditeur 5L existe, mais pas de transaction pour lui
        when(appUserRepository.findById(5L)).thenReturn(Optional.of(mock(com.paymybuddy.model.AppUser.class)));
        when(transactionRepository.findBySenderId(5)).thenReturn(Collections.emptyList());

        // WHEN : appel de la méthode
        List<TransactionDto> result = service.getTransactionsBySender(5);

        // THEN : la liste doit être vide
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(appUserRepository).findById(5L);
        verify(transactionRepository).findBySenderId(5);
    }

    /**
     * Test de getTransactionsByReceiver lorsque le destinataire n’existe pas.
     * <p>
     * GIVEN   : appUserRepository.findById(7L) retourne Optional.empty().
     * WHEN    : on appelle service.getTransactionsByReceiver(7).
     * THEN    : on s’attend à ResourceNotFoundException dont le message mentionne "Destinataire introuvable".
     * </p>
     */
    @Test
    void getTransactionsByReceiver_shouldThrowWhenReceiverNotExist() {
        // GIVEN : destinataire 7L inexistant
        when(appUserRepository.findById(7L)).thenReturn(Optional.empty());

        // WHEN + THEN : exception levée
        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class, () -> {
            service.getTransactionsByReceiver(7);
        });
        assertTrue(ex.getMessage().contains("Destinataire introuvable"));
        verify(appUserRepository).findById(7L);
        verifyNoMoreInteractions(transactionRepository);
    }

    /**
     * Test de getTransactionsByReceiver avec un destinataire valide mais sans transaction associée.
     * <p>
     * GIVEN   : appUserRepository.findById(7L) retourne un utilisateur,
     *           transactionRepository.findByReceiverId(7) retourne liste vide.
     * WHEN    : on appelle service.getTransactionsByReceiver(7).
     * THEN    : on obtient une liste vide de DTO.
     * </p>
     */
    @Test
    void getTransactionsByReceiver_shouldReturnEmptyListWhenNoTransactions() {
        // GIVEN : destinataire 7L existe, mais pas de transaction pour lui
        when(appUserRepository.findById(7L)).thenReturn(Optional.of(mock(com.paymybuddy.model.AppUser.class)));
        when(transactionRepository.findByReceiverId(7)).thenReturn(Collections.emptyList());

        // WHEN : appel de la méthode
        List<TransactionDto> result = service.getTransactionsByReceiver(7);

        // THEN : la liste doit être vide
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(appUserRepository).findById(7L);
        verify(transactionRepository).findByReceiverId(7);
    }
}

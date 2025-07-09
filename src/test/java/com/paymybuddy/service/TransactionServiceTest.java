package com.paymybuddy.service;

import com.paymybuddy.dto.TransactionDto;
import com.paymybuddy.exception.ResourceNotFoundException;
import com.paymybuddy.model.AppUser;
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
     * GIVEN   : un TransactionDto avec senderId = 5 (inexistant) et receiverEmail = "r@e.com".
     * WHEN    : on appelle service.createTransaction(dto).
     * THEN    : on s'attend à une ResourceNotFoundException dont le message contient "Expéditeur introuvable".
     * </p>
     */
    @Test
    void createTransaction_shouldThrowWhenSenderNotFound() {
        TransactionDto dto = new TransactionDto();
        dto.setSenderId(5);
        dto.setReceiverEmail("r@e.com");
        dto.setDescription("Test");
        dto.setAmount(BigDecimal.valueOf(100));

        when(appUserRepository.findById(5L)).thenReturn(Optional.empty());

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
     * GIVEN   : senderId = 1 (existe), receiverEmail = "missing@e.com" (inexistant).
     * WHEN    : on appelle service.createTransaction(dto).
     * THEN    : on s'attend à une ResourceNotFoundException dont le message contient "Destinataire introuvable".
     * </p>
     */
    @Test
    void createTransaction_shouldThrowWhenReceiverNotFound() {
        TransactionDto dto = new TransactionDto();
        dto.setSenderId(1);
        dto.setReceiverEmail("missing@e.com");
        dto.setDescription("Test");
        dto.setAmount(BigDecimal.valueOf(100));

        when(appUserRepository.findById(1L)).thenReturn(Optional.of(mock(AppUser.class)));
        when(appUserRepository.findByEmail("missing@e.com")).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class, () -> {
            service.createTransaction(dto);
        });
        assertTrue(ex.getMessage().contains("Destinataire introuvable"));

        verify(appUserRepository).findById(1L);
        verify(appUserRepository).findByEmail("missing@e.com");
        verifyNoMoreInteractions(appUserRepository);
        verifyNoInteractions(transactionRepository);
    }

    /**
     * Test du cas où le montant (amount) est inférieur ou égal à zéro.
     * <p>
     * GIVEN   : sender et receiver existent, mais amount = 0.
     * WHEN    : on appelle service.createTransaction(dto).
     * THEN    : on s’attend à IllegalArgumentException dont le message contient "Le montant doit être strictement supérieur à 0".
     * </p>
     */
    @Test
    void createTransaction_shouldThrowWhenAmountNotPositive() {
        TransactionDto dto = new TransactionDto();
        dto.setSenderId(1);
        dto.setReceiverEmail("r@e.com");
        dto.setDescription("Test");
        dto.setAmount(BigDecimal.ZERO);

        when(appUserRepository.findById(1L)).thenReturn(Optional.of(mock(AppUser.class)));
        when(appUserRepository.findByEmail("r@e.com")).thenReturn(Optional.of(mock(AppUser.class)));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            service.createTransaction(dto);
        });
        assertTrue(ex.getMessage().contains("Le montant doit être strictement supérieur à 0"));

        verify(appUserRepository).findById(1L);
        verify(appUserRepository).findByEmail("r@e.com");
        verifyNoInteractions(transactionRepository);
    }

    /**
     * Test de la création d'une transaction valide.
     * <p>
     * GIVEN   : sender et receiver existent, amount positif.
     * WHEN    : on appelle service.createTransaction(dto).
     * THEN    : on obtient une Transaction sauvegardée, et on vérifie que les champs passés
     *           vers transactionRepository.save(...) sont corrects.
     * </p>
     */
    @Test
    void createTransaction_shouldSaveValidTransaction() {
        TransactionDto dto = new TransactionDto();
        dto.setSenderId(1);
        dto.setReceiverEmail("r@e.com");
        dto.setDescription("Paiement");
        dto.setAmount(BigDecimal.valueOf(200));

        AppUser mockSender = mock(AppUser.class);
        when(mockSender.getId()).thenReturn(1L);
        AppUser mockReceiver = mock(AppUser.class);
        when(mockReceiver.getId()).thenReturn(2L);

        when(appUserRepository.findById(1L)).thenReturn(Optional.of(mockSender));
        when(appUserRepository.findByEmail("r@e.com")).thenReturn(Optional.of(mockReceiver));

        // Préparer la Transaction renvoyée par le repository
        Transaction saved = new Transaction();
        saved.setId(10);
        saved.setSenderId(1);
        saved.setReceiverId(2);
        saved.setDescription("Paiement");
        saved.setAmount(BigDecimal.valueOf(200));
        saved.setFeePercent(BigDecimal.valueOf(0.5));

        when(transactionRepository.save(any(Transaction.class))).thenReturn(saved);

        Transaction result = service.createTransaction(dto);

        assertNotNull(result);
        assertEquals(10, result.getId());
        assertEquals(1, result.getSenderId());
        assertEquals(2, result.getReceiverId());
        assertEquals("Paiement", result.getDescription());
        assertEquals(BigDecimal.valueOf(200), result.getAmount());
        assertEquals(BigDecimal.valueOf(0.5), result.getFeePercent());

        // Vérifier l'objet passé à save()
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
     * Test de findAllDto lorsque la base est vide.
     * <p>
     * GIVEN   : transactionRepository.findAll() retourne une liste vide.
     * WHEN    : on appelle service.findAllDto().
     * THEN    : on s’attend à obtenir une liste vide de DTO.
     * </p>
     */
    @Test
    void findAllDto_shouldReturnEmptyListWhenNoData() {
        when(transactionRepository.findAll()).thenReturn(Collections.emptyList());

        List<TransactionDto> result = service.findAllDto();

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(transactionRepository).findAll();
    }

    /**
     * Test de findBySenderIdDto lorsque l’expéditeur n’existe pas.
     * <p>
     * GIVEN   : appUserRepository.findById(5L) retourne Optional.empty().
     * WHEN    : on appelle service.findBySenderIdDto(5).
     * THEN    : on s’attend à ResourceNotFoundException dont le message mentionne "Expéditeur introuvable".
     * </p>
     */
    @Test
    void findBySenderIdDto_shouldThrowWhenSenderNotExist() {
        when(appUserRepository.findById(5L)).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class, () -> {
            service.findBySenderIdDto(5);
        });
        assertTrue(ex.getMessage().contains("Expéditeur introuvable"));
        verify(appUserRepository).findById(5L);
        verifyNoMoreInteractions(transactionRepository);
    }

    /**
     * Test de findBySenderIdDto avec un expéditeur valide mais sans transaction associée.
     * <p>
     * GIVEN   : appUserRepository.findById(5L) retourne un utilisateur,
     *           transactionRepository.findBySenderId(5) retourne liste vide.
     * WHEN    : on appelle service.findBySenderIdDto(5).
     * THEN    : on obtient une liste vide de DTO.
     * </p>
     */
    @Test
    void findBySenderIdDto_shouldReturnEmptyListWhenNoTransactions() {
        AppUser mockUser = mock(AppUser.class);
        when(appUserRepository.findById(5L)).thenReturn(Optional.of(mockUser));
        when(transactionRepository.findBySenderId(5)).thenReturn(Collections.emptyList());

        List<TransactionDto> result = service.findBySenderIdDto(5);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(appUserRepository).findById(5L);
        verify(transactionRepository).findBySenderId(5);
    }
}

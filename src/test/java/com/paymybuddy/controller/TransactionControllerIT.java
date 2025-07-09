package com.paymybuddy.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.paymybuddy.dto.TransactionDto;
import com.paymybuddy.model.AppUser;
import com.paymybuddy.model.Transaction;
import com.paymybuddy.repository.AppUserRepository;
import com.paymybuddy.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests d'intégration pour le TransactionController.
 * <p>
 * Ces tests démarrent tout le contexte Spring (incluant la sécurité et la base de données),
 * puis utilisent MockMvc pour simuler des requêtes HTTP sur /api/transactions.
 * </p>
 */
@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser
class TransactionControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private AppUserRepository appUserRepository;

    @BeforeEach
    void setUp() {
        // On purge d'abord les transactions pour lever les FKs
        transactionRepository.deleteAll();
        // Puis on purge les utilisateurs
        appUserRepository.deleteAll();
    }

    /**
     * Créer une transaction valide doit retourner 201 et sauvegarder en base.
     * <p>
     * GIVEN  : deux utilisateurs existants en base (sender et receiver)
     *          et un TransactionDto avec senderId, receiverEmail, description et amount valides.<br>
     * WHEN   : on POST /api/transactions avec ce DTO sérialisé en JSON.<br>
     * THEN   : on reçoit HTTP 201 Created, un JSON contenant id, senderId, receiverId, description, amount,
     *          et la table 'transaction' contient exactement cet enregistrement.
     * </p>
     */
    @Test
    @DisplayName("Créer une transaction valide doit retourner 201 et sauvegarder en base")
    void testCreateTransaction_ShouldReturn201AndSave() throws Exception {
        // ==== GIVEN ====
        AppUser sender = new AppUser();
        sender.setUsername("userSender");
        sender.setEmail("sender@mail.com");
        sender.setPassword("$2a$10$hash");
        sender = appUserRepository.save(sender);

        AppUser receiver = new AppUser();
        receiver.setUsername("userReceiver");
        receiver.setEmail("receiver@mail.com");
        receiver.setPassword("$2a$10$hash2");
        receiver = appUserRepository.save(receiver);

        TransactionDto dto = new TransactionDto();
        dto.setSenderId(sender.getId().intValue());
        dto.setReceiverEmail(receiver.getEmail());
        dto.setDescription("Cadeau d'anniversaire");
        dto.setAmount(BigDecimal.valueOf(150.75));

        String jsonBody = objectMapper.writeValueAsString(dto);

        // ==== WHEN ====
        mockMvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                // ==== THEN ====
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.senderId").value(sender.getId().intValue()))
                .andExpect(jsonPath("$.receiverId").value(receiver.getId().intValue()))
                .andExpect(jsonPath("$.description").value("Cadeau d'anniversaire"))
                .andExpect(jsonPath("$.amount").value(150.75));

        // ==== THEN bis ====
        assertThat(transactionRepository.count()).isEqualTo(1);

        Transaction saved = transactionRepository.findAll().get(0);
        assertThat(saved.getSenderId()).isEqualTo(sender.getId().intValue());
        assertThat(saved.getReceiverId()).isEqualTo(receiver.getId().intValue());
        assertThat(saved.getDescription()).isEqualTo("Cadeau d'anniversaire");
        assertThat(saved.getAmount()).isEqualByComparingTo(BigDecimal.valueOf(150.75));
    }

    /**
     * Lister toutes les transactions doit retourner 200 et la liste des DTO.
     * <p>
     * GIVEN  : deux transactions préenregistrées en base<br>
     * WHEN   : on GET /api/transactions<br>
     * THEN   : on reçoit HTTP 200 OK et un tableau JSON de taille 2 contenant les DTO correspondants.
     * </p>
     */
    @Test
    @DisplayName("Lister toutes les transactions doit retourner 200 et la liste des DTO")
    void testGetAllTransactions_ShouldReturn200AndList() throws Exception {
        // ==== GIVEN ====
        AppUser user1 = new AppUser();
        user1.setUsername("alpha");
        user1.setEmail("alpha@mail.com");
        user1.setPassword("hash");
        user1 = appUserRepository.save(user1);

        AppUser user2 = new AppUser();
        user2.setUsername("beta");
        user2.setEmail("beta@mail.com");
        user2.setPassword("hash2");
        user2 = appUserRepository.save(user2);

        Transaction t1 = new Transaction(
                user1.getId().intValue(),
                user2.getId().intValue(),
                "Paiement A→B",
                BigDecimal.valueOf(50.00),
                BigDecimal.valueOf(0.5)
        );
        Transaction t2 = new Transaction(
                user2.getId().intValue(),
                user1.getId().intValue(),
                "Remboursement B→A",
                BigDecimal.valueOf(25.25),
                BigDecimal.valueOf(0.5)
        );
        transactionRepository.save(t1);
        transactionRepository.save(t2);

        // ==== WHEN ====
        mockMvc.perform(get("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON))
                // ==== THEN ====
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].senderId").value(user1.getId().intValue()))
                .andExpect(jsonPath("$[1].amount").value(25.25));
    }

    /**
     * Créer transaction avec sender inexistant doit retourner 404.
     * <p>
     * GIVEN  : un TransactionDto avec senderId inexistant (99) et receiverEmail valide<br>
     * WHEN   : on POST /api/transactions avec ce DTO<br>
     * THEN   : on reçoit HTTP 404 Not Found et un message contenant "Expéditeur introuvable".
     * </p>
     */
    @Test
    @DisplayName("Créer transaction avec sender inexistant doit retourner 404")
    void testCreateTransaction_SenderNotFound_ShouldReturn404() throws Exception {
        // ==== GIVEN ====
        TransactionDto dto = new TransactionDto();
        dto.setSenderId(99); // inexistant
        dto.setReceiverEmail("receiver@notfound.com");
        dto.setDescription("Erreur");
        dto.setAmount(BigDecimal.valueOf(10));
        String json = objectMapper.writeValueAsString(dto);

        // ==== WHEN + THEN ====
        mockMvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isNotFound())
                .andExpect(content().string(containsString("Expéditeur introuvable")));
    }

    /**
     * Créer transaction avec receiver inexistant doit retourner 404.
     * <p>
     * GIVEN  : un utilisateur sender valide en base et un DTO avec receiverEmail inexistant<br>
     * WHEN   : on POST /api/transactions avec ce DTO<br>
     * THEN   : on reçoit HTTP 404 Not Found et un message contenant "Destinataire introuvable".
     * </p>
     */
    @Test
    @DisplayName("Créer transaction avec receiver inexistant doit retourner 404")
    void testCreateTransaction_ReceiverNotFound_ShouldReturn404() throws Exception {
        // ==== GIVEN ====
        AppUser valid = new AppUser();
        valid.setUsername("valid");
        valid.setEmail("valid@mail.com");
        valid.setPassword("hash");
        valid = appUserRepository.save(valid);

        TransactionDto dto = new TransactionDto();
        dto.setSenderId(valid.getId().intValue());
        dto.setReceiverEmail("inexistant@mail.com");
        dto.setDescription("Erreur reç.");
        dto.setAmount(BigDecimal.valueOf(10));
        String json = objectMapper.writeValueAsString(dto);

        // ==== WHEN + THEN ====
        mockMvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isNotFound())
                .andExpect(content().string(containsString("Destinataire introuvable")));
    }

    /**
     * Créer transaction avec montant non positif doit retourner 400.
     * <p>
     * GIVEN  : deux utilisateurs existants et un DTO avec amount = 0<br>
     * WHEN   : on POST /api/transactions avec ce DTO<br>
     * THEN   : on reçoit HTTP 400 Bad Request et un message contenant
     *          "Le montant doit être strictement supérieur à 0".
     * </p>
     */
    @Test
    @DisplayName("Créer transaction avec montant non positif doit retourner 400")
    void testCreateTransaction_AmountNotPositive_ShouldReturnBadRequest() throws Exception {
        // ==== GIVEN ====
        AppUser sender = new AppUser();
        sender.setUsername("senderX");
        sender.setEmail("senderX@mail.com");
        sender.setPassword("hash");
        sender = appUserRepository.save(sender);

        AppUser receiver = new AppUser();
        receiver.setUsername("receiverX");
        receiver.setEmail("receiverX@mail.com");
        receiver.setPassword("hash2");
        receiver = appUserRepository.save(receiver);

        TransactionDto dto = new TransactionDto();
        dto.setSenderId(sender.getId().intValue());
        dto.setReceiverEmail(receiver.getEmail());
        dto.setDescription("Zéro montant");
        dto.setAmount(BigDecimal.ZERO);
        String json = objectMapper.writeValueAsString(dto);

        // ==== WHEN + THEN ====
        mockMvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Le montant doit être strictement supérieur à 0")));
    }
}

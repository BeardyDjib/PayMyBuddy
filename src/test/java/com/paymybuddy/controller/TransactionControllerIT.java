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
@WithMockUser                     // <<-- Simule un utilisateur connecté pour toutes les requêtes
class TransactionControllerIT {

    @Autowired
    private MockMvc mockMvc; // Pour envoyer des requêtes HTTP factices

    @Autowired
    private ObjectMapper objectMapper; // Pour sérialiser/désérialiser JSON

    @Autowired
    private TransactionRepository transactionRepository; // Accès direct à la table transaction

    @Autowired
    private AppUserRepository appUserRepository; // Accès direct à la table app_user

    /**
     * Avant chaque test, on vide les tables app_user et transaction
     * pour repartir d’une base propre.
     */
    @BeforeEach
    void setUp() {
        // Supprimer d’abord toutes les transactions (FK)
        transactionRepository.deleteAll();
        // Puis supprimer tous les utilisateurs
        appUserRepository.deleteAll();
    }

    /**
     * Test de l'endpoint POST /api/transactions pour créer une transaction valide.
     * <p>
     * GIVEN : deux utilisateurs en base (sender et receiver).
     * WHEN  : on envoie une requête POST avec JSON TransactionDto.
     * THEN  : on reçoit HTTP 201 Created, le corps contient l'objet Transaction avec un id,
     *         et la table 'transaction' contient bien ce nouvel enregistrement.
     * </p>
     */
    @Test
    @DisplayName("Créer une transaction valide doit retourner 201 et sauvegarder en base")
    void testCreateTransaction_ShouldReturn201AndSave() throws Exception {
        // ==== GIVEN ====
        // 1. Créer deux utilisateurs en base pour sender et receiver
        AppUser sender = new AppUser();
        sender.setUsername("userSender");
        sender.setEmail("sender@mail.com");
        sender.setPassword("$2a$10$hash"); // mot de passe factice
        sender = appUserRepository.save(sender);

        AppUser receiver = new AppUser();
        receiver.setUsername("userReceiver");
        receiver.setEmail("receiver@mail.com");
        receiver.setPassword("$2a$10$hash2");
        receiver = appUserRepository.save(receiver);

        // 2. Préparer le DTO JSON pour la transaction
        TransactionDto dto = new TransactionDto();
        dto.setSenderId(sender.getId().intValue());
        dto.setReceiverId(receiver.getId().intValue());
        dto.setDescription("Cadeau d'anniversaire");
        dto.setAmount(BigDecimal.valueOf(150.75));

        String jsonBody = objectMapper.writeValueAsString(dto);

        // ==== WHEN ====
        mockMvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                // ==== THEN (vérifications) ====
                .andExpect(status().isCreated()) // HTTP 201
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                // Le JSON retourné doit contenir "id"
                .andExpect(jsonPath("$.id").exists())
                // Vérifier senderId
                .andExpect(jsonPath("$.senderId").value(sender.getId().intValue()))
                // Vérifier receiverId
                .andExpect(jsonPath("$.receiverId").value(receiver.getId().intValue()))
                // Vérifier amount
                .andExpect(jsonPath("$.amount").value(150.75))
                // Vérifier description
                .andExpect(jsonPath("$.description").value("Cadeau d'anniversaire"));

        // ==== THEN bis : vérifier en DB ====
        assertThat(transactionRepository.count()).isEqualTo(1);

        Transaction saved = transactionRepository.findAll().get(0);
        assertThat(saved.getSenderId()).isEqualTo(sender.getId().intValue());
        assertThat(saved.getReceiverId()).isEqualTo(receiver.getId().intValue());
        assertThat(saved.getDescription()).isEqualTo("Cadeau d'anniversaire");
        assertThat(saved.getAmount()).isEqualByComparingTo(BigDecimal.valueOf(150.75));
    }

    /**
     * Test de l'endpoint GET /api/transactions pour lister toutes les transactions.
     * <p>
     * GIVEN : on a préparé deux transactions en base.
     * WHEN  : on envoie une requête GET sur /api/transactions.
     * THEN  : on reçoit HTTP 200 OK et un tableau JSON avec ces transactions (DTO).
     * </p>
     */
    @Test
    @DisplayName("Lister toutes les transactions doit retourner 200 et la liste des DTO")
    void testGetAllTransactions_ShouldReturn200AndList() throws Exception {
        // ==== GIVEN ====
        // Créer un sender et un receiver
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

        // Créer deux transactions en base
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
                .andExpect(status().isOk()) // HTTP 200
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                // Réponse : un tableau JSON de taille 2
                .andExpect(jsonPath("$", hasSize(2)))
                // Vérifier le premier élément (senderId)
                .andExpect(jsonPath("$[0].senderId").value(user1.getId().intValue()))
                // Vérifier le second élément (amount)
                .andExpect(jsonPath("$[1].amount").value(25.25));
    }

    /**
     * Test d’erreur : création de transaction avec sender inexistant.
     * <p>
     * GIVEN : aucun utilisateur avec ID = 99.
     * WHEN  : appel POST /api/transactions avec ce senderId.
     * THEN  : on s'attend à HTTP 404 NOT FOUND + message "Expéditeur introuvable".
     * </p>
     */
    @Test
    @DisplayName("Créer transaction avec sender inexistant doit retourner 404")
    void testCreateTransaction_SenderNotFound_ShouldReturn404() throws Exception {
        // ==== GIVEN ====
        TransactionDto dto = new TransactionDto();
        dto.setSenderId(99);
        dto.setReceiverId(100);  // pas testé ici
        dto.setDescription("Erreur");
        dto.setAmount(BigDecimal.valueOf(10));

        String json = objectMapper.writeValueAsString(dto);

        // ==== WHEN ====
        mockMvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                // ==== THEN ====
                .andExpect(status().isNotFound())
                .andExpect(content().string(containsString("Expéditeur introuvable")));
    }

    /**
     * Test d’erreur : création de transaction avec receiver inexistant.
     * <p>
     * GIVEN : sender valide, receiver ID = 50 inexistant.
     * WHEN  : appel POST /api/transactions avec ce receiverId.
     * THEN  : on s'attend à HTTP 404 NOT FOUND + message "Destinataire introuvable".
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
        dto.setReceiverId(50);  // inexistant
        dto.setDescription("Erreur reç.");
        dto.setAmount(BigDecimal.valueOf(10));

        String json = objectMapper.writeValueAsString(dto);

        // ==== WHEN ====
        mockMvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                // ==== THEN ====
                .andExpect(status().isNotFound())
                .andExpect(content().string(containsString("Destinataire introuvable")));
    }

    /**
     * Test d’erreur : création de transaction avec montant non positif.
     * <p>
     * GIVEN : sender et receiver valides, amount = 0.
     * WHEN  : appel POST /api/transactions.
     * THEN  : on s'attend à HTTP 400 BAD REQUEST + message "Le montant doit être strictement supérieur à 0".
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
        dto.setReceiverId(receiver.getId().intValue());
        dto.setDescription("Zéro montant");
        dto.setAmount(BigDecimal.ZERO);

        String json = objectMapper.writeValueAsString(dto);

        // ==== WHEN ====
        mockMvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                // ==== THEN ====
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Le montant doit être strictement supérieur à 0")));
    }
}

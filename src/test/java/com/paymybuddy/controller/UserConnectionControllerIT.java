package com.paymybuddy.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.paymybuddy.dto.UserConnectionDto;
import com.paymybuddy.model.AppUser;
import com.paymybuddy.model.UserConnection;
import com.paymybuddy.repository.AppUserRepository;
import com.paymybuddy.repository.UserConnectionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests d'intégration pour le UserConnectionController.
 * <p>
 * Ces tests démarrent tout le contexte Spring (sécurité + base PostgreSQL) et utilisent MockMvc
 * pour simuler de vraies requêtes HTTP sur /api/connections.
 * </p>
 */
@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser   // Simule un utilisateur authentifié pour tous les tests
class UserConnectionControllerIT {

    @Autowired
    private MockMvc mockMvc; // pour envoyer des requêtes HTTP factices

    @Autowired
    private ObjectMapper objectMapper; // pour sérialiser/désérialiser JSON

    @Autowired
    private AppUserRepository appUserRepository; // accès direct à la table app_user

    @Autowired
    private UserConnectionRepository userConnectionRepository; // accès direct à la table user_connection

    /**
     * Avant chaque test, on vide les tables user_connection et app_user
     * afin de repartir d'une base propre.
     */
    @BeforeEach
    void setUp() {
        // d'abord delete toutes les connexions
        userConnectionRepository.deleteAll();
        // puis delete tous les utilisateurs
        appUserRepository.deleteAll();
    }

    /**
     * Test de l’endpoint PUT /api/connections pour ajouter une connexion valide.
     * <p>
     * GIVEN : deux utilisateurs en base (user et friend).
     * WHEN  : on envoie PUT /api/connections avec JSON { userId, connectionId }.
     * THEN  : on s’attend à HTTP 201 Created et à ce que la relation soit présente en base.
     * </p>
     */
    @Test
    @DisplayName("Ajouter une connexion valide doit retourner 201 et enregistrer en base")
    void testAddConnection_ShouldReturn201AndSave() throws Exception {
        // ==== GIVEN ====
        // 1. Créer deux utilisateurs en base
        AppUser user = new AppUser();
        user.setUsername("alice");
        user.setEmail("alice@mail.com");
        user.setPassword("$2a$10$hash");
        user = appUserRepository.save(user);

        AppUser friend = new AppUser();
        friend.setUsername("bob");
        friend.setEmail("bob@mail.com");
        friend.setPassword("$2a$10$hash2");
        friend = appUserRepository.save(friend);

        // 2. Préparer le DTO JSON pour la connexion
        UserConnectionDto dto = new UserConnectionDto();
        dto.setUserId(user.getId().intValue());
        dto.setConnectionId(friend.getId().intValue());

        String jsonBody = objectMapper.writeValueAsString(dto);

        // ==== WHEN ====
        mockMvc.perform(put("/api/connections")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                // ==== THEN ====
                .andExpect(status().isCreated()) // 201
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                // Le JSON retourné doit contenir userId et connectionId
                .andExpect(jsonPath("$.userId").value(user.getId().intValue()))
                .andExpect(jsonPath("$.connectionId").value(friend.getId().intValue()));

        // ==== THEN bis : vérifier en base ====
        assertThat(userConnectionRepository.count()).isEqualTo(1);
        // on peut récupérer l’enregistrement et vérifier les IDs
        var saved = userConnectionRepository.findAll().get(0);
        assertThat(saved.getUserId()).isEqualTo(user.getId().intValue());
        assertThat(saved.getConnectionId()).isEqualTo(friend.getId().intValue());
    }

    /**
     * Test de l’endpoint GET /api/connections/{userId} pour lister les connexions.
     * <p>
     * GIVEN : on a ajouté deux connexions pour un même user (user → friend1, user → friend2).
     * WHEN  : on envoie GET /api/connections/{userId}.
     * THEN  : on s’attend à HTTP 200 OK et à un tableau JSON de taille 2 contenant connectionId = friend1 et friend2.
     * </p>
     */
    @Test
    @DisplayName("Lister les connexions doit retourner 200 et la liste des connectionIds")
    void testListConnections_ShouldReturn200AndList() throws Exception {
        // ==== GIVEN ====
        // Créer un user et deux amis
        AppUser user = new AppUser();
        user.setUsername("charlie");
        user.setEmail("charlie@mail.com");
        user.setPassword("hash");
        user = appUserRepository.save(user);

        AppUser friend1 = new AppUser();
        friend1.setUsername("david");
        friend1.setEmail("david@mail.com");
        friend1.setPassword("hash2");
        friend1 = appUserRepository.save(friend1);

        AppUser friend2 = new AppUser();
        friend2.setUsername("eve");
        friend2.setEmail("eve@mail.com");
        friend2.setPassword("hash3");
        friend2 = appUserRepository.save(friend2);

        // Ajouter deux connexions en base manuellement
        userConnectionRepository.save(new UserConnection(user.getId().intValue(), friend1.getId().intValue()));
        userConnectionRepository.save(new UserConnection(user.getId().intValue(), friend2.getId().intValue()));

        // ==== WHEN ====
        mockMvc.perform(get("/api/connections/" + user.getId().intValue())
                        .contentType(MediaType.APPLICATION_JSON))
                // ==== THEN ====
                .andExpect(status().isOk()) // 200
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                // Le JSON doit être un tableau de taille 2
                .andExpect(jsonPath("$", hasSize(2)))
                // Les connectionIds doivent contenir friend1 et friend2 (ordre non garanti)
                .andExpect(jsonPath("$[*].connectionId", containsInAnyOrder(
                        friend1.getId().intValue(),
                        friend2.getId().intValue()
                )));
    }

    /**
     * Test d’erreur : GET /api/connections/{userId} avec userId inexistant doit retourner 404.
     * <p>
     * GIVEN : aucun utilisateur avec ID = 50.
     * WHEN  : on envoie GET /api/connections/50.
     * THEN  : on s’attend à HTTP 404 NOT FOUND.
     * </p>
     */
    @Test
    @DisplayName("Lister connexions pour un user inexistant doit retourner 404")
    void testListConnections_UserNotFound_ShouldReturn404() throws Exception {
        // ==== GIVEN ====
        // on n’insère aucun utilisateur

        // ==== WHEN ====
        mockMvc.perform(get("/api/connections/50")
                        .contentType(MediaType.APPLICATION_JSON))
                // ==== THEN ====
                .andExpect(status().isNotFound())
                .andExpect(content().string(containsString("Utilisateur introuvable")));
    }

    /**
     * Test d’erreur : PUT /api/connections avec userId inexistant doit retourner 404.
     * <p>
     * GIVEN : aucun utilisateur avec ID = 60.
     * WHEN  : on envoie PUT /api/connections avec JSON { userId:60, connectionId:61 }.
     * THEN  : on s’attend à HTTP 404 NOT FOUND.
     * </p>
     */
    @Test
    @DisplayName("Ajouter connection pour user inexistant doit retourner 404")
    void testAddConnection_UserNotFound_ShouldReturn404() throws Exception {
        // ==== GIVEN ====
        UserConnectionDto dto = new UserConnectionDto(60, 61);
        String json = objectMapper.writeValueAsString(dto);

        // ==== WHEN ====
        mockMvc.perform(put("/api/connections")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                // ==== THEN ====
                .andExpect(status().isNotFound())
                .andExpect(content().string(containsString("Utilisateur introuvable")));
    }

    /**
     * Test d’erreur : DELETE /api/connections avec relation inexistante doit retourner 404.
     * <p>
     * GIVEN : user et friend existent, mais aucune relation en base.
     * WHEN  : on envoie DELETE /api/connections avec JSON { userId, connectionId }.
     * THEN  : on s’attend à HTTP 404 NOT FOUND.
     * </p>
     */
    @Test
    @DisplayName("Supprimer connexion inexistante doit retourner 404")
    void testRemoveConnection_RelationNotFound_ShouldReturn404() throws Exception {
        // ==== GIVEN ====
        AppUser user = new AppUser();
        user.setUsername("foo");
        user.setEmail("foo@mail.com");
        user.setPassword("hash");
        user = appUserRepository.save(user);

        AppUser friend = new AppUser();
        friend.setUsername("bar");
        friend.setEmail("bar@mail.com");
        friend.setPassword("hash2");
        friend = appUserRepository.save(friend);

        // Aucune relation dans user_connection

        UserConnectionDto dto = new UserConnectionDto(user.getId().intValue(), friend.getId().intValue());
        String json = objectMapper.writeValueAsString(dto);

        // ==== WHEN ====
        mockMvc.perform(delete("/api/connections")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                // ==== THEN ====
                .andExpect(status().isNotFound())
                .andExpect(content().string(containsString("Connexion inexistante")));
    }
}

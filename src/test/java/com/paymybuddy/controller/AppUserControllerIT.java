package com.paymybuddy.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.paymybuddy.model.AppUser;
import com.paymybuddy.repository.AppUserRepository;
import com.paymybuddy.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests d'intégration de la classe AppUserController.
 * On vérifie ici le bon fonctionnement de l'enregistrement (POST /api/users/register)
 * et de la récupération des utilisateurs (GET /api/users),
 * en simulant de vraies requêtes HTTP via MockMvc.
 */
@SpringBootTest
@AutoConfigureMockMvc
public class AppUserControllerIT {

    @Autowired
    private MockMvc mockMvc; // Permet d'exécuter des requêtes HTTP fictives

    @Autowired
    private ObjectMapper objectMapper; // Convertit les objets Java <-> JSON

    @Autowired
    private TransactionRepository txRepository; // Pour vider les transactions en amont

    @Autowired
    private AppUserRepository userRepository;

    /**
     * Avant chaque test, on vide d'abord la table transaction (FK),
     * puis la table app_user pour être sûr de partir d’un état propre.
     */
    @BeforeEach
    public void setUp() {
        txRepository.deleteAll();
        userRepository.deleteAll();
    }

    /**
     * Test d'enregistrement d'un nouvel utilisateur.
     *
     * Objectif :
     * Vérifier que l'endpoint POST /api/users/register fonctionne correctement
     * lorsqu’un utilisateur valide est soumis.
     *
     * Étapes (Given / When / Then) :
     * - Given : un utilisateur JSON avec un email unique, username et mot de passe.
     * - When : on appelle l'endpoint d'enregistrement avec ce JSON.
     * - Then :
     *      - Le code HTTP retourné est 201 Created.
     *      - Le JSON de réponse contient un ID et l’email.
     *      - L'utilisateur est bien enregistré en base.
     */
    @Test
    public void testRegisterUser_ShouldReturn201() throws Exception {
        // GIVEN : un utilisateur JSON prêt à être envoyé
        AppUser user = new AppUser();
        user.setUsername("jane");
        user.setEmail("jane@mail.com");
        user.setPassword("password123");

        // WHEN : on fait une requête POST vers /api/users/register
        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                // THEN : on attend un code HTTP 201 et un corps JSON avec les bons champs
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.email").value("jane@mail.com"));

        // THEN bis : on vérifie aussi que l'utilisateur a bien été sauvegardé en base
        assertThat(userRepository.findByEmail("jane@mail.com")).isPresent();
    }

    /**
     * Test de récupération de tous les utilisateurs enregistrés.
     * Objectif : s'assurer que l'endpoint GET /api/users retourne bien les utilisateurs enregistrés.
     *
     * Étapes (Given / When / Then) :
     * - Given : Un utilisateur déjà enregistré dans la base de données.
     * - When : On appelle GET /api/users en tant qu’utilisateur connecté.
     * - Then : On obtient un code 200 et une liste contenant cet utilisateur,
     *          sans exposer le mot de passe.
     */
    @Test
    @WithMockUser(username = "admin@mail.com", roles = "USER") // Simule un utilisateur connecté
    public void testGetUsers_ShouldReturn200() throws Exception {
        // GIVEN : un utilisateur déjà en base
        AppUser user = new AppUser();
        user.setUsername("admin");
        user.setEmail("admin@mail.com");
        user.setPassword("$2a$10$motdepassehache"); // haché à la main
        userRepository.save(user);

        // WHEN : on fait une requête GET vers /api/users
        mockMvc.perform(get("/api/users"))
                // THEN : code 200 et JSON avec le champ email au bon endroit
                .andExpect(status().isOk())
                // index 0 (le premier élément de la liste) doit contenir l'email "admin@mail.com"
                .andExpect(jsonPath("$[0].email").value("admin@mail.com"))
                // On vérifie aussi qu’il n’y a **pas** de champ password dans le JSON
                .andExpect(jsonPath("$[0].password").doesNotExist());
    }

    /**
     * Test d'échec d’enregistrement en cas d’email déjà existant.
     *
     * Objectif :
     * Vérifier que l'endpoint POST /api/users/register retourne un code 409 Conflict
     * lorsque l’email soumis est déjà enregistré.
     *
     * Étapes (Given / When / Then) :
     * - Given : un utilisateur est déjà enregistré avec un email.
     * - When : on tente d’enregistrer un nouvel utilisateur avec le même email.
     * - Then :
     *      - Le code HTTP retourné est 409 Conflict.
     *      - Le corps de la réponse contient un message d’erreur explicite.
     */
    @Test
    public void givenExistingEmail_whenRegister_thenReturnsConflict() throws Exception {
        // GIVEN : un utilisateur est déjà en base
        AppUser existingUser = new AppUser();
        existingUser.setUsername("john");
        existingUser.setEmail("john@mail.com");
        existingUser.setPassword("azerty123");
        userRepository.save(existingUser);

        // GIVEN suite : tentative d’un 2ème utilisateur avec le même email
        AppUser conflictUser = new AppUser();
        conflictUser.setUsername("johnny");
        conflictUser.setEmail("john@mail.com"); // même email que ci-dessus
        conflictUser.setPassword("password2");
        String json = objectMapper.writeValueAsString(conflictUser);

        // WHEN : on tente de créer ce nouvel utilisateur
        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                // THEN : on s’attend à un 409 Conflict et à un message d’erreur explicite
                .andExpect(status().isConflict())
                .andExpect(content().string(containsString("L'email est déjà utilisé")));
    }
}

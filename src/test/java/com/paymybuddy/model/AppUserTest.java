package com.paymybuddy.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests unitaires de la classe AppUser.
 */
class AppUserTest {

    /**
     * Vérifie les getters, setters, equals, hashCode et toString.
     */
    @Test
    @DisplayName("Test complet de la classe AppUser (getters/setters/toString/equals/hashCode)")
    void testAppUserMethods() {
        // GIVEN - un objet AppUser
        AppUser user1 = new AppUser();
        user1.setId(1L);
        user1.setUsername("testUser");
        user1.setEmail("test@example.com");
        user1.setPassword("securePassword");

        AppUser user2 = new AppUser();
        user2.setId(1L);
        user2.setUsername("testUser");
        user2.setEmail("test@example.com");
        user2.setPassword("securePassword");

        // WHEN - on accède aux getters et on compare les objets
        Long id = user1.getId();
        String username = user1.getUsername();
        String email = user1.getEmail();
        String password = user1.getPassword();

        // THEN - on vérifie le contenu et les méthodes générées
        assertThat(id).isEqualTo(1L);
        assertThat(username).isEqualTo("testUser");
        assertThat(email).isEqualTo("test@example.com");
        assertThat(password).isEqualTo("securePassword");

        assertThat(user1).isEqualTo(user2); // equals
        assertThat(user1.hashCode()).isEqualTo(user2.hashCode()); // hashCode
        assertThat(user1.toString()).contains("testUser", "test@example.com"); // toString
    }
}

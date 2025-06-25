package com.paymybuddy.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires pour la classe UserConnection (entité de liaison).
 * <p>
 * On couvre :
 * <ul>
 *   <li>Constructeur avec (userId, connectionId) et getters</li>
 *   <li>Constructeur par défaut</li>
 *   <li>equals(Object) (basé sur EmbeddedId)</li>
 *   <li>hashCode()</li>
 *   <li>toString()</li>
 * </ul>
 * </p>
 */
class UserConnectionTest {

    /**
     * Test du constructeur UserConnection(Integer, Integer) et des getters correspondants.
     * <p>
     * GIVEN : on fournit userId et connectionId.
     * WHEN  : on instancie.
     * THEN  : getUserId() et getConnectionId() doivent renvoyer les valeurs fournies.
     * </p>
     */
    @Test
    @DisplayName("Constructeur complet et getters doivent renvoyer les bons IDs")
    void testFullConstructorAndGetters() {
        // GIVEN : des IDs d'exemple
        Integer userId = 7;
        Integer connectionId = 13;

        // WHEN : on crée l’objet via le constructeur
        UserConnection uc = new UserConnection(userId, connectionId);

        // THEN : getUserId() et getConnectionId() renvoient les bonnes valeurs
        assertEquals(userId, uc.getUserId(), "getUserId() doit renvoyer le userId passé.");
        assertEquals(connectionId, uc.getConnectionId(), "getConnectionId() doit renvoyer le connectionId passé.");
    }

    /**
     * Test du constructeur par défaut et du setter pour l’EmbeddedId.
     * <p>
     * GIVEN : on appelle new UserConnection().
     * WHEN  : on crée un UserConnectionId et on l’affecte via setId(...).
     * THEN  : getId() doit renvoyer l’EmbeddedId, et getUserId()/getConnectionId() les bonnes valeurs.
     * </p>
     */
    @Test
    @DisplayName("Constructeur vide et setter EmbeddedId doivent fonctionner")
    void testNoArgConstructorAndSetId() {
        // GIVEN : une instance via constructeur vide
        UserConnection uc = new UserConnection();

        // WHEN : on crée un EmbeddedId et on l’affecte
        UserConnectionId id = new UserConnectionId(21, 34);
        uc.setId(id);

        // THEN : getId() doit renvoyer cet id, et getUserId()/getConnectionId() idem
        assertSame(id, uc.getId(), "getId() doit renvoyer la même instance d'EmbeddedId.");
        assertEquals(21, uc.getUserId(), "getUserId() doit correspondre à id.getUserId().");
        assertEquals(34, uc.getConnectionId(), "getConnectionId() doit correspondre à id.getConnectionId().");
    }

    /**
     * Test des méthodes equals() et hashCode().
     * <p>
     * Scénarios :
     * <ul>
     *   <li>Deux objets avec même EmbeddedId → égaux, même hashCode.</li>
     *   <li>Deux objets avec id différents → non égaux, hashCode différent.</li>
     *   <li>Comparaison à null ou autre type → false pour equals.</li>
     * </ul>
     * </p>
     */
    @Test
    @DisplayName("equals() et hashCode() doivent se baser sur la clé composite EmbeddedId")
    void testEqualsAndHashCode() {
        // GIVEN : deux objets avec même id composite
        UserConnectionId id1 = new UserConnectionId(5, 10);
        UserConnection uc1 = new UserConnection();
        uc1.setId(id1);
        UserConnection uc2 = new UserConnection();
        uc2.setId(new UserConnectionId(5, 10)); // nouvelle instance mais même valeurs

        // WHEN / THEN : uc1.equals(uc2) doit être true, hashCodes identiques
        assertTrue(uc1.equals(uc2), "Deux UserConnection avec même EmbeddedId doivent être égaux.");
        assertEquals(uc1.hashCode(), uc2.hashCode(),
                "Pour deux objets égaux, hashCode doit être identique.");

        // GIVEN : deux EmbeddedId différents
        UserConnection uc3 = new UserConnection();
        uc3.setId(new UserConnectionId(5, 11));
        UserConnection uc4 = new UserConnection();
        uc4.setId(new UserConnectionId(6, 10));

        // THEN : uc3 ne doit pas être égal à uc4
        assertFalse(uc3.equals(uc4), "Deux UserConnection avec EmbeddedId différents ne doivent pas être égaux.");
        assertNotEquals(uc3.hashCode(), uc4.hashCode(),
                "hashCode doit être différent si les EmbeddedId sont différents.");

        // THEN : equals(null) → false ; equals avec autre type → false
        assertFalse(uc1.equals(null), "equals(null) doit renvoyer false.");
        assertFalse(uc1.equals("string"), "equals avec autre type doit renvoyer false.");
    }

    /**
     * Test de la méthode toString().
     * <p>
     * Vérifie que la chaîne résultante contient "userId=<valeur>" et "connectionId=<valeur>".
     * </p>
     */
    @Test
    @DisplayName("toString() doit contenir userId et connectionId")
    void testToStringContainsBothIds() {
        // GIVEN : objet UserConnection avec id composite connu
        UserConnection uc = new UserConnection(2, 99);
        uc.setId(new UserConnectionId(2, 99));

        // WHEN : on appelle toString()
        String str = uc.toString();

        // THEN : la chaîne contient "userId=2" et "connectionId=99"
        assertThat(str).contains("userId=2");
        assertThat(str).contains("connectionId=99");
    }
}

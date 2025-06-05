package com.paymybuddy.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires pour la classe Transaction.
 * <p>
 * On couvre ici :
 * <ul>
 *   <li>Constructeur avec tous les paramètres</li>
 *   <li>Constructeur sans argument (JPA)</li>
 *   <li>Les getters et setters (id, senderId, receiverId, description, amount, feePercent)</li>
 *   <li>La logique de setDescription (tronquer à 255 si nécessaire)</li>
 *   <li>La logique de setFeePercent (gestion du null → défaut 0,5)</li>
 *   <li>Les méthodes equals(Object) et hashCode()</li>
 *   <li>La méthode toString()</li>
 * </ul>
 * </p>
 */
class TransactionTest {

    /**
     * Test du constructeur complet {@code Transaction(Integer, Integer, String, BigDecimal, BigDecimal)}
     * et des getters correspondants.
     * <p>
     * GIVEN : on fournit tous les champs (senderId, receiverId, description, amount, feePercent).
     * WHEN  : on instancie via ce constructeur.
     * THEN  : tous les getters doivent renvoyer les valeurs passées.
     * </p>
     */
    @Test
    @DisplayName("Constructeur complet et getters doivent conserver les valeurs fournies")
    void testFullConstructorAndGetters() {
        // GIVEN : des valeurs d'exemple
        Integer senderId = 42;
        Integer receiverId = 84;
        String description = "Paiement urgent";
        BigDecimal amount = BigDecimal.valueOf(123.45);
        BigDecimal feePercent = BigDecimal.valueOf(0.75);

        // WHEN : on crée l’objet Transaction
        Transaction tx = new Transaction(senderId, receiverId, description, amount, feePercent);

        // THEN : chaque getter renvoie exactement la valeur passée
        assertNull(tx.getId(), "L'id doit être null juste après instanciation (avant save).");
        assertEquals(senderId, tx.getSenderId(), "senderId doit correspondre au constructeur.");
        assertEquals(receiverId, tx.getReceiverId(), "receiverId doit correspondre au constructeur.");
        assertEquals(description, tx.getDescription(), "description doit correspondre au constructeur.");
        assertEquals(amount, tx.getAmount(), "amount doit correspondre au constructeur.");
        assertEquals(feePercent, tx.getFeePercent(), "feePercent doit correspondre au constructeur.");
    }

    /**
     * Test du constructeur sans argument {@code Transaction()}.
     * <p>
     * GIVEN : on appelle le constructeur par défaut.
     * WHEN  : on examine les champs.
     * THEN  : tous les champs doivent être null (sauf feePercent initialisé à 0,5 via l’attribut).
     * </p>
     */
    @Test
    @DisplayName("Constructeur vide doit initialiser feePercent à 0,5 et laisser les autres champs à null")
    void testNoArgConstructor() {
        // WHEN : on crée l’objet Transaction sans argument
        Transaction tx = new Transaction();

        // THEN : id, senderId, receiverId, description, amount sont null
        assertNull(tx.getId(),         "id doit être null.");
        assertNull(tx.getSenderId(),   "senderId doit être null.");
        assertNull(tx.getReceiverId(), "receiverId doit être null.");
        assertNull(tx.getDescription(),"description doit être null.");
        assertNull(tx.getAmount(),     "amount doit être null.");

        // feePercent par défaut défini dans l’attribut
        assertEquals(BigDecimal.valueOf(0.5), tx.getFeePercent(),
                "feePercent doit être initialisé à 0,5 par défaut.");
    }

    /**
     * Test des setters/getters pour id, senderId, receiverId et amount.
     * <p>
     * GIVEN : on instancie un objet Transaction vide.
     * WHEN  : on utilise setId, setSenderId, setReceiverId, setAmount.
     * THEN  : on vérifie avec les getters que les valeurs sont bien enregistrées.
     * </p>
     */
    @Test
    @DisplayName("Setters et getters pour id, senderId, receiverId et amount doivent fonctionner")
    void testSimpleSettersAndGetters() {
        // GIVEN : une transaction vide
        Transaction tx = new Transaction();

        // WHEN : on modifie chaque champ
        tx.setId(100);
        tx.setSenderId(10);
        tx.setReceiverId(20);
        BigDecimal montant = BigDecimal.valueOf(999.99);
        tx.setAmount(montant);

        // THEN : les getters doivent renvoyer les valeurs exactes
        assertEquals(100, tx.getId(), "getId doit renvoyer la valeur définie par setId.");
        assertEquals(10,  tx.getSenderId(),   "getSenderId doit renvoyer la valeur définie par setSenderId.");
        assertEquals(20,  tx.getReceiverId(), "getReceiverId doit renvoyer la valeur définie par setReceiverId.");
        assertEquals(montant, tx.getAmount(), "getAmount doit renvoyer la valeur définie par setAmount.");
    }

    /**
     * Test du setter setDescription pour différents cas :
     * - description normale (longueur ≤ 255)
     * - description trop longue (longueur > 255) → vérifie que la chaîne est tronquée à 255 caractères
     * <p>
     * GIVEN : deux chaînes (une courte, une longue).
     * WHEN  : on appelle setDescription(...) pour chacune.
     * THEN  : getDescription() doit renvoyer soit la même chaîne, soit une version tronquée à 255.
     * </p>
     */
    @Test
    @DisplayName("setDescription doit accepter une chaîne courte et tronquer une chaîne trop longue")
    void testSetDescriptionTruncation() {
        // GIVEN : une chaîne courte (100 caractères)
        String courte = "a".repeat(100);
        //       une chaîne longue (300 caractères)
        String longue = "b".repeat(300);

        Transaction tx = new Transaction();

        // WHEN : on assigne la chaîne courte
        tx.setDescription(courte);
        // THEN : on récupère identique
        assertEquals(courte, tx.getDescription(),
                "getDescription doit renvoyer exactement la chaîne courte passée.");

        // WHEN : on assigne la chaîne longue
        tx.setDescription(longue);
        String result = tx.getDescription();

        // THEN : la longueur doit être exactement 255
        assertNotNull(result, "result ne doit pas être null.");
        assertEquals(255, result.length(), "La chaîne doit être tronquée à 255 caractères.");

        // Chaque caractère doit être 'b'
        for (char c : result.toCharArray()) {
            assertEquals('b', c, "Chaque caractère après tronquage doit être 'b'.");
        }
    }

    /**
     * Test du setter setFeePercent pour deux cas :
     * - valeur non nulle (doit être prise en compte)
     * - valeur null (doit réinitialiser à 0,5)
     * <p>
     * GIVEN : une transaction vide.
     * WHEN  : on appelle setFeePercent(...) avec null puis une valeur spécifique.
     * THEN  : getFeePercent() renvoie 0,5 ou la valeur passée.
     * </p>
     */
    @Test
    @DisplayName("setFeePercent doit accepter une valeur non nulle et gérer null en réinitialisant à 0,5")
    void testSetFeePercent() {
        // GIVEN : une transaction vide
        Transaction tx = new Transaction();

        // WHEN : on passe null
        tx.setFeePercent(null);
        // THEN : getFeePercent doit donner 0,5
        assertEquals(BigDecimal.valueOf(0.5), tx.getFeePercent(),
                "Lorsque la valeur passée est null, feePercent doit redevenir 0,5.");

        // WHEN : on passe 1,25
        tx.setFeePercent(BigDecimal.valueOf(1.25));
        // THEN : getFeePercent doit renvoyer 1,25
        assertEquals(BigDecimal.valueOf(1.25), tx.getFeePercent(),
                "getFeePercent doit renvoyer la valeur passée (1,25).");
    }

    /**
     * Test de la méthode equals(Object) et hashCode().
     * <p>
     * Scénarios testés :
     * <ul>
     *   <li>Deux instances avec le même id (pas null) → doivent être égales et avoir le même hashCode.</li>
     *   <li>Deux instances avec des id différents → ne doivent pas être égales (même si autres champs identiques).</li>
     *   <li>Comparaison avec null ou un autre type → should return false.</li>
     * </ul>
     * </p>
     */
    @Test
    @DisplayName("equals() et hashCode() doivent se baser sur l'id uniquement")
    void testEqualsAndHashCode() {
        // GIVEN : deux transactions avec même id = 5, mais champs différents
        Transaction t1 = new Transaction(1, 2, "Desc1", BigDecimal.TEN, BigDecimal.valueOf(0.5));
        t1.setId(5);
        Transaction t2 = new Transaction(3, 4, "Desc2", BigDecimal.valueOf(123), BigDecimal.valueOf(0.5));
        t2.setId(5);

        // WHEN / THEN : t1.equals(t2) doit être vrai, hashCode identique
        assertTrue(t1.equals(t2), "Deux transactions avec même id doivent être égales.");
        assertEquals(t1.hashCode(), t2.hashCode(),
                "Deux transactions égales doivent avoir même hashCode.");

        // GIVEN : deux transactions avec id différents
        Transaction t3 = new Transaction();
        t3.setId(6);
        Transaction t4 = new Transaction();
        t4.setId(7);

        // THEN : pas égales
        assertFalse(t3.equals(t4), "Transactions avec ids différents ne doivent pas être égales.");
        assertNotEquals(t3.hashCode(), t4.hashCode(),
                "HashCodes doivent être différents si les ids le sont.");

        // THEN : comparaison avec null ou autre type
        assertFalse(t1.equals(null), "equals(null) doit renvoyer false.");
        assertFalse(t1.equals("chaine"), "equals avec un autre type doit renvoyer false.");
    }

    /**
     * Test de la méthode toString().
     * <p>
     * Vérifie que le résultat contient la représentation textuelle de chaque champ.
     * </p>
     */
    @Test
    @DisplayName("toString() doit contenir id, senderId, receiverId, description, amount, feePercent")
    void testToStringContainsAllFields() {
        // GIVEN : une transaction avec des valeurs connues
        Transaction tx = new Transaction(11, 22, "Libellé test", BigDecimal.valueOf(55.55), BigDecimal.valueOf(2.5));
        tx.setId(33);

        // WHEN : on appelle toString()
        String str = tx.toString();

        // THEN : la chaîne doit contenir toutes les valeurs
        assertThat(str).contains("id=33");
        assertThat(str).contains("senderId=11");
        assertThat(str).contains("receiverId=22");
        assertThat(str).contains("description='Libellé test'");
        assertThat(str).contains("amount=55.55");
        assertThat(str).contains("feePercent=2.5");
    }
}

package com.paymybuddy.service;

import com.paymybuddy.dto.UserConnectionDto;
import com.paymybuddy.exception.ResourceNotFoundException;
import com.paymybuddy.exception.UserAlreadyExistsException;
import com.paymybuddy.model.UserConnection;
import com.paymybuddy.model.UserConnectionId;
import com.paymybuddy.repository.UserConnectionRepository;
import com.paymybuddy.repository.AppUserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

// ----------- Tests pour addConnection() -----------

/**
 * Tests unitaires pour la classe UserConnectionService.
 * <p>
 * On utilise Mockito pour simuler l’AppUserRepository et le UserConnectionRepository.
 * On teste les cas d’erreur et les cas de succès pour les méthodes :
 * <ul>
 *     <li>{@code addConnection()}</li>
 *     <li>{@code removeConnection()}</li>
 *     <li>{@code getConnectionsForUser()}</li>
 * </ul>
 */
@ExtendWith(MockitoExtension.class)
class UserConnectionServiceTest {

    @Mock
    private UserConnectionRepository userConnectionRepository;

    @Mock
    private AppUserRepository appUserRepository;

    @InjectMocks
    private UserConnectionService service;

    /**
     * Teste le cas où l'utilisateur principal (userId) n'existe pas.
     * <p>Doit lancer une {@link ResourceNotFoundException}.</p>
     */

    @Test
    @DisplayName("addConnection : userId inexistant doit lancer ResourceNotFoundException")
    void testAddConnection_UserNotFound() {
        // GIVEN : appUserRepository.findById(userId) renvoie Optional.empty()
        Integer userId = 10;
        Integer connectionId = 20;
        UserConnectionDto dto = new UserConnectionDto(userId, connectionId);

        when(appUserRepository.findById(userId.longValue()))
                .thenReturn(Optional.empty());

        // WHEN + THEN : ResourceNotFoundException
        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class, () -> {
            service.addConnection(dto);
        });
        assertTrue(ex.getMessage().contains("Utilisateur introuvable"));
        verify(appUserRepository).findById(userId.longValue());
        verifyNoMoreInteractions(appUserRepository);
        verifyNoInteractions(userConnectionRepository);
    }

    /**
     * Teste le cas où l’utilisateur à connecter (connectionId) n’existe pas.
     * <p>Doit lancer une {@link ResourceNotFoundException}.</p>
     */

    @Test
    @DisplayName("addConnection : connectionId inexistant doit lancer ResourceNotFoundException")
    void testAddConnection_ConnectionNotFound() {
        // GIVEN : user existe, connection inexistant
        Integer userId = 10;
        Integer connectionId = 20;
        UserConnectionDto dto = new UserConnectionDto(userId, connectionId);

        when(appUserRepository.findById(userId.longValue()))
                .thenReturn(Optional.of(mock(com.paymybuddy.model.AppUser.class)));
        when(appUserRepository.findById(connectionId.longValue()))
                .thenReturn(Optional.empty());

        // WHEN + THEN : ResourceNotFoundException
        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class, () -> {
            service.addConnection(dto);
        });
        assertTrue(ex.getMessage().contains("Utilisateur introuvable"));
        verify(appUserRepository).findById(userId.longValue());
        verify(appUserRepository).findById(connectionId.longValue());
        verifyNoMoreInteractions(userConnectionRepository);
    }

    /**
     * Teste le cas où l’utilisateur essaie de se connecter à lui-même.
     * <p>Doit lancer une {@link IllegalArgumentException}.</p>
     */

    @DisplayName("addConnection : même userId et connectionId => IllegalArgumentException")
    @Test
    void testAddConnection_SameUserAndConnection() {
        // GIVEN : Un DTO avec userId == connectionId
        Integer userId = 5;
        UserConnectionDto dto = new UserConnectionDto(userId, userId);

        // WHEN + THEN : L'appel doit lever une IllegalArgumentException
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            service.addConnection(dto);
        });

        assertTrue(ex.getMessage().contains("se connecter à lui-même"));

        // On s'assure qu'aucune interaction avec les repositories n'a eu lieu
        verifyNoInteractions(appUserRepository, userConnectionRepository);
    }

    /**
     * Teste le cas où la relation de connexion existe déjà en base.
     * <p>Doit lancer une {@link UserAlreadyExistsException}.</p>
     */

    @Test
    @DisplayName("addConnection : relation déjà existante doit lancer UserAlreadyExistsException")
    void testAddConnection_AlreadyExists() {
        // GIVEN : user et connection existent, mais relation déjà en base
        Integer userId = 3;
        Integer connectionId = 4;
        UserConnectionDto dto = new UserConnectionDto(userId, connectionId);

        when(appUserRepository.findById(userId.longValue()))
                .thenReturn(Optional.of(mock(com.paymybuddy.model.AppUser.class)));
        when(appUserRepository.findById(connectionId.longValue()))
                .thenReturn(Optional.of(mock(com.paymybuddy.model.AppUser.class)));

        UserConnectionId id = new UserConnectionId(userId, connectionId);
        when(userConnectionRepository.existsById(id)).thenReturn(true);

        // WHEN + THEN : UserAlreadyExistsException
        UserAlreadyExistsException ex = assertThrows(UserAlreadyExistsException.class, () -> {
            service.addConnection(dto);
        });
        assertTrue(ex.getMessage().contains("Connexion déjà existante"));
        verify(userConnectionRepository).existsById(id);
        verifyNoMoreInteractions(userConnectionRepository);
    }

    /**
     * Teste le cas où la connexion est valide et doit être créée.
     * <p>La méthode {@code save()} doit être appelée, et les données retournées doivent correspondre à l’entrée.</p>
     */

    @Test
    @DisplayName("addConnection : cas valide doit sauvegarder la relation")
    void testAddConnection_ValidCase() {
        // GIVEN : user et connection existent, relation non existante
        Integer userId = 7;
        Integer connectionId = 8;
        UserConnectionDto dto = new UserConnectionDto(userId, connectionId);

        when(appUserRepository.findById(userId.longValue()))
                .thenReturn(Optional.of(mock(com.paymybuddy.model.AppUser.class)));
        when(appUserRepository.findById(connectionId.longValue()))
                .thenReturn(Optional.of(mock(com.paymybuddy.model.AppUser.class)));

        UserConnectionId id = new UserConnectionId(userId, connectionId);
        when(userConnectionRepository.existsById(id)).thenReturn(false);

        // WHEN : on appelle addConnection
        UserConnectionDto result = service.addConnection(dto);

        // THEN : vérifier que save() s'est bien exécuté avec l’objet UserConnection correct
        ArgumentCaptor<UserConnection> captor = ArgumentCaptor.forClass(UserConnection.class);
        verify(userConnectionRepository).save(captor.capture());
        UserConnection saved = captor.getValue();
        assertEquals(userId, saved.getUserId(), "Le userId dans l'entité doit être identique au DTO.");
        assertEquals(connectionId, saved.getConnectionId(), "Le connectionId dans l'entité doit être identique au DTO.");

        // Résultat retourné doit être égal au DTO d’entrée
        assertEquals(userId, result.getUserId());
        assertEquals(connectionId, result.getConnectionId());
    }

    // ---------- Tests pour removeConnection() ----------

    /**
     * Teste le cas où l’utilisateur principal (userId) n’existe pas.
     * <p>Doit lancer une {@link ResourceNotFoundException}.</p>
     */

    @Test
    @DisplayName("removeConnection : userId inexistant doit lancer ResourceNotFoundException")
    void testRemoveConnection_UserNotFound() {
        // GIVEN : appUserRepository.findById(userId) renvoie Optional.empty()
        Integer userId = 9;
        Integer connectionId = 10;
        UserConnectionDto dto = new UserConnectionDto(userId, connectionId);

        when(appUserRepository.findById(userId.longValue()))
                .thenReturn(Optional.empty());

        // WHEN + THEN : ResourceNotFoundException
        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class, () -> {
            service.removeConnection(dto);
        });
        assertTrue(ex.getMessage().contains("Utilisateur introuvable"));
        verify(appUserRepository).findById(userId.longValue());
        verifyNoMoreInteractions(appUserRepository);
        verifyNoInteractions(userConnectionRepository);
    }

    /**
     * Teste le cas où l’utilisateur à déconnecter (connectionId) n’existe pas.
     * <p>Doit lancer une {@link ResourceNotFoundException}.</p>
     */

    @Test
    @DisplayName("removeConnection : connectionId inexistant doit lancer ResourceNotFoundException")
    void testRemoveConnection_ConnectionNotFound() {
        // GIVEN : user existe, connection inexistant
        Integer userId = 11;
        Integer connectionId = 12;
        UserConnectionDto dto = new UserConnectionDto(userId, connectionId);

        when(appUserRepository.findById(userId.longValue()))
                .thenReturn(Optional.of(mock(com.paymybuddy.model.AppUser.class)));
        when(appUserRepository.findById(connectionId.longValue()))
                .thenReturn(Optional.empty());

        // WHEN + THEN : ResourceNotFoundException
        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class, () -> {
            service.removeConnection(dto);
        });
        assertTrue(ex.getMessage().contains("Utilisateur introuvable"));
        verify(appUserRepository).findById(userId.longValue());
        verify(appUserRepository).findById(connectionId.longValue());
        verifyNoMoreInteractions(userConnectionRepository);
    }

    /**
     * Teste le cas où la connexion n’existe pas entre les deux utilisateurs.
     * <p>Doit lancer une {@link ResourceNotFoundException}.</p>
     */

    @Test
    @DisplayName("removeConnection : relation inexistante doit lancer ResourceNotFoundException")
    void testRemoveConnection_RelationNotFound() {
        // GIVEN : user et connection existent, mais relation non présente
        Integer userId = 13;
        Integer connectionId = 14;
        UserConnectionDto dto = new UserConnectionDto(userId, connectionId);

        when(appUserRepository.findById(userId.longValue()))
                .thenReturn(Optional.of(mock(com.paymybuddy.model.AppUser.class)));
        when(appUserRepository.findById(connectionId.longValue()))
                .thenReturn(Optional.of(mock(com.paymybuddy.model.AppUser.class)));

        UserConnectionId id = new UserConnectionId(userId, connectionId);
        when(userConnectionRepository.existsById(id)).thenReturn(false);

        // WHEN + THEN : ResourceNotFoundException
        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class, () -> {
            service.removeConnection(dto);
        });
        assertTrue(ex.getMessage().contains("Connexion inexistante"));
        verify(userConnectionRepository).existsById(id);
    }

    /**
     * Teste le cas où la suppression de la connexion est valide.
     * <p>Doit appeler {@code deleteById()} sur le repository.</p>
     */

    @Test
    @DisplayName("removeConnection : cas valide doit appeler deleteById()")
    void testRemoveConnection_ValidCase() {
        // GIVEN : user et connection existent, relation présente
        Integer userId = 15;
        Integer connectionId = 16;
        UserConnectionDto dto = new UserConnectionDto(userId, connectionId);

        when(appUserRepository.findById(userId.longValue()))
                .thenReturn(Optional.of(mock(com.paymybuddy.model.AppUser.class)));
        when(appUserRepository.findById(connectionId.longValue()))
                .thenReturn(Optional.of(mock(com.paymybuddy.model.AppUser.class)));

        UserConnectionId id = new UserConnectionId(userId, connectionId);
        when(userConnectionRepository.existsById(id)).thenReturn(true);

        // WHEN : on appelle removeConnection
        service.removeConnection(dto);

        // THEN : vérifier que deleteById(id) a été appelé
        verify(userConnectionRepository).deleteById(id);
    }

    // ---------- Tests pour getConnectionsForUser() ----------

    /**
     * Teste le cas où l’utilisateur n’existe pas dans la base.
     * <p>Doit lancer une {@link ResourceNotFoundException}.</p>
     */

    @Test
    @DisplayName("getConnectionsForUser : userId inexistant doit lancer ResourceNotFoundException")
    void testGetConnectionsForUser_UserNotFound() {
        // GIVEN : appUserRepository.findById(userId) renvoie Optional.empty()
        Integer userId = 17;

        when(appUserRepository.findById(userId.longValue()))
                .thenReturn(Optional.empty());

        // WHEN + THEN : ResourceNotFoundException
        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class, () -> {
            service.getConnectionsForUser(userId);
        });
        assertTrue(ex.getMessage().contains("Utilisateur introuvable"));
        verify(appUserRepository).findById(userId.longValue());
        verifyNoMoreInteractions(userConnectionRepository);
    }

    /**
     * Teste le cas où l’utilisateur existe mais n’a aucune connexion.
     * <p>Doit retourner une liste vide.</p>
     */

    @Test
    @DisplayName("getConnectionsForUser : aucun ami doit retourner liste vide")
    void testGetConnectionsForUser_NoConnections() {
        // GIVEN : user existant, mais aucune connexion enregistrée
        Integer userId = 18;

        when(appUserRepository.findById(userId.longValue()))
                .thenReturn(Optional.of(mock(com.paymybuddy.model.AppUser.class)));
        when(userConnectionRepository.findByIdUserId(userId))
                .thenReturn(Collections.emptyList());

        // WHEN : on appelle la méthode
        List<UserConnectionDto> result = service.getConnectionsForUser(userId);

        // THEN : liste vide
        assertNotNull(result);
        assertTrue(result.isEmpty(), "La liste doit être vide car aucune connexion n’est enregistrée.");
        verify(userConnectionRepository).findByIdUserId(userId);
    }

    /**
     * Teste le cas où l’utilisateur a des connexions existantes.
     * <p>
     * GIVEN   : un utilisateur principal existant en base (userId = 19),
     *           et deux UserConnection ([19→21], [19→22]) retournées par le repository,
     *           ainsi que des AppUser correspondants pour les amis (IDs 21 et 22).<br>
     * WHEN    : on appelle service.getConnectionsForUser(19).<br>
     * THEN    : on s’attend à obtenir une liste de deux UserConnectionDto,
     *           contenant exactement les paires (userId, connectionId) fournies,
     *           avec les champs friendEmail et friendUsername correctement remplis.
     * </p>
     */
    @Test
    @DisplayName("getConnectionsForUser : des connexions existantes doivent être retournées")
    void testGetConnectionsForUser_WithConnections() {
        // ==== GIVEN ====
        Integer userId = 19;
        Integer friend1 = 21;
        Integer friend2 = 22;

        // 1) stub de l’utilisateur principal
        com.paymybuddy.model.AppUser mainUser = mock(com.paymybuddy.model.AppUser.class);
        when(appUserRepository.findById(userId.longValue()))
                .thenReturn(Optional.of(mainUser));

        // 2) stub des connexions retournées par le repository
        UserConnection uc1 = new UserConnection(userId, friend1);
        UserConnection uc2 = new UserConnection(userId, friend2);
        when(userConnectionRepository.findByIdUserId(userId))
                .thenReturn(List.of(uc1, uc2));

        // 3) stub des entités AppUser pour chaque ami
        com.paymybuddy.model.AppUser friendEntity1 = mock(com.paymybuddy.model.AppUser.class);
        when(friendEntity1.getEmail()).thenReturn("ami1@mail.com");
        when(friendEntity1.getUsername()).thenReturn("amiUn");
        when(appUserRepository.findById(friend1.longValue()))
                .thenReturn(Optional.of(friendEntity1));

        com.paymybuddy.model.AppUser friendEntity2 = mock(com.paymybuddy.model.AppUser.class);
        when(friendEntity2.getEmail()).thenReturn("ami2@mail.com");
        when(friendEntity2.getUsername()).thenReturn("amiDeux");
        when(appUserRepository.findById(friend2.longValue()))
                .thenReturn(Optional.of(friendEntity2));

        // ==== WHEN ====
        List<UserConnectionDto> result = service.getConnectionsForUser(userId);

        // ==== THEN ====
        // 1) La liste doit contenir exactement 2 éléments
        assertNotNull(result, "Le résultat ne doit pas être null");
        assertEquals(2, result.size(), "La liste doit contenir 2 connexions");

        // 2) Vérification du premier élément
        UserConnectionDto dto1 = result.get(0);
        assertEquals(userId, dto1.getUserId(), "Le userId du premier DTO doit être " + userId);
        assertEquals(friend1, dto1.getConnectionId(), "Le connectionId du premier DTO doit être " + friend1);
        assertEquals("ami1@mail.com", dto1.getFriendEmail(), "Le friendEmail du premier DTO doit être ami1@mail.com");
        assertEquals("amiUn", dto1.getFriendUsername(), "Le friendUsername du premier DTO doit être amiUn");

        // 3) Vérification du deuxième élément
        UserConnectionDto dto2 = result.get(1);
        assertEquals(userId, dto2.getUserId(), "Le userId du deuxième DTO doit être " + userId);
        assertEquals(friend2, dto2.getConnectionId(), "Le connectionId du deuxième DTO doit être " + friend2);
        assertEquals("ami2@mail.com", dto2.getFriendEmail(), "Le friendEmail du deuxième DTO doit être ami2@mail.com");
        assertEquals("amiDeux", dto2.getFriendUsername(), "Le friendUsername du deuxième DTO doit être amiDeux");

        // 4) Vérifier les interactions sur les repositories

        // findById(userId) est appelé au moins une fois
        verify(appUserRepository, atLeastOnce()).findById(userId.longValue());

        // findByIdUserId(userId) exactement une fois
        verify(userConnectionRepository).findByIdUserId(userId);

        // Chaque friendId fait deux appels à findById(...)
        verify(appUserRepository, times(2)).findById(friend1.longValue());
        verify(appUserRepository, times(2)).findById(friend2.longValue());

        // Pas d’autres interactions
        verifyNoMoreInteractions(appUserRepository, userConnectionRepository);
    }


}

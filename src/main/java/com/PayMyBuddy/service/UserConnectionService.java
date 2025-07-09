package com.paymybuddy.service;

import com.paymybuddy.dto.UserConnectionDto;
import com.paymybuddy.exception.ResourceNotFoundException;
import com.paymybuddy.exception.UserAlreadyExistsException;
import com.paymybuddy.model.UserConnection;
import com.paymybuddy.model.UserConnectionId;
import com.paymybuddy.repository.UserConnectionRepository;
import com.paymybuddy.repository.AppUserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * Service qui gère la logique métier des connexions entre utilisateurs :
 * - ajout d’une connexion,
 * - suppression,
 * - récupération des connexions.
 */
@Service
public class UserConnectionService {

    private final UserConnectionRepository userConnectionRepository;
    private final AppUserRepository appUserRepository;

    public UserConnectionService(UserConnectionRepository userConnectionRepository,
                                 AppUserRepository appUserRepository) {
        this.userConnectionRepository = userConnectionRepository;
        this.appUserRepository = appUserRepository;
    }

    /**
     * Ajoute une connexion entre deux utilisateurs.
     */
    @Transactional
    public UserConnectionDto addConnection(UserConnectionDto dto) {
        Integer userId = dto.getUserId();
        Integer connectionId = dto.getConnectionId();

        if (userId.equals(connectionId)) {
            throw new IllegalArgumentException("Un utilisateur ne peut pas se connecter à lui-même.");
        }

        appUserRepository.findById(userId.longValue())
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable (ID = " + userId + ")"));

        appUserRepository.findById(connectionId.longValue())
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable (ID = " + connectionId + ")"));

        if (userConnectionRepository.existsById(new UserConnectionId(userId, connectionId))) {
            throw new UserAlreadyExistsException("Connexion déjà existante.");
        }

        UserConnection entity = new UserConnection(userId, connectionId);
        userConnectionRepository.save(entity);

        return dto;
    }

    /**
     * Supprime une connexion entre deux utilisateurs.
     */
    @Transactional
    public void removeConnection(UserConnectionDto dto) {
        Integer userId = dto.getUserId();
        Integer connectionId = dto.getConnectionId();

        appUserRepository.findById(userId.longValue())
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable (ID = " + userId + ")"));

        appUserRepository.findById(connectionId.longValue())
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable (ID = " + connectionId + ")"));

        UserConnectionId id = new UserConnectionId(userId, connectionId);
        if (!userConnectionRepository.existsById(id)) {
            throw new ResourceNotFoundException("Connexion inexistante entre " + userId + " et " + connectionId);
        }

        userConnectionRepository.deleteById(id);
    }

    /**
     * Liste les connexions d’un utilisateur.
     */
    @Transactional(readOnly = true)
    public List<UserConnectionDto> getConnectionsForUser(Integer userId) {
        appUserRepository.findById(userId.longValue())
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable (ID = " + userId + ")"));

        List<UserConnection> list = userConnectionRepository.findByIdUserId(userId);
        List<UserConnectionDto> dtoList = new ArrayList<>();

        for (UserConnection uc : list) {
            // Chargement des infos utilisateur
            String myUsername = appUserRepository.findById(uc.getUserId().longValue())
                    .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable : " + uc.getUserId()))
                    .getUsername();

            String friendEmail = appUserRepository.findById(uc.getConnectionId().longValue())
                    .orElseThrow(() -> new ResourceNotFoundException("Connexion introuvable : " + uc.getConnectionId()))
                    .getEmail();

            String friendUsername = appUserRepository.findById(uc.getConnectionId().longValue())
                    .orElseThrow(() -> new ResourceNotFoundException("Connexion introuvable : " + uc.getConnectionId()))
                    .getUsername();

            dtoList.add(new UserConnectionDto(
                    uc.getUserId(),
                    uc.getConnectionId(),
                    myUsername,
                    friendEmail,
                    friendUsername
            ));
        }

        return dtoList;
    }

}
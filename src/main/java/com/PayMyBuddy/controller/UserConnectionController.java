package com.paymybuddy.controller;

import com.paymybuddy.dto.UserConnectionDto;
import com.paymybuddy.service.UserConnectionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Contrôleur REST pour gérer les connexions (amis) entre utilisateurs.
 * <p>
 * Endpoints :
 *   - PUT    /api/connections       : ajouter une connexion
 *   - DELETE /api/connections       : supprimer une connexion
 *   - GET    /api/connections/{id}  : lister les connexions d'un utilisateur
 * </p>
 */
@RestController
@RequestMapping("/api/connections")
public class UserConnectionController {

    private final UserConnectionService service;

    public UserConnectionController(UserConnectionService service) {
        this.service = service;
    }

    /**
     * Ajoute une connexion (PUT /api/connections).
     * <p>
     * Corps de la requête : JSON UserConnectionDto { userId, connectionId }.
     * Renvoie 201 Created si tout se passe bien, avec le même DTO en corps.
     * </p>
     *
     * @param dto DTO contenant userId et connectionId.
     * @return ResponseEntity<UserConnectionDto> en HTTP 201 Created.
     */
    @PutMapping
    public ResponseEntity<UserConnectionDto> addConnection(@RequestBody UserConnectionDto dto) {
        UserConnectionDto saved = service.addConnection(dto);
        return new ResponseEntity<>(saved, HttpStatus.CREATED);
    }

    /**
     * Supprime une connexion (DELETE /api/connections).
     * <p>
     * Corps de la requête : JSON UserConnectionDto { userId, connectionId }.
     * Renvoie 204 No Content si la suppression réussit.
     * </p>
     *
     * @param dto DTO contenant userId et connectionId.
     * @return ResponseEntity<Void> en HTTP 204 No Content.
     */
    @DeleteMapping
    public ResponseEntity<Void> removeConnection(@RequestBody UserConnectionDto dto) {
        service.removeConnection(dto);
        return ResponseEntity.noContent().build();
    }

    /**
     * Liste toutes les connexions d'un utilisateur (GET /api/connections/{userId}).
     * <p>
     * Renvoie 200 OK et un tableau JSON de UserConnectionDto.
     * </p>
     *
     * @param userId ID de l'utilisateur dont on veut les connexions.
     * @return ResponseEntity<List<UserConnectionDto>> en HTTP 200 OK.
     */
    @GetMapping("/{userId}")
    public ResponseEntity<List<UserConnectionDto>> listConnections(@PathVariable Integer userId) {
        List<UserConnectionDto> list = service.getConnectionsForUser(userId);
        return ResponseEntity.ok(list);
    }
}

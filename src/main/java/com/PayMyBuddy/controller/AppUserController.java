package com.paymybuddy.controller;

import com.paymybuddy.dto.AppUserDto;
import com.paymybuddy.model.AppUser;
import com.paymybuddy.service.AppUserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Contrôleur REST pour les opérations sur les utilisateurs.
 */
@RestController
@RequestMapping("/api/users")
public class AppUserController {

    private final AppUserService service;

    public AppUserController(AppUserService service) {
        this.service = service;
    }

    /**
     * Inscrire un nouvel utilisateur (open).
     * @param user JSON avec username, email, password.
     * @return Utilisateur créé.
     */
    @PostMapping("/register")
    public ResponseEntity<AppUser> register(@RequestBody AppUser user) {
        AppUser saved = service.register(user);
        return new ResponseEntity<>(saved, HttpStatus.CREATED);
    }

    /**
     * Lister tous les utilisateurs (secured).
     * @return Liste des utilisateurs.
     */
    @GetMapping
    public ResponseEntity<List<AppUserDto>> listUsers() {
        List<AppUserDto> dtos = service.findAllDto();
        return ResponseEntity.ok(dtos);
    }

}
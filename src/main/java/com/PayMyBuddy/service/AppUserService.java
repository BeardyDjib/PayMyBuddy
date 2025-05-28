package com.paymybuddy.service;

import com.paymybuddy.model.AppUser;
import com.paymybuddy.repository.AppUserRepository;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;


import java.util.List;

@Service
public class AppUserService {

    private final AppUserRepository repository;

    public AppUserService(AppUserRepository repository) {
        this.repository = repository;
    }

    public List<AppUser> findAll() {
        return repository.findAll();
    }

    public AppUser save(AppUser user) {
        // hachage du mot de passe
        String hashed = BCrypt.hashpw(user.getPassword(), BCrypt.gensalt());
        user.setPassword(hashed);
        return repository.save(user);
    }

    public AppUser findByEmail(String email) {
        return repository.findByEmail(email).orElse(null);
    }
}
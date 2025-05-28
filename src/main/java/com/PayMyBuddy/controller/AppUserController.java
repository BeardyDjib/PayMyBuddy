package com.paymybuddy.controller;

import com.paymybuddy.model.AppUser;
import com.paymybuddy.service.AppUserService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class AppUserController {

    private final AppUserService service;

    public AppUserController(AppUserService service) {
        this.service = service;
    }

    @PostMapping
    public AppUser register(@RequestBody AppUser user) {
        return service.save(user);
    }

    @GetMapping
    public List<AppUser> listUsers() {
        return service.findAll();
    }
}
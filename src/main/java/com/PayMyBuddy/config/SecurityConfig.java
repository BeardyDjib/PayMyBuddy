package com.paymybuddy.config;

import com.paymybuddy.service.AppUserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Configuration de la sécurité HTTP et de l’authentification.
 * Définit les règles d’accès aux endpoints et gère l’encodage des mots de passe avec BCrypt.
 */

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final AppUserService appUserService;

    public SecurityConfig(AppUserService appUserService) {
        this.appUserService = appUserService;
    }

    /** Encode les mots de passe avec BCrypt. */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /** Configure DaoAuthenticationProvider avec AppUserService et BCrypt pour l’authentification. */

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        var provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService((UserDetailsService) appUserService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    /**
     * Définit les règles d’accès :
     * - POST /api/users/register et pages Thymeleaf publiques accessibles sans authentification
     * - tout le reste nécessite une authentification via formulaire (formLogin)
     */

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        // 1) API REST publique pour l'inscription
                        .requestMatchers(HttpMethod.POST, "/api/users/register").permitAll()

                        // 2) pages publiques : login, création de compte Thymeleaf, ressources statiques
                        .requestMatchers("/login", "/users/new", "/css/**", "/js/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/users").permitAll()

                        // 3) tout le reste nécessite authentification
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .defaultSuccessUrl("/", true)
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout")
                )
                .authenticationProvider(authenticationProvider());

        return http.build();
    }

}
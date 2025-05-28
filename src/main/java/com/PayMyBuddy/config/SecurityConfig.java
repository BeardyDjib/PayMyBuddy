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
 * Configuration de la sécurité HTTP et authentification.
 * Protège les endpoints, gère l'encodage des mots de passe et les rôles.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final AppUserService appUserService;

    public SecurityConfig(AppUserService appUserService) {
        this.appUserService = appUserService;
    }

    /**
     * Bean pour encoder et vérifier les mots de passe en BCrypt.
     * @return PasswordEncoder utilisant BCrypt.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Fait le lien entre AppUserService (UserDetailsService) et l'encodeur de mot de passe.
     * @return DaoAuthenticationProvider configuré.
     */
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService((UserDetailsService) appUserService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    /**
     * Chaîne de filtres de sécurité définissant l'accès.
     * - /api/users/register et swagger/unsecured sont publics.
     * - Toutes les autres requêtes nécessitent authentification.
     * @param http HttpSecurity à configurer.
     * @return SecurityFilterChain initialisée.
     * @throws Exception en cas d'erreur de configuration.
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // Désactive CSRF pour simplicité d'exemples d'API REST
                .csrf(csrf -> csrf.disable())

                // Contrôle d'accès aux endpoints
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.POST, "/api/users/register").permitAll()
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        .anyRequest().authenticated()
                )

                // Formulaire de login par défaut (Spring fournit /login)
                .httpBasic(Customizer.withDefaults())

                // Utilise notre provider pour l'authentification
                .authenticationProvider(authenticationProvider());

        return http.build();
    }
}

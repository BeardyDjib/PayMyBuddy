package com.paymybuddy.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // désactive la protection CSRF pour les tests POST
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/users/**").permitAll() // autorise tous les accès à /api/users
                        .anyRequest().authenticated() // le reste est protégé
                )
                .httpBasic(Customizer.withDefaults()); // autorisation basique
        return http.build();
    }
}

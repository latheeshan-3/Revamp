// src/main/java/com/revamp/auth/auth/config/SecurityConfig.java
package com.revamp.auth.auth.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**").permitAll() // allow all auth endpoints
                .anyRequest().authenticated()
            )
            .csrf(csrf -> csrf.disable()); // disable CSRF entirely

        return http.build();
    }
}


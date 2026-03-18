package com.example.riyanov.messenger.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // новый синтаксис отключения CSRF
                .authorizeHttpRequests(authz -> authz
                        .requestMatchers("/api/test/register","/api/test/login").permitAll()
                        .anyRequest().authenticated()
                )
                .httpBasic(httpBasic -> {}); // можно также использовать .httpBasic(Customizer.withDefaults())

        return http.build();
    }
}
package com.resume_parser.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.*;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    @Autowired JwtFilter jwtFilter;

    @Bean
        public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
            http
                    .csrf(csrf -> csrf.disable())
                    .authorizeHttpRequests(auth -> auth
                            .requestMatchers("/signup", "/login").permitAll()
                            .anyRequest().authenticated()
                    )
                    .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
            return http.build();
        }
    }

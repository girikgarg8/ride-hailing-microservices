package com.girikgarg.uberauthservice.config;

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
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) // Disable CSRF for API
            .authorizeHttpRequests(authorize -> authorize
                .requestMatchers("/api/v1/auth/signup/**").permitAll() // Allow signup endpoints
                .requestMatchers("/api/v1/auth/signin/**").permitAll() // Allow signup endpoints
                .requestMatchers("/actuator/health").permitAll() // Allow health check
                .anyRequest().authenticated() // All other requests require authentication
            );
        
        return http.build();
    }
    
    @Bean
    public PasswordEncoder bcryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }
}

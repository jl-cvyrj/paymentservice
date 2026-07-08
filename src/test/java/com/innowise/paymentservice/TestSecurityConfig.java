package com.innowise.paymentservice;

import com.innowise.paymentservice.config.JwtAuthFilter;
import com.innowise.paymentservice.service.JwtService;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class TestSecurityConfig {
    @Bean
    public JwtAuthFilter jwtAuthFilter(JwtService jwtService) {
        return new JwtAuthFilter(jwtService);
    }
}
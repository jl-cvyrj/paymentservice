package com.innowise.paymentservice.service;

import io.jsonwebtoken.Claims;
import com.innowise.paymentservice.exception.TokenException;

public interface JwtService {

    Claims extractAllClaims(String token);

    Long extractUserId(String token);

    String extractRole(String token);

    void isTokenValid(String token) throws TokenException;

    public String extractTokenType(String token);
}
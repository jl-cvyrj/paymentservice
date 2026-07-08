package com.innowise.paymentservice.entity.user;

import org.springframework.security.core.GrantedAuthority;

public enum UserRole implements GrantedAuthority {
    ADMIN,
    USER;

    @Override
    public String getAuthority() {
        return "ROLE_" + this.name();
    }
}
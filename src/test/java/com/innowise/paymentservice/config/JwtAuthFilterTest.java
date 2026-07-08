package com.innowise.paymentservice.config;

import com.innowise.paymentservice.service.JwtService;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtAuthFilterTest {

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private JwtAuthFilter jwtAuthFilter;

    private String generateTestTokenForUserId(Long userId, String role) {
        String token = "mocked-token";
        when(jwtService.extractTokenType(token)).thenReturn("access");
        when(jwtService.extractUserId(token)).thenReturn(userId);
        when(jwtService.extractRole(token)).thenReturn(role);
        return token;
    }

    @Test
    void filter_ShouldSetAuthentication_WhenTokenIsValid() throws Exception {
        String token = generateTestTokenForUserId(42L, "USER");

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + token);
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain filterChain = mock(FilterChain.class);

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals(42L, SecurityContextHolder.getContext().getAuthentication().getPrincipal());
        verify(filterChain).doFilter(request, response);
    }
}
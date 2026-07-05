package com.innowise.paymentservice.controller;

import com.innowise.paymentservice.dto.PaymentRequestDto;
import com.innowise.paymentservice.entity.Payment;
import com.innowise.paymentservice.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {
    private final PaymentService service;

    @PostMapping
    public ResponseEntity<Payment> createPayment(@RequestBody PaymentRequestDto request, @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(service.createPayment(request, jwt.getClaim("sub")));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Payment> getPayment(@PathVariable String id, @AuthenticationPrincipal Jwt jwt) {
        Payment payment = service.findById(id);
        boolean isAdmin = jwt.getClaimAsStringList("authorities").contains("ROLE_ADMIN");
        if (!isAdmin && !payment.getUserId().equals(jwt.getClaim("sub"))) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        return ResponseEntity.ok(payment);
    }

    @GetMapping
    public ResponseEntity<List<Payment>> getAllPayments(@RequestParam(required = false) String orderId, @RequestParam(required = false) String status, @RequestParam(required = false) String userId, @AuthenticationPrincipal Jwt jwt) {
        boolean isAdmin = jwt.getClaimAsStringList("authorities").contains("ROLE_ADMIN");
        String effectiveUserId = (isAdmin && userId != null) ? userId : jwt.getClaim("sub");
        return ResponseEntity.ok(service.findPayments(orderId, status, effectiveUserId));
    }

    @GetMapping("/users/{userId}/summary")
    public ResponseEntity<Double> getUserSummary(@PathVariable String userId, @RequestParam Instant start, @RequestParam Instant end, @AuthenticationPrincipal Jwt jwt) {
        boolean isAdmin = jwt.getClaimAsStringList("authorities").contains("ROLE_ADMIN");
        if (!isAdmin && !userId.equals(jwt.getClaim("sub"))) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        return ResponseEntity.ok(service.getSummary(userId, start, end));
    }

    @GetMapping("/summary")
    @PreAuthorize("hasAuthority('SCOPE_ADMIN')")
    public ResponseEntity<Double> getGlobalSummary() {
        return ResponseEntity.ok(service.getGlobalSummary());
    }
}
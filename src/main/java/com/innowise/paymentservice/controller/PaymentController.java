package com.innowise.paymentservice.controller;

import com.innowise.paymentservice.dto.PaymentRequestDto;
import com.innowise.paymentservice.dto.PaymentResponseDto;
import com.innowise.paymentservice.entity.PaymentStatus;
import com.innowise.paymentservice.mapper.PaymentMapper;
import com.innowise.paymentservice.service.PaymentService;
import jakarta.validation.Valid;
import org.mapstruct.Builder;
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
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;
    private final PaymentMapper paymentMapper;

    public PaymentController(PaymentService paymentService, PaymentMapper paymentMapper) {
        this.paymentService = paymentService;
        this.paymentMapper = paymentMapper;
    }

    @PostMapping
    public Mono<ResponseEntity<PaymentResponseDto>> createPayment(@Valid @RequestBody PaymentRequestDto request, @AuthenticationPrincipal Jwt jwt) {
        return paymentService.createPayment(request, jwt.getClaim("sub"))
                .map(dto -> {
                    return ResponseEntity.status(HttpStatus.ACCEPTED).body(dto);
                });
    }

    @GetMapping("/{id}")
    public Mono<ResponseEntity<PaymentResponseDto>> getPayment(@PathVariable String id, @AuthenticationPrincipal Jwt jwt) {
        return paymentService.findById(id)
                .map(payment -> {
                    boolean isAdmin = jwt.getClaimAsStringList("authorities").contains("ROLE_ADMIN");
                    if (!isAdmin && !payment.getUserId().equals(jwt.getClaim("sub"))) {
                        return ResponseEntity.status(HttpStatus.FORBIDDEN).<PaymentResponseDto>build();
                    }
                    return ResponseEntity.ok(paymentMapper.toDto(payment));
                })
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<PaymentResponseDto>> getAllPayments(
            @RequestParam(required = false) String orderId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String userId,
            @AuthenticationPrincipal Jwt jwt) {

        boolean isAdmin = jwt.getClaimAsStringList("authorities").contains("ROLE_ADMIN");
        String effectiveUserId = (isAdmin && userId != null) ? userId : jwt.getClaim("sub");

        List<PaymentResponseDto> response = paymentService.findPayments(orderId, status, effectiveUserId)
                .stream()
                .map(paymentMapper::toDto)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/users/{userId}/summary")
    public ResponseEntity<Double> getUserSummary(@PathVariable String userId, @RequestParam Instant start, @RequestParam Instant end, @AuthenticationPrincipal Jwt jwt) {
        boolean isAdmin = jwt.getClaimAsStringList("authorities").contains("ROLE_ADMIN");
        if (!isAdmin && !userId.equals(jwt.getClaim("sub"))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(paymentService.getSummary(userId, start, end));
    }

    @GetMapping("/summary")
    @PreAuthorize("hasAuthority('SCOPE_ADMIN')")
    public ResponseEntity<Double> getGlobalSummary() {
        return ResponseEntity.ok(paymentService.getGlobalSummary());
    }
}
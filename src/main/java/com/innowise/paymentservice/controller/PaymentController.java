package com.innowise.paymentservice.controller;

import com.innowise.paymentservice.dto.PaymentRequestDto;
import com.innowise.paymentservice.dto.PaymentResponseDto;
import com.innowise.paymentservice.mapper.PaymentMapper;
import com.innowise.paymentservice.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

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
    public Mono<ResponseEntity<PaymentResponseDto>> createPayment(@Valid @RequestBody PaymentRequestDto request, Authentication auth) {
        String userId = auth.getName();
        return paymentService.createPayment(request, userId)
                .map(dto -> ResponseEntity.status(HttpStatus.ACCEPTED).body(dto));
    }

    @GetMapping("/{id}")
    public Mono<ResponseEntity<PaymentResponseDto>> getPayment(@PathVariable String id, Authentication auth) {

        boolean isAdmin = isAdmin(auth);
        return paymentService.findById(id)
                .map(payment -> {
                    if (!isAdmin && !payment.getUserId().equals(auth.getName())) {
                        return ResponseEntity.status(HttpStatus.FORBIDDEN).<PaymentResponseDto>build();
                    }
                    return ResponseEntity.ok(paymentMapper.toDto(payment));
                })
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<PaymentResponseDto>> getAllPayments(@RequestParam(required = false) String orderId, @RequestParam(required = false) String status, @RequestParam(required = false) String userId, Authentication auth) {

        boolean isAdmin = isAdmin(auth);
        String effectiveUserId = (isAdmin && userId != null) ? userId : auth.getName();

        List<PaymentResponseDto> response = paymentService.findPayments(orderId, status, effectiveUserId)
                .stream()
                .map(paymentMapper::toDto)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/users/{userId}/summary")
    public ResponseEntity<Double> getUserSummary(@PathVariable String userId, @RequestParam Instant start, @RequestParam Instant end, Authentication auth) {

        if (!isAdmin(auth) && !userId.equals(auth.getName())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(paymentService.getSummary(userId, start, end));
    }

    @GetMapping("/summary")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Double> getGlobalSummary() {
        return ResponseEntity.ok(paymentService.getGlobalSummary());
    }

    private boolean isAdmin(Authentication auth) {
        return auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }
}
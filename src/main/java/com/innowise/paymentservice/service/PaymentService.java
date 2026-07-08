package com.innowise.paymentservice.service;

import com.innowise.paymentservice.dto.PaymentRequestDto;
import com.innowise.paymentservice.dto.PaymentResponseDto;
import com.innowise.paymentservice.entity.Payment;
import com.innowise.paymentservice.exception.ResourceNotFoundException;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.List;

public interface PaymentService {

    Mono<PaymentResponseDto> createPayment(PaymentRequestDto request, String userId);

    Mono<Payment> findById(String id) throws ResourceNotFoundException;

    List<Payment> findPayments(String orderId, String status, String userId);

    Double getSummary(String userId, Instant start, Instant end);

    Double getGlobalSummary();
}
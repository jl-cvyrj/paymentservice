package com.innowise.paymentservice.service;

import com.innowise.paymentservice.dto.PaymentRequestDto;
import com.innowise.paymentservice.dto.PaymentResponseDto;
import com.innowise.paymentservice.entity.Payment;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public interface PaymentService {

    PaymentResponseDto createPayment(PaymentRequestDto request, String userId);

    Payment findById(String id);

    List<Payment> findPayments(String orderId, String status, String userId);

    Double getSummary(String userId, Instant start, Instant end);

    Double getGlobalSummary();
}
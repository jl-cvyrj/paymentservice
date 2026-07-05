package com.innowise.paymentservice.dto;

import com.innowise.paymentservice.entity.PaymentStatus;
import java.math.BigDecimal;
import java.time.Instant;

public record PaymentResponseDto(
        String id,
        String orderId,
        String userId,
        PaymentStatus status,
        Instant timestamp,
        BigDecimal paymentAmount
) {}
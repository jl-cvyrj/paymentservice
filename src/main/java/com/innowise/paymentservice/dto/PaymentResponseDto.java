package com.innowise.paymentservice.dto;

import com.innowise.paymentservice.entity.PaymentStatus;
import lombok.Builder;
import java.math.BigDecimal;
import java.time.Instant;

@Builder(toBuilder = true)
public record PaymentResponseDto(
        String id,
        String orderId,
        String userId,
        PaymentStatus status,
        Instant timestamp,
        BigDecimal paymentAmount
) {}
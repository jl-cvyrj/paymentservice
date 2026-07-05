package com.innowise.paymentservice.dto;

import java.math.BigDecimal;

public record PaymentRequestDto(String orderId, BigDecimal paymentAmount) {}
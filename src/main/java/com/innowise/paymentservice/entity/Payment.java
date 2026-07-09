package com.innowise.paymentservice.entity;

import org.springframework.data.mongodb.core.mapping.Field;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Document(collection = "payments")
@Data
@NoArgsConstructor

public class Payment {

    @Id
    private String id;

    @Field("order_id")
    private String orderId;

    @Field("user_id")
    private String userId;

    private PaymentStatus status;

    private Instant timestamp;

    private BigDecimal paymentAmount;

}

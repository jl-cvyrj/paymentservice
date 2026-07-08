package com.innowise.paymentservice.service.impl;

import com.innowise.paymentservice.client.RandomNumberClient;
import com.innowise.paymentservice.dto.PaymentRequestDto;
import com.innowise.paymentservice.dto.PaymentResponseDto;
import com.innowise.paymentservice.dto.event.PaymentEventDto;
import com.innowise.paymentservice.entity.Payment;
import com.innowise.paymentservice.entity.PaymentStatus;
import com.innowise.paymentservice.mapper.PaymentMapper;
import com.innowise.paymentservice.repository.PaymentRepository;
import reactor.test.StepVerifier;
import org.junit.jupiter.api.Test;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.anyString;

class PaymentServiceUnitTest {

    private final PaymentRepository paymentRepository;
    private final MongoTemplate mongoTemplate;
    private final PaymentMapper paymentMapper;
    private final RandomNumberClient randomClient;
    private final KafkaTemplate<String, PaymentEventDto> kafkaTemplate;
    private final PaymentServiceImpl paymentService;

    public PaymentServiceUnitTest() {
        this.paymentRepository = mock(PaymentRepository.class);
        this.mongoTemplate = mock(MongoTemplate.class);
        this.paymentMapper = mock(PaymentMapper.class);
        this.randomClient = mock(RandomNumberClient.class);
        this.kafkaTemplate = mock(KafkaTemplate.class);

        this.paymentService = new PaymentServiceImpl(
                paymentRepository,
                mongoTemplate,
                paymentMapper,
                randomClient,
                kafkaTemplate
        );
    }

    @Test
    void createPayment_Success() {
        PaymentRequestDto request = new PaymentRequestDto("ord1", BigDecimal.valueOf(100.0));
        Payment payment = new Payment();
        payment.setOrderId("ord1");

        when(paymentMapper.toEntity(request)).thenReturn(payment);
        when(paymentMapper.toDto(any())).thenReturn(
                PaymentResponseDto.builder()
                        .orderId("ord1")
                        .status(PaymentStatus.SUCCESS)
                        .build()
        );
        when(paymentRepository.save(any(Payment.class))).thenReturn(Mono.just(payment));
        when(randomClient.getRandomNumber()).thenReturn(Mono.just(2));
        when(kafkaTemplate.send(anyString(), anyString(), any()))
                .thenReturn(CompletableFuture.completedFuture(mock(org.springframework.kafka.support.SendResult.class)));

        StepVerifier.create(paymentService.createPayment(request, "user"))
                .expectNextMatches(dto -> dto.status() == PaymentStatus.SUCCESS)
                .verifyComplete();
    }

    @Test
    void createPayment_Failure() {
        PaymentRequestDto request = new PaymentRequestDto("ord1", BigDecimal.valueOf(100.0));
        Payment payment = new Payment();
        payment.setOrderId("ord1");

        when(paymentMapper.toEntity(request)).thenReturn(payment);
        when(paymentMapper.toDto(any())).thenReturn(
                PaymentResponseDto.builder()
                        .orderId("ord1")
                        .status(PaymentStatus.FAILED)
                        .build()
        );
        when(paymentRepository.save(any(Payment.class))).thenReturn(Mono.just(payment));
        when(randomClient.getRandomNumber()).thenReturn(Mono.error(new RuntimeException("API Down")));
        when(kafkaTemplate.send(anyString(), anyString(), any()))
                .thenReturn(CompletableFuture.completedFuture(mock(org.springframework.kafka.support.SendResult.class)));

        StepVerifier.create(paymentService.createPayment(request, "user"))
                .expectNextMatches(dto -> dto.status() == PaymentStatus.FAILED)
                .verifyComplete();
    }
}
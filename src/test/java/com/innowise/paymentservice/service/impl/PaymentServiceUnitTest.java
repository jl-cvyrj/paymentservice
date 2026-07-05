package com.innowise.paymentservice.service.impl;

import com.innowise.paymentservice.client.RandomNumberClient;
import com.innowise.paymentservice.dto.PaymentRequestDto;
import com.innowise.paymentservice.dto.event.PaymentEventDto;
import com.innowise.paymentservice.entity.Payment;
import com.innowise.paymentservice.mapper.PaymentMapper;
import com.innowise.paymentservice.repository.PaymentRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.kafka.core.KafkaTemplate;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.anyString;

class PaymentServiceUnitTest {

    private final PaymentRepository paymentRepository;
    private final MongoTemplate mongoTemplate;
    private final PaymentMapper paymentMapper;
    private final RandomNumberClient randomClient;
    private final KafkaTemplate<String, PaymentEventDto> kafkaTemplate;
    private final PaymentServiceImpl paymentService;

    public PaymentServiceUnitTest() {
        this.paymentRepository = Mockito.mock(PaymentRepository.class);
        this.mongoTemplate = Mockito.mock(MongoTemplate.class);
        this.paymentMapper = Mockito.mock(PaymentMapper.class);
        this.randomClient = Mockito.mock(RandomNumberClient.class);
        this.kafkaTemplate = Mockito.mock(KafkaTemplate.class);

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

        when(paymentMapper.toEntity(request)).thenReturn(payment);
        when(randomClient.getRandomNumber()).thenReturn(2);
        when(paymentRepository.save(any(Payment.class))).thenReturn(payment);

        paymentService.createPayment(request, "user");

        verify(paymentRepository, times(2)).save(payment);
        verify(kafkaTemplate).send(anyString(), isNull(), any(PaymentEventDto.class));
    }

    @Test
    void createPayment_Failure() {
        PaymentRequestDto request = new PaymentRequestDto("ord1", BigDecimal.valueOf(100.0));
        when(randomClient.getRandomNumber()).thenThrow(new RuntimeException("API Down"));

        try {
            paymentService.createPayment(request, "user");
        } catch (Exception e) {
        }

        verify(paymentRepository, never()).save(any());
    }
}
package com.innowise.paymentservice.service.impl;

import com.innowise.paymentservice.client.RandomNumberClient;
import com.innowise.paymentservice.dto.PaymentRequestDto;
import com.innowise.paymentservice.dto.PaymentResponseDto;
import com.innowise.paymentservice.dto.event.PaymentEventDto;
import com.innowise.paymentservice.entity.Payment;
import com.innowise.paymentservice.entity.PaymentStatus;
import com.innowise.paymentservice.exception.ResourceNotFoundException;
import com.innowise.paymentservice.mapper.PaymentMapper;
import com.innowise.paymentservice.repository.PaymentRepository;
import com.innowise.paymentservice.service.PaymentService;
import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.List;

@Service
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final MongoTemplate mongoTemplate;
    private final PaymentMapper paymentMapper;
    private final RandomNumberClient randomClient;
    private final KafkaTemplate<String, PaymentEventDto> kafkaTemplate;

    public PaymentServiceImpl(PaymentRepository repository, MongoTemplate mongoTemplate, PaymentMapper paymentMapper, RandomNumberClient randomClient, KafkaTemplate<String, PaymentEventDto> kafkaTemplate) {
        this.paymentRepository = repository;
        this.mongoTemplate = mongoTemplate;
        this.paymentMapper = paymentMapper;
        this.randomClient = randomClient;
        this.kafkaTemplate = kafkaTemplate;
    }

    public Mono<PaymentResponseDto> createPayment(PaymentRequestDto request, String userId) {
        Payment payment = paymentMapper.toEntity(request);
        payment.setUserId(userId);
        payment.setStatus(PaymentStatus.PENDING);
        payment.setTimestamp(Instant.now());

        return paymentRepository.save(payment)
                .flatMap(savedPayment ->
                        randomClient.getRandomNumber()
                                .map(number -> {
                                    savedPayment.setStatus(number % 2 == 0 ? PaymentStatus.SUCCESS : PaymentStatus.FAILED);
                                    return savedPayment;
                                })
                                .onErrorResume(e -> {
                                    savedPayment.setStatus(PaymentStatus.FAILED);
                                    return Mono.just(savedPayment);
                                })
                )
                .flatMap(paymentRepository::save)
                .flatMap(updatedPayment ->
                        Mono.fromFuture(
                                kafkaTemplate.send("payment-events", updatedPayment.getOrderId(),
                                        new PaymentEventDto(updatedPayment.getOrderId(), updatedPayment.getStatus().name()))
                                )
                                .thenReturn(paymentMapper.toDto(updatedPayment))
                );
    }

    public Mono<Payment> findById(String id) {
        return paymentRepository.findById(id)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Payment not found")));
    }

    public List<Payment> findPayments(String orderId, String status, String userId) {
        Criteria criteria = Criteria.where("userId").is(userId);

        if (orderId != null) {
            criteria.and("orderId").is(orderId);
        }
        if (status != null) {
            criteria.and("status").is(PaymentStatus.valueOf(status));
        }

        Query query = new Query(criteria);
        return mongoTemplate.find(query, Payment.class);
    }

    public Double getSummary(String userId, Instant start, Instant end) {
        Aggregation agg = Aggregation.newAggregation(
                Aggregation.match(Criteria.where("userId").is(userId)
                        .and("status").is(PaymentStatus.SUCCESS)
                        .and("timestamp").gte(start).lte(end)),
                Aggregation.group().sum("paymentAmount").as("total")
        );
        Document result = mongoTemplate.aggregate(agg, "payments", Document.class).getUniqueMappedResult();
        return (result != null) ? result.getDouble("total") : 0.0;
    }

    public Double getGlobalSummary() {
        Aggregation agg = Aggregation.newAggregation(
                Aggregation.match(Criteria.where("status").is(PaymentStatus.SUCCESS)),
                Aggregation.group().sum("paymentAmount").as("total")
        );
        Document result = mongoTemplate.aggregate(agg, "payments", Document.class).getUniqueMappedResult();
        return (result != null) ? result.getDouble("total") : 0.0;
    }
}
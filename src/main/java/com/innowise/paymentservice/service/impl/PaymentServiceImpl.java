package com.innowise.paymentservice.service.impl;

import com.innowise.paymentservice.client.RandomNumberClient;
import com.innowise.paymentservice.dto.PaymentRequestDto;
import com.innowise.paymentservice.dto.PaymentResponseDto;
import com.innowise.paymentservice.entity.Payment;
import com.innowise.paymentservice.entity.PaymentStatus;
import com.innowise.paymentservice.mapper.PaymentMapper;
import com.innowise.paymentservice.repository.PaymentRepository;
import com.innowise.paymentservice.service.PaymentService;
import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final MongoTemplate mongoTemplate;
    private final PaymentMapper paymentMapper;
    private final RandomNumberClient randomClient;

    public PaymentServiceImpl(PaymentRepository repository, MongoTemplate mongoTemplate, PaymentMapper paymentMapper, RandomNumberClient randomClient) {
        this.paymentRepository = repository;
        this.mongoTemplate = mongoTemplate;
        this.paymentMapper = paymentMapper;
        this.randomClient = randomClient;
    }

    public PaymentResponseDto createPayment(PaymentRequestDto request, String userId) {
        Payment payment = paymentMapper.toEntity(request);
        payment.setUserId(userId);
        payment.setStatus(PaymentStatus.PENDING);
        payment.setTimestamp(Instant.now());
        paymentRepository.save(payment);

        try {
            Integer number = randomClient.getRandomNumber();
            payment.setStatus(number % 2 == 0 ? PaymentStatus.SUCCESS : PaymentStatus.FAILED);
        } catch (Exception e) {
            payment.setStatus(PaymentStatus.FAILED);
        }

        return paymentMapper.toDto(paymentRepository.save(payment));
    }

    public Payment findById(String id) {
        return paymentRepository.findById(id).orElseThrow(() -> new RuntimeException("Payment not found"));
    }

    public List<Payment> findPayments(String orderId, String status, String userId) {
        if (orderId != null) {
            return paymentRepository.findAllByUserIdAndOrderId(userId, orderId);
        }
        if (status != null) {
            return paymentRepository.findAllByUserIdAndStatus(userId, PaymentStatus.valueOf(status));
        }
        return paymentRepository.findAllByUserId(userId);
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
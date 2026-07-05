package com.innowise.paymentservice.repository;

import com.innowise.paymentservice.entity.Payment;
import com.innowise.paymentservice.entity.PaymentStatus;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface PaymentRepository extends MongoRepository<Payment, String> {

    @Query("{ 'userId': ?0 }")
    List<Payment> findAllByUserId(String userId);

    @Query("{ 'orderId': ?0 }")
    List<Payment> findAllByOrderId(String orderId);

    @Query("{ 'status': ?0 }")
    List<Payment> findAllByStatus(PaymentStatus status);
}

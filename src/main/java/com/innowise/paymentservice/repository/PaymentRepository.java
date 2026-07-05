package com.innowise.paymentservice.repository;

import com.innowise.paymentservice.entity.Payment;
import com.innowise.paymentservice.entity.PaymentStatus;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaymentRepository extends MongoRepository<Payment, String> {
    List<Payment> findAllByUserId(String userId);

    @Query("{ 'userId': ?0, 'orderId': ?1 }")
    List<Payment> findAllByUserIdAndOrderId(String userId, String orderId);

    @Query("{ 'userId': ?0, 'status': ?1 }")
    List<Payment> findAllByUserIdAndStatus(String userId, PaymentStatus status);
}

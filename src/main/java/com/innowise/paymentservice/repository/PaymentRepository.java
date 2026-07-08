package com.innowise.paymentservice.repository;

import com.innowise.paymentservice.entity.Payment;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentRepository extends ReactiveMongoRepository<Payment, String> {

}

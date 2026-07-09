package com.innowise.paymentservice.entity;

public enum PaymentStatus {

    PENDING,
    SUCCESS,
    FAILED;

    public String getStatusLabel() {
        return "STATUS_" + this.name();
    }
}

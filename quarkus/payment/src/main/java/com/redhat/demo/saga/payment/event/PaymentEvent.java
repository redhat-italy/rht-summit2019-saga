package com.redhat.demo.saga.payment.event;

import javax.persistence.*;
import java.time.Instant;

@Entity
public class PaymentEvent {

    //orderId is the correlationId
    @Id
    private String correlationId;

    @Column(nullable = false)
    private String accountId;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private PaymentEventType paymentEventType;

    private Instant createdOn;

    public String getCorrelationId() {
        return correlationId;
    }

    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }

    public Instant getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(Instant createdOn) {
        this.createdOn = createdOn;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public PaymentEventType getPaymentEventType() {
        return paymentEventType;
    }

    public void setPaymentEventType(PaymentEventType paymentEventType) {
        this.paymentEventType = paymentEventType;
    }



}

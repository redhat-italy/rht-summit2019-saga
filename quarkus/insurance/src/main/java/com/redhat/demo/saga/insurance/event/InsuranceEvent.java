package com.redhat.demo.saga.insurance.event;

import com.redhat.demo.saga.insurance.constant.InsuranceEventType;

import javax.persistence.*;
import java.io.Serializable;
import java.time.Instant;

@Entity
public class InsuranceEvent implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false)
    private Long id;

    //orderId is the correlationId
    @Column(nullable = false)
    private String correlationId;

    @Column(nullable = false)
    private Long itemId;

    @Column(nullable = false)
    private String accountId;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private InsuranceEventType itemEventType;

    @Column(nullable = false)
    private Double totalCost;

    private Instant createdOn;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }

    public Long getItemId() {
        return itemId;
    }

    public void setItemId(Long itemId) {
        this.itemId = itemId;
    }

    public InsuranceEventType getItemEventType() {
        return itemEventType;
    }

    public void setItemEventType(InsuranceEventType itemEventType) {
        this.itemEventType = itemEventType;
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

    public Double getTotalCost() {
        return totalCost;
    }

    public void setTotalCost(Double totalCost) {
        this.totalCost = totalCost;
    }
}

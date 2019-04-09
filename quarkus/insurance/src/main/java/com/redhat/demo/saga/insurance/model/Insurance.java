package com.redhat.demo.saga.insurance.model;

import javax.persistence.*;

@Entity
@NamedQuery(name = "Insurance.findByAccountAndState",
        query = "SELECT i FROM Insurance i where i.accountId = :accountId and i.state = :state")
@NamedQuery(name = "Insurance.findByOrderIdAndState",
        query = "SELECT i FROM Insurance i where i.orderId = :orderId and i.state = :state")
public class Insurance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false)
    private Long id;

    @Column(nullable = false)
    private String orderId;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private InsuranceState state;

    @Column(nullable = false)
    private String accountId;

    private String name;

    private Double totalCost;

    public Long getId() {
        return id;
    }

    public String getOrderId() {
        return orderId;
    }

    public InsuranceState getState() {
        return state;
    }

    public String getAccountId() {
        return accountId;
    }

    public String getName() {
        return name;
    }

    public Double getTotalCost() {
        return totalCost;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public void setState(InsuranceState state) {
        this.state = state;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setTotalCost(Double totalCost) {
        this.totalCost = totalCost;
    }
}

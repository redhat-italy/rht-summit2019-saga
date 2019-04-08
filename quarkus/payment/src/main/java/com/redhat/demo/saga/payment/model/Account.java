package com.redhat.demo.saga.payment.model;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity(name = "Account")
@Table(name = "account")
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false)
    private Long id;

    @OneToMany
    private List<Payment> payments = new ArrayList<>();

    private Double funds;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public List<Payment> getPayments() {
        return payments;
    }

    public void setPayments(List<Payment> payments) {
        this.payments = payments;
    }

    public Double getFunds() {
        return funds;
    }

    public void setFunds(Double funds) {
        this.funds = funds;
    }
}

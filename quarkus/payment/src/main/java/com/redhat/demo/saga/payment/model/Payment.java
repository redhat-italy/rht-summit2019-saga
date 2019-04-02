package com.redhat.demo.saga.payment.model;

import javax.persistence.*;

@Entity(name = "Payment")
@Table(name = "payment")
@NamedQuery(name = "Payment.findByOrder",
        query = "SELECT p FROM Payment p where p.orderId = :orderId")
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false)
    private Long id;

    @Column(unique = true, nullable = false)
    private String orderId;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private PaymentState state;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id")
    private Account account;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public PaymentState getState() {
        return state;
    }

    public void setState(PaymentState state) {
        this.state = state;
    }
}

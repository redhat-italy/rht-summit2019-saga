package com.redhat.demo.saga.payment.model;

import javax.persistence.*;

@Entity(name = "Payment")
@Table(name = "payment")
@NamedQuery(name = "Payment.findByOrder",
        query = "SELECT p FROM Payment p where p.order.id = :orderId")
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false)
    private Long id;

    @OneToOne
    private Order order;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private PaymentState state;

    @ManyToOne
    private Account account;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
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

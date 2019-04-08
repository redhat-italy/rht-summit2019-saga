package com.redhat.demo.saga.payment.model;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity(name = "Order")
@Table(name = "order")
public class Order {

    @Id
    private String id;

    @OneToOne
    private Payment payment;

    @OneToMany
    private List<OrderItem> orderItems = new ArrayList<>();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<OrderItem> getOrderItems() {
        return orderItems;
    }

    public void setOrderItems(List<OrderItem> orderItems) {
        this.orderItems = orderItems;
    }

    public Payment getPayment() {
        return payment;
    }

    public void setPayment(Payment payment) {
        this.payment = payment;
    }
}

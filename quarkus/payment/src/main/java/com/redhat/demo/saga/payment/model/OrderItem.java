package com.redhat.demo.saga.payment.model;

import javax.persistence.*;
import java.util.Objects;

@Entity(name = "OrderItem")
@Table(name = "orderitem")
public class OrderItem {

    @Id
    private String id;

    private Double cost;

    @ManyToOne
    private Order order;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Double getCost() {
        return cost;
    }

    public void setCost(Double cost) {
        this.cost = cost;
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public OrderItem() { }

    public OrderItem(String id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OrderItem orderItem = (OrderItem) o;
        return Objects.equals(id, orderItem.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}

package com.redhat.demo.saga.payment.model;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.validation.constraints.Size;

/**
 * Entity class that maps the Payment domain object
 *
 * @author Mauro Vocale
 * @version 1.0.0 10/04/2019
 */
@Entity(name = "Payment")
@Table(name = "payment")
@NamedQuery(name = "Payment.findByOrder",
        query = "SELECT p FROM Payment p where p.orderId = :orderId")
@NamedQuery(name = "Payment.findAll", query = "SELECT p FROM Payment p")
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false)
    private Integer id;

    @Column(nullable = false)
    private String orderId;

    @Column(nullable = false)
    private Double orderCost;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private PaymentState state;

    private String accountId;
    
    @Basic(optional = true)
    @Size(min = 1, max = 100)
    @Column(name = "lraId")
    private String lraId;
    
    public Payment() {
        
    }

    public Payment(String orderId, Double orderCost, PaymentState state, String accountId,
            String lraId) {
        this.orderId = orderId;
        this.orderCost = orderCost;
        this.state = state;
        this.accountId = accountId;
        this.lraId = lraId;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public PaymentState getState() {
        return state;
    }

    public void setState(PaymentState state) {
        this.state = state;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public Double getOrderCost() {
        return orderCost;
    }

    public void setOrderCost(Double orderCost) {
        this.orderCost = orderCost;
    }

    public String getLraId() {
        return lraId;
    }

    public void setLraId(String lraId) {
        this.lraId = lraId;
    }
    
}

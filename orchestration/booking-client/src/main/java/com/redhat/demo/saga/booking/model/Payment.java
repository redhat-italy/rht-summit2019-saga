package com.redhat.demo.saga.booking.model;

/**
 *
 * @author maurovocale
 */
public class Payment {

    private Integer id;

    private String orderId;

    private Double orderCost;

    private PaymentState state;

    private String accountId;
    
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

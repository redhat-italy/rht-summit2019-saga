package com.redhat.demo.saga.booking.model;

/**
 * Class that maps the Insurance domain object
 *
 * @author Mauro Vocale
 * @version 1.0.0 10/04/2019
 */
public class Insurance {

    private Integer id;

    private String orderId;

    private InsuranceState state;

    private String accountId;
    
    private Integer ticketId;

    private String name;

    private Double totalCost;
    
    private String lraId;

    public Insurance() {
        
    }
    
    public Insurance(String orderId, InsuranceState state, String accountId, 
            String name, Double totalCost, Integer ticketId, String lraId) {
        this.orderId = orderId;
        this.state = state;
        this.accountId = accountId;
        this.name = name;
        this.totalCost = totalCost;
        this.ticketId = ticketId;
        this.lraId = lraId;
    }

    public Integer getId() {
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

    public void setId(Integer id) {
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

    public String getLraId() {
        return lraId;
    }

    public void setLraId(String lraId) {
        this.lraId = lraId;
    }

    public Integer getTicketId() {
        return ticketId;
    }

    public void setTicketId(Integer ticketId) {
        this.ticketId = ticketId;
    }
    
}

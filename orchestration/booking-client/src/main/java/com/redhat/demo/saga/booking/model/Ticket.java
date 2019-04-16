package com.redhat.demo.saga.booking.model;

import java.io.Serializable;

/**
 * Class that maps the Ticket domain object
 * 
 * @author Mauro Vocale
 * @version 1.0.0 14/04/2019
 */
public class Ticket {
    
    private Integer id;

    private String orderId;

    private String accountId;
    
    private TicketState state;

    private String name;

    private String numberOfPersons;

    private Double totalCost;
    
    private String lraId;

    public Ticket() {
    }

    public Ticket(String orderId, String accountId, 
            String name, String numberOfPersons, Double totalCost, String lraId) {
        this.orderId = orderId;
        this.accountId = accountId;
        this.name = name;
        this.numberOfPersons = numberOfPersons;
        this.totalCost = totalCost;
        this.lraId = lraId;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNumberOfPersons() {
        return numberOfPersons;
    }

    public void setNumberOfPersons(String numberOfPersons) {
        this.numberOfPersons = numberOfPersons;
    }

    public Double getTotalCost() {
        return totalCost;
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

    public TicketState getState() {
        return state;
    }

    public void setState(TicketState state) {
        this.state = state;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "Ticket{" + "id=" + id + ", orderId=" + orderId + ", accountId=" + accountId + ", state=" + state + ", name=" + name + ", numberOfPersons=" + numberOfPersons + ", totalCost=" + totalCost + ", lraId=" + lraId + '}';
    }
    
    

}


package com.redhat.demo.saga.insurance.model;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQuery;
import javax.validation.constraints.Size;

/**
 * Entity class that maps the Insurance domain object
 *
 * @author Mauro Vocale
 * @version 1.0.0 10/04/2019
 */
@Entity
@NamedQuery(name = "Insurance.findByAccountAndState",
        query = "SELECT i FROM Insurance i where i.accountId = :accountId and i.state = :state")
@NamedQuery(name = "Insurance.findByOrderIdAndState",
        query = "SELECT i FROM Insurance i where i.orderId = :orderId and i.state = :state")
@NamedQuery(name = "Insurance.findAll", query = "SELECT i FROM Insurance i")
public class Insurance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false)
    private Integer id;

    @Column(nullable = false)
    private String orderId;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private InsuranceState state;

    @Column(nullable = false)
    private String accountId;
    
    @Column(nullable = false)
    private Integer ticketId;

    private String name;

    private Double totalCost;
    
    @Basic(optional = true)
    @Size(min = 1, max = 100)
    @Column(name = "lraId")
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

    public Integer getTicketId() {
        return ticketId;
    }

    public void setTicketId(Integer ticketId) {
        this.ticketId = ticketId;
    }

    public String getLraId() {
        return lraId;
    }

    public void setLraId(String lraId) {
        this.lraId = lraId;
    }
    
}

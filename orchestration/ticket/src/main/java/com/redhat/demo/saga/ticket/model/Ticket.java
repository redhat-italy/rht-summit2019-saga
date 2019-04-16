package com.redhat.demo.saga.ticket.model;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.validation.constraints.Size;

/**
 * Entity class that maps the Ticket domain object
 * 
 * @author Mauro Vocale
 *@version 1.0.0 09/04/2019
 */
@Entity
@Table(name = "Ticket")
@NamedQueries({
        @NamedQuery(name = "Ticket.findAll", query = "SELECT t FROM Ticket t")
})
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false)
    private Integer id;

    @Column(unique = true, nullable = false)
    private String orderId;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private TicketState state;

    @Column(nullable = false)
    private String accountId;

    private String name;

    private String numberOfPersons;

    private Double totalCost;
    
    @Basic(optional = true)
    @Size(min = 1, max = 100)
    @Column(name = "lraId")
    private String lraId;

    public Ticket() {
    }

    public Ticket(String orderId, TicketState state, String accountId, 
            String name, String numberOfPersons, Double totalCost, String lraId) {
        this.orderId = orderId;
        this.state = state;
        this.accountId = accountId;
        this.name = name;
        this.numberOfPersons = numberOfPersons;
        this.totalCost = totalCost;
        this.lraId = lraId;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public TicketState getState() {
        return state;
    }

    public void setState(TicketState state) {
        this.state = state;
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

    @Override
    public String toString() {
        return "Ticket{" + "id=" + id + ", orderId=" + orderId + ", state=" + state + ", accountId=" + accountId + ", name=" + name + ", numberOfPersons=" + numberOfPersons + ", totalCost=" + totalCost + ", lraId=" + lraId + '}';
    }

}

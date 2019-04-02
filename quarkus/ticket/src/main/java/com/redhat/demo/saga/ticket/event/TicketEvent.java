package com.redhat.demo.saga.ticket.event;

import javax.persistence.*;
import java.time.Instant;

@Entity
public class TicketEvent {

    //orderId is the correlationId
    @Id
    private String correlationId;

    @Column(nullable = false)
    private Long ticketId;

    @Column(nullable = false)
    private String accountId;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private TicketEventType ticketEventType;

    @Column(nullable = false)
    private Double totalCost;

    private Instant createdOn;

    public String getCorrelationId() {
        return correlationId;
    }

    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }

    public Long getTicketId() {
        return ticketId;
    }

    public void setTicketId(Long ticketId) {
        this.ticketId = ticketId;
    }

    public TicketEventType getTicketEventType() {
        return ticketEventType;
    }

    public void setTicketEventType(TicketEventType ticketEventType) {
        this.ticketEventType = ticketEventType;
    }

    public Instant getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(Instant createdOn) {
        this.createdOn = createdOn;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }


    public Double getTotalCost() {
        return totalCost;
    }

    public void setTotalCost(Double totalCost) {
        this.totalCost = totalCost;
    }
}

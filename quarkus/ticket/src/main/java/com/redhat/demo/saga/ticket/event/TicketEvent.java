package com.redhat.demo.saga.ticket.event;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.time.LocalDate;

@Entity
public class TicketEvent {

    @Id
    private Long id;
    private String accountId;
    private Long ticketId;
    private TicketEventType ticketEventType;
    private LocalDate createdOn;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public LocalDate getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(LocalDate createdOn) {
        this.createdOn = createdOn;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

}

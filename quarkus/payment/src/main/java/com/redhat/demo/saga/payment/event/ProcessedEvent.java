package com.redhat.demo.saga.payment.event;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQuery;
import java.time.Instant;

@Entity
@NamedQuery(name = "ProcessedEvent.findByEventType",
        query = "SELECT p FROM ProcessedEvent p where p.correlationId = :correlationId and p.eventType = :eventType")
@NamedQuery(name = "ProcessedEvent.findByItemIdAndEventType",
        query = "SELECT p FROM ProcessedEvent p where p.correlationId = :correlationId and p.itemId = :itemId and p.eventType = :eventType")
public class ProcessedEvent {

    @Id
    private String correlationId;

    private String itemId;

    private String eventType;

    private Instant receivedOn;

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public Instant getReceivedOn() {
        return receivedOn;
    }

    public void setReceivedOn(Instant receivedOn) {
        this.receivedOn = receivedOn;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }


    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }
}

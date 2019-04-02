package com.redhat.demo.saga.payment.event;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.time.Instant;

@Entity
public class ProcessedEvent {

    @Id
    private String correlationId;

    public Instant getReceivedOn() {
        return receivedOn;
    }

    public void setReceivedOn(Instant receivedOn) {
        this.receivedOn = receivedOn;
    }

    private Instant receivedOn;

    public String getCorrelationId() {
        return correlationId;
    }

    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }


}

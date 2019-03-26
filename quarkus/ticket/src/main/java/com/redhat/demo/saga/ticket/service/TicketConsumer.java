package com.redhat.demo.saga.ticket.service;


import io.smallrye.reactive.messaging.annotations.Broadcast;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Outgoing;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class TicketConsumer {

    private static final double CONVERSION_RATE = 0.88;

    @Incoming("tickets-data")
    @Outgoing("tickets-data-stream")
    @Broadcast
    public double process(int priceInUsd) {
        return priceInUsd * CONVERSION_RATE;
    }
}

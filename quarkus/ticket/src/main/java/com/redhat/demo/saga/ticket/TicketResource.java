package com.redhat.demo.saga.ticket;

import com.redhat.demo.saga.ticket.service.TicketProducer;
import io.smallrye.reactive.messaging.annotations.Stream;
import org.reactivestreams.Publisher;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/tickets")
public class TicketResource {

    @Inject
    @Stream("tickets-data-stream")
    Publisher<Double> prices;

    @Inject
    TicketProducer ticketProducer;

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String hello() {
        return "hello";
    }

    @GET
    @Path("/stream")
    @Produces(MediaType.SERVER_SENT_EVENTS)
    public Publisher<Double> stream() {
        return prices;
    }
}
package com.redhat.demo.saga.ticket;

import com.redhat.demo.saga.ticket.service.TicketService;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/tickets")
public class TicketResource {

    @Inject
    TicketService ticketService;

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String hello() {
        return "hello";
    }

}
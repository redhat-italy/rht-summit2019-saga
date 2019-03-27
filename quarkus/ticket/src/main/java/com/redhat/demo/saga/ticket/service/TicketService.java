package com.redhat.demo.saga.ticket.service;

import com.redhat.demo.saga.ticket.event.TicketEvent;
import com.redhat.demo.saga.ticket.event.TicketEventType;
import com.redhat.demo.saga.ticket.model.Ticket;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import java.time.LocalDate;

@ApplicationScoped
public class TicketService {

    @Inject
    EntityManager entityManager;

    @Transactional
    public void createTicket(Ticket ticket) {
        entityManager.persist(ticket);
        entityManager.flush();

        TicketEvent ticketEvent = new TicketEvent();
        ticketEvent.setCreatedOn(LocalDate.now());
        ticketEvent.setTicketId(ticket.getId());
        ticketEvent.setTicketEventType(TicketEventType.TICKET_CREATED);

        entityManager.persist(ticketEvent);
        entityManager.flush();
    }
}

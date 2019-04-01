package com.redhat.demo.saga.ticket.service;

import com.redhat.demo.saga.ticket.event.TicketEvent;
import com.redhat.demo.saga.ticket.event.TicketEventType;
import com.redhat.demo.saga.ticket.model.Ticket;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.transaction.Transactional;
import java.time.Instant;

import com.redhat.demo.saga.ticket.model.TicketState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class TicketService {

    private static final Logger LOGGER = LoggerFactory.getLogger(TicketService.class);

    @Inject
    EntityManager entityManager;

    @Inject
    EventService eventService;

    @Transactional
    public Ticket bookTicket(Ticket ticket) {

        Ticket existing = findTicketsByAccountAndState(ticket.getAccountId(), TicketState.TICKET_BOOKED_PENDING);
        if(existing != null) {
            LOGGER.error("A pending booking with orderId {} exists for account {}", existing.getOrderId(), ticket.getAccountId());
            existing.setMessageSeverityTicket("ERROR");
            existing.setMessageOnTicket("Pending booking, same account!");
            return existing;
        }

        ticket.setState(TicketState.TICKET_BOOKED_PENDING);

        entityManager.persist(ticket);
        entityManager.flush();

        TicketEvent ticketEvent = new TicketEvent();
        ticketEvent.setCorrelationId(ticket.getOrderId());
        ticketEvent.setAccountId(ticket.getAccountId());
        ticketEvent.setCreatedOn(Instant.now());
        ticketEvent.setTicketId(ticket.getId());
        ticketEvent.setTicketEventType(TicketEventType.TICKET_CREATED);

        entityManager.persist(ticketEvent);
        entityManager.flush();

        return ticket;
    }

    public Ticket findTicketsByAccountAndState(String accountId, TicketState ticketState) {
        Ticket ticket = null;
        try {
            ticket = (Ticket) entityManager.createNamedQuery("Ticket.findByAccountAndState")
                    .setParameter("accountId", accountId)
                    .setParameter("state", ticketState)
                    .getSingleResult();
        }
        catch (NoResultException nre){ }
        return ticket;
    }

    @Transactional
    public void onPaymentCreated(String correlationId) {

        if(eventService.isEventProcessed(correlationId)) {
            LOGGER.error("A payment event with same id {} already processed, discard!", correlationId);
            return;
        }

        //TODO
        //process event

        //persist message log

    }

    @Transactional
    public void onPaymentRefused(String correlationId) {

        if(eventService.isEventProcessed(correlationId)) {
            LOGGER.error("A payment event with same id {} already processed, discard!", correlationId);
            return;
        }

        //TODO
        //process event

        //persist message log

    }
}

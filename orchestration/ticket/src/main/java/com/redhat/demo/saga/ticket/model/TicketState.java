package com.redhat.demo.saga.ticket.model;

/**
 * Enumeration class that maps the possible state of the ticket
 * 
 * @author Mauro Vocale
 * @version 1.0.0 09/04/2019
 */
public enum TicketState {

    TICKET_BOOKED_PENDING, TICKET_BOOKED, TICKET_AVAILABLE;
}

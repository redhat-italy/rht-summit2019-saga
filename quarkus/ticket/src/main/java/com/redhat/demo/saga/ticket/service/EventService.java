package com.redhat.demo.saga.ticket.service;

import com.redhat.demo.saga.ticket.event.ProcessedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.transaction.Transactional;

@ApplicationScoped
public class EventService {

    private static final Logger LOGGER = LoggerFactory.getLogger(EventService.class);

    @Inject
    EntityManager entityManager;

    @Transactional
    public boolean isEventProcessed(String correlationId) {
        return entityManager.find(ProcessedEvent.class, correlationId) != null;
    }
}

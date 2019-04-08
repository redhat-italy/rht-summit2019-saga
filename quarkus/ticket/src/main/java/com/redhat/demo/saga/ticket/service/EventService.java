package com.redhat.demo.saga.ticket.service;

import com.redhat.demo.saga.ticket.event.ProcessedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.transaction.Transactional;

@ApplicationScoped
public class EventService {

    private static final Logger LOGGER = LoggerFactory.getLogger(EventService.class);

    @Inject
    EntityManager entityManager;

    @Transactional
    public ProcessedEvent processEvent(ProcessedEvent processedEvent) {

        entityManager.persist(processedEvent);
        entityManager.flush();

        return processedEvent;
    }

    @Transactional
    public boolean isEventProcessed(String orderId) {
        ProcessedEvent processedEvent = null;
        try {

            processedEvent = (ProcessedEvent) entityManager.createNamedQuery("ProcessedEvent.findByCorrelationId")
                    .setParameter("correlationId", orderId)
                    .getSingleResult();

        } catch (NoResultException nre) {
            return false;
        }
        return processedEvent != null;
    }
}

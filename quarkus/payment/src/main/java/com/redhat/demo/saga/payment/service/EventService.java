package com.redhat.demo.saga.payment.service;

import com.redhat.demo.saga.payment.event.ProcessedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.transaction.Transactional;
import java.time.Instant;

@ApplicationScoped
public class EventService {

    private static final Logger LOGGER = LoggerFactory.getLogger(EventService.class);

    @Inject
    EntityManager entityManager;

    @Transactional
    public ProcessedEvent processEvent(String orderId, String itemId, String eventType) {

        //create ProcessedEvent
        ProcessedEvent processedEvent = new ProcessedEvent();
        processedEvent.setCorrelationId(orderId);
        processedEvent.setReceivedOn(Instant.now());
        processedEvent.setEventType(eventType);
        if (itemId != null)
            processedEvent.setItemId(itemId);

        entityManager.persist(processedEvent);
        entityManager.flush();

        return processedEvent;
    }

    public boolean isEventProcessed(String orderId, String orderItem, String eventType) {
        ProcessedEvent processedEvent = null;
        try {
            if (orderItem != null) {
                processedEvent = (ProcessedEvent) entityManager.createNamedQuery("ProcessedEvent.findByItemIdAndEventType")
                        .setParameter("correlationId", orderId)
                        .setParameter("orderItem", orderItem)
                        .setParameter("eventType", eventType)
                        .getSingleResult();
            } else {
                processedEvent = (ProcessedEvent) entityManager.createNamedQuery("ProcessedEvent.findByEventType")
                        .setParameter("correlationId", orderId)
                        .setParameter("eventType", eventType)
                        .getSingleResult();
            }
        } catch (NoResultException nre) {
            return false;
        }
        return processedEvent != null;
    }


}

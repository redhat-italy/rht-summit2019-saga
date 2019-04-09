package com.redhat.demo.saga.insurance.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.redhat.demo.saga.insurance.constant.InsuranceEventType;
import com.redhat.demo.saga.insurance.constant.OrderEventType;
import com.redhat.demo.saga.insurance.constant.PaymentEventType;
import com.redhat.demo.saga.insurance.constant.TicketEventType;
import com.redhat.demo.saga.insurance.event.*;
import com.redhat.demo.saga.insurance.model.Insurance;
import com.redhat.demo.saga.insurance.model.InsuranceState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.transaction.Transactional;
import java.time.Instant;

@ApplicationScoped
public class InsuranceService {

    private static final Logger LOGGER = LoggerFactory.getLogger(InsuranceService.class);

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Inject
    EntityManager entityManager;

    @Inject
    EventService eventService;


    @Transactional
    public void onEventReceived(String orderId, String payload) {

        LOGGER.info("Received event: orderId {} - payload {}" , orderId, payload);

        JsonNode json;
        String accountId;
        String itemEventType;
        Double itemCost;
        Boolean insuranceRequired;

        try {
            json = objectMapper.readTree(payload);
            accountId = json.get("accountid").asText();
            itemEventType = json.get("itemeventtype").asText();
            itemCost = json.get("totalcost").asDouble();
            insuranceRequired = json.get("insurancerequired").asBoolean();
        } catch (Exception e) {
            LOGGER.error("Can't create JsonNode {}", e);
            return;
        }

        //verify if item is already processed
        if(eventService.isEventProcessed(orderId, itemEventType)) {
            LOGGER.error("An event with same order id {}, itemEventType {} already processed, discard!", orderId, itemEventType);
            return;
        }


        Insurance existing = findInsurancesByAccountAndState(accountId, InsuranceState.INSURANCE_BOOKED_PENDING);
        if(existing != null) {
            LOGGER.error("A pending booking with orderId {} exists for account {}", existing.getOrderId(), accountId);
            return;
        }

        LOGGER.info("Received eventtype {} for order id {}", itemEventType, orderId);

        //verify item
        if(itemEventType.equals(TicketEventType.TICKET_CREATED.name())) {

            Double insuranceCost = 0.0;

            if(insuranceRequired) {

                //TODO get sample insurance data

                //create insurance
                Insurance insurance = new Insurance();
                insurance.setState(InsuranceState.INSURANCE_BOOKED_PENDING);
                insurance.setOrderId(orderId);
                insurance.setAccountId(accountId);
                insurance.setName("I1");
                insurance.setTotalCost(10.0);
                insuranceCost = 10.0;

                entityManager.persist(insurance);
                entityManager.flush();

                // Create insuranceEvent
                InsuranceEvent insuranceEvent = new InsuranceEvent();
                insuranceEvent.setCorrelationId(insurance.getOrderId());
                insuranceEvent.setAccountId(insurance.getAccountId());
                insuranceEvent.setCreatedOn(Instant.now());
                insuranceEvent.setItemId(insurance.getId());
                insuranceEvent.setTotalCost(insurance.getTotalCost());
                insuranceEvent.setItemEventType(InsuranceEventType.INSURANCE_CREATED);

                entityManager.persist(insuranceEvent);
                entityManager.flush();
            }

            // Create Order
            OrderEvent orderEvent = new OrderEvent();
            orderEvent.setCreatedOn(Instant.now());
            orderEvent.setItemEventType(OrderEventType.ORDER_COMPLETED);
            orderEvent.setAccountId(accountId);
            orderEvent.setCorrelationId(orderId);
            //total amount for order
            orderEvent.setTotalCost(itemCost + insuranceCost);

            entityManager.persist(orderEvent);
            entityManager.flush();


            //create ProcessedEvent
            ProcessedEvent processedEvent = new ProcessedEvent();
            processedEvent.setCorrelationId(orderId);
            processedEvent.setReceivedOn(Instant.now());
            processedEvent.setEventType(itemEventType);
            eventService.processEvent(processedEvent);

        }  else {
            LOGGER.error("Event type not recognized!");
            return;
        }

    }

    @Transactional
    public void onPaymentReceived(String correlationId, JsonNode json) {

        String accountId = json.get("accountid").asText();
        String itemEventType = json.get("itemeventtype").asText();

        if(eventService.isEventProcessed(correlationId, itemEventType)) {
            LOGGER.error("A payment event with same orderId {} itemEventType {} already processed, discard!", correlationId, itemEventType);
            return;
        }

        //find insurance
        Insurance insurance = findInsurancesByOrderIdAndState(correlationId, InsuranceState.INSURANCE_BOOKED_PENDING);
        if(insurance != null) {
            //verify item
            if(itemEventType == PaymentEventType.PAYMENT_ACCEPTED.name()) {
                insurance.setState(InsuranceState.INSURANCE_BOOKED);

            } else if(itemEventType == PaymentEventType.PAYMENT_REFUSED.name()) {
                insurance.setState(InsuranceState.INSURANCE_PAYMENT_REFUSED);
            }
        }

        //create ProcessedEvent
        ProcessedEvent processedEvent = new ProcessedEvent();
        processedEvent.setCorrelationId(correlationId);
        processedEvent.setReceivedOn(Instant.now());
        processedEvent.setEventType(itemEventType);
        eventService.processEvent(processedEvent);

    }

    public Insurance findInsurancesByAccountAndState(String accountId, InsuranceState insuranceState) {
        Insurance insurance = null;
        try {
            insurance = (Insurance) entityManager.createNamedQuery("Insurance.findByAccountAndState")
                    .setParameter("accountId", accountId)
                    .setParameter("state", insuranceState)
                    .getSingleResult();
        }
        catch (NoResultException nre){ }
        return insurance;
    }

    public Insurance findInsurancesByOrderIdAndState(String orderId, InsuranceState insuranceState) {
        Insurance insurance = null;
        try {
            insurance = (Insurance) entityManager.createNamedQuery("Insurance.findByOrderIdAndState")
                    .setParameter("orderId", orderId)
                    .setParameter("state", insuranceState)
                    .getSingleResult();
        }
        catch (NoResultException nre){ }
        return insurance;
    }


}

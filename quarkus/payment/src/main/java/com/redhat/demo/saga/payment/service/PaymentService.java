package com.redhat.demo.saga.payment.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.redhat.demo.saga.payment.event.OrderEventType;
import com.redhat.demo.saga.payment.event.PaymentEvent;
import com.redhat.demo.saga.payment.event.PaymentEventType;
import com.redhat.demo.saga.payment.event.TicketEventType;
import com.redhat.demo.saga.payment.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.transaction.Transactional;
import java.time.Instant;
import java.util.List;

@ApplicationScoped
public class PaymentService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PaymentService.class);

    @Inject
    EntityManager entityManager;

    @Inject
    EventService eventService;

    @Inject
    AccountService accountService;


    @Transactional
    public void onEventReceived(String orderId, JsonNode json) {

        String itemId = json.get("itemid").asText();
        String accountId = json.get("accountid").asText();
        String itemEventType = json.get("itemeventtype").asText();
        Double itemCost = json.get("totalcost").asDouble();

        //verify if item is already processed
        if(eventService.isEventProcessed(orderId, itemId, itemEventType)) {
            LOGGER.error("An event with same order id {}, item id {}, itemEventType {} already processed, discard!", orderId, itemId, itemEventType);
            return;
        }

        //verify there is already a orderId for payment
        Payment payment = findPaymentByOrderId(orderId);
        //get Account
        Account account = accountService.findById(accountId);

        //verify item
        if(itemEventType == TicketEventType.TICKET_CREATED.name()) {
            if(payment != null && payment.getOrder().getOrderItems().contains(new OrderItem(itemId))) {
                LOGGER.error("A payment with same order id {} and item id {} already processed, discard!", orderId, itemId);
                return;
            }

            boolean newPayment = false;

            if(payment == null) {
                newPayment = true;
                payment = new Payment();
                payment.setAccount(account);
                payment.setState(PaymentState.PAYMENT_INPROGRESS);
                Order order = new Order();
                order.setId(orderId);
                order.setPayment(payment);
            }

            OrderItem orderItem = new OrderItem();
            orderItem.setId(itemId);
            orderItem.setCost(itemCost);
            orderItem.setOrder(payment.getOrder());
            payment.getOrder().getOrderItems().add(orderItem);

            if(newPayment)
                createPayment(payment);

            eventService.processEvent(orderId, orderItem.getId(),TicketEventType.TICKET_CREATED.name());

        } else if(itemEventType == OrderEventType.ORDER_COMPLETED.name()) {
            if (payment == null) {
                LOGGER.error("No payment for order id {}!", orderId);
                return;
            }
            //check order item in order event
            List<String> itemIds = json.findValuesAsText("itemsIds");
            for(String tmp: itemIds) {
                if (!payment.getOrder().getOrderItems().contains(new OrderItem(tmp))) {
                    LOGGER.error("A payment order id {} doesn't not contain all the items required!", orderId);
                    return;
                }
            }
            //verify account funds
            Double orderCost = 0.0;
            for(OrderItem orderItem: payment.getOrder().getOrderItems())
                orderCost+= orderItem.getCost();
            if(account.getFunds() < orderCost)
                payment.setState(PaymentState.PAYMENT_REFUSED);
            else
                payment.setState(PaymentState.PAYMENT_ACCEPTED);

            createPaymentEvent(payment);
            eventService.processEvent(orderId, null, OrderEventType.ORDER_COMPLETED.name());
        }

    }

    @Transactional
    public void createPayment(Payment payment) {
        entityManager.persist(payment);
        entityManager.flush();

        PaymentEvent paymentEvent = new PaymentEvent();
        paymentEvent.setCorrelationId(payment.getOrder().getId());
        paymentEvent.setAccountId(String.valueOf(payment.getAccount().getId()));
        paymentEvent.setCreatedOn(Instant.now());
        paymentEvent.setItemeventtype(PaymentEventType.valueOf(payment.getState().name()));

        entityManager.persist(paymentEvent);
        entityManager.flush();
    }

    @Transactional
    public void createPaymentEvent(Payment payment) {
        PaymentEvent paymentEvent = new PaymentEvent();
        paymentEvent.setCorrelationId(payment.getOrder().getId());
        paymentEvent.setAccountId(String.valueOf(payment.getAccount().getId()));
        paymentEvent.setCreatedOn(Instant.now());
        paymentEvent.setItemeventtype(PaymentEventType.valueOf(payment.getState().name()));

        entityManager.persist(paymentEvent);
        entityManager.flush();
    }


    public Payment findPaymentByOrderId(String orderId) {
        Payment payment = null;
        try {
            payment = (Payment) entityManager.createNamedQuery("Payment.findByOrder")
                    .setParameter("orderId", orderId)
                    .getSingleResult();
        }
        catch (NoResultException nre){ }
        return payment;
    }

}

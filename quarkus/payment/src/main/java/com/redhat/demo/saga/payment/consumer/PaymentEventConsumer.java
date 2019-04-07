package com.redhat.demo.saga.payment.consumer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.redhat.demo.saga.payment.event.OrderEventType;
import com.redhat.demo.saga.payment.event.TicketEventType;
import com.redhat.demo.saga.payment.model.*;
import com.redhat.demo.saga.payment.service.AccountService;
import com.redhat.demo.saga.payment.service.EventService;
import com.redhat.demo.saga.payment.service.PaymentService;
import io.smallrye.reactive.messaging.kafka.KafkaMessage;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletionStage;

@ApplicationScoped
public class PaymentEventConsumer {

    private static final Logger LOGGER = LoggerFactory.getLogger(PaymentEventConsumer.class);

    @Inject
    EventService eventService;

    @Inject
    PaymentService paymentService;

    @Inject
    AccountService accountService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Incoming("tickets")
    @Transactional
    public CompletionStage<Void> onMessage(KafkaMessage<String, String> message) throws IOException {
        try {
            JsonNode json = objectMapper.readTree(message.getPayload());
            final Optional<String> orderId = message.getHeaders().getOneAsString("correlationid");
            String itemId = json.get("itemid").asText();
            String accountId = json.get("accountid").asText();
            String itemEventType = json.get("ticketeventtype").asText();
            Double itemCost = json.get("totalcost").asDouble();


            if (orderId.isPresent()) {
                //verify if item is already processed
                if(eventService.isEventProcessed(orderId.get(), itemId, itemEventType)) {
                    LOGGER.error("An event with same order id {}, item id {}, itemEventType {} already processed, discard!", orderId.get(), itemId, itemEventType);
                    return null;
                }

                //verify there is already a orderId for payment
                Payment payment = paymentService.findPaymentByOrderId(orderId.get());
                //get Account
                Account account = accountService.findById(accountId);

                //verify item
                if(itemEventType == TicketEventType.TICKET_CREATED.name()) {
                    if(payment != null && payment.getOrder().getOrderItems().contains(new OrderItem(itemId))) {
                        LOGGER.error("A payment with same order id {} and item id {} already processed, discard!", orderId.get(), itemId);
                        return null;
                    }

                    boolean newPayment = false;

                    if(payment == null) {
                        newPayment = true;
                        payment = new Payment();
                        payment.setAccount(account);
                        payment.setState(PaymentState.PAYMENT_INPROGRESS);
                        Order order = new Order();
                        order.setId(orderId.get());
                        order.setPayment(payment);
                    }

                    OrderItem orderItem = new OrderItem();
                    orderItem.setId(itemId);
                    orderItem.setCost(itemCost);
                    orderItem.setOrder(payment.getOrder());
                    payment.getOrder().getOrderItems().add(orderItem);

                    if(newPayment)
                        paymentService.createPayment(payment);

                    eventService.processEvent(orderId.get(), orderItem.getId(),TicketEventType.TICKET_CREATED.name());

                } else if(itemEventType == OrderEventType.ORDER_COMPLETED.name()) {
                    if (payment == null) {
                        LOGGER.error("No payment for order id {}!", orderId.get());
                        return null;
                    }
                    //check order item in order event
                    List<String> itemIds = json.findValuesAsText("itemsIds");
                    for(String tmp: itemIds) {
                        if (!payment.getOrder().getOrderItems().contains(new OrderItem(tmp))) {
                            LOGGER.error("A payment order id {} doesn't not contain all the items required!", orderId.get());
                            return null;
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

                    eventService.processEvent(orderId.get(), null, OrderEventType.ORDER_COMPLETED.name());
                }

            }


        }
        catch (Throwable t) {

        }
        return message.ack();
    }
}

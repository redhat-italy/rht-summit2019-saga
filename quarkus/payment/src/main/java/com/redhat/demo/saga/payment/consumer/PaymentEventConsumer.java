package com.redhat.demo.saga.payment.consumer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.redhat.demo.saga.payment.event.ProcessedEvent;
import com.redhat.demo.saga.payment.event.TicketEventType;
import com.redhat.demo.saga.payment.model.Account;
import com.redhat.demo.saga.payment.model.Payment;
import com.redhat.demo.saga.payment.model.PaymentState;
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
import java.time.Instant;
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
            final Optional<String> orderId = message.getHeaders().getOneAsString("correlationid");
            if (orderId.isPresent()) {
                if(eventService.isEventProcessed(orderId.get())) {
                    LOGGER.error("A payment event with same order id {} already processed, discard!", orderId.get());
                    return null;
                }

                //create ProcessedEvent
                ProcessedEvent processedEvent = new ProcessedEvent();
                processedEvent.setCorrelationId(orderId.get());
                processedEvent.setReceivedOn(Instant.now());
                eventService.processEvent(processedEvent);

                //verify there is already a orderId for payment
                Payment payment = paymentService.findPaymentByOrderId(orderId.get());

                if(payment != null) {
                    //TODO send payment refused event
                } else {
                    JsonNode json = objectMapper.readTree(message.getPayload());
                    String accountId = json.get("accountid").asText();
                    String ticketEventType = json.get("ticketeventtype").asText();
                    Double ticketCost = json.get("totalcost").asDouble();
                    //verify account funds
                    if(ticketEventType == TicketEventType.TICKET_CREATED.name()) {
                        Account account = accountService.findById(accountId);
                        if(account != null) {
                           Double currentFunds = account.getFunds();
                           if(currentFunds > ticketCost) {
                               //create payment
                               payment = new Payment();
                               payment.setAccount(account);
                               payment.setOrderId(orderId.get());
                               payment.setState(PaymentState.PAYMENT_ACCEPTED);
                           }
                           else {
                               payment = new Payment();
                               payment.setAccount(account);
                               payment.setOrderId(orderId.get());
                               payment.setState(PaymentState.PAYMENT_REFUSED);
                           }
                           paymentService.createPayment(payment);
                        }
                    }
                }
            }


        }
        catch (Throwable t) {

        }
        return message.ack();
    }
}

package com.redhat.demo.saga.payment.consumer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.redhat.demo.saga.payment.service.PaymentService;
import io.smallrye.reactive.messaging.kafka.KafkaMessage;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.CompletionStage;

@ApplicationScoped
public class PaymentEventConsumer {

    private static final Logger LOGGER = LoggerFactory.getLogger(PaymentEventConsumer.class);

    @Inject
    PaymentService paymentService;


    private final ObjectMapper objectMapper = new ObjectMapper();

    @Incoming("tickets")
    @Transactional
    public CompletionStage<Void> onMessage(KafkaMessage<String, String> message) throws IOException {

        JsonNode json = objectMapper.readTree(message.getPayload());
        final Optional<String> orderId = message.getHeaders().getOneAsString("correlationid");

        if (orderId.isPresent())
            paymentService.onEventReceived(orderId.get(), json);


        return message.ack();
    }
}

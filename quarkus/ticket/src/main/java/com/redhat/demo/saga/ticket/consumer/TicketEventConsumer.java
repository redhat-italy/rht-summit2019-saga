package com.redhat.demo.saga.ticket.consumer;


import com.redhat.demo.saga.ticket.service.TicketService;
import io.smallrye.reactive.messaging.kafka.KafkaMessage;
import org.eclipse.microprofile.reactive.messaging.Incoming;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.io.IOException;
import java.util.concurrent.CompletionStage;

@ApplicationScoped
public class TicketEventConsumer {

    @Inject
    TicketService ticketService;


    @Incoming("payments")
    public CompletionStage<Void> onMessage(KafkaMessage<String, String> message) throws IOException {
        try {
            //TODO if payment accepted

            //TODO if payment refused

        }
        catch (Throwable t) {

        }
        return message.ack();
    }
}

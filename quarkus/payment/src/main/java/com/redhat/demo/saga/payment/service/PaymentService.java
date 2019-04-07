package com.redhat.demo.saga.payment.service;

import com.redhat.demo.saga.payment.event.PaymentEvent;
import com.redhat.demo.saga.payment.event.PaymentEventType;
import com.redhat.demo.saga.payment.model.Payment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.transaction.Transactional;
import java.time.Instant;

@ApplicationScoped
public class PaymentService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PaymentService.class);

    @Inject
    EntityManager entityManager;

    @Transactional
    public void createPayment(Payment payment) {
        entityManager.persist(payment);
        entityManager.flush();

        PaymentEvent paymentEvent = new PaymentEvent();
        paymentEvent.setCorrelationId(payment.getOrder().getId());
        paymentEvent.setAccountId(String.valueOf(payment.getAccount().getId()));
        paymentEvent.setCreatedOn(Instant.now());
        paymentEvent.setPaymentEventType(PaymentEventType.valueOf(payment.getState().name()));

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

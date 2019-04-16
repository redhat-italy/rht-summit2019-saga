package com.redhat.demo.saga.payment.model;

/**
 * Enumeration class that maps the possible state of the payment
 * 
 * @author Mauro Vocale
 * @version 1.0.0 10/04/2019
 */
public enum PaymentState {

    PAYMENT_ACCEPTED, PAYMENT_REFUSED, PAYMENT_INPROGRESS;
}
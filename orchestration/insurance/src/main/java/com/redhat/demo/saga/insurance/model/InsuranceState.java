package com.redhat.demo.saga.insurance.model;

/**
 * Enumeration class that maps the possible state of the insurance
 * 
 * @author Mauro Vocale
 * @version 1.0.0 10/04/2019
 */
public enum InsuranceState {

    INSURANCE_BOOKED_PENDING, INSURANCE_BOOKED, INSURANCE_PAYMENT_REFUSED;
}

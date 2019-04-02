package com.redhat.demo.saga.payment;

import com.redhat.demo.saga.ticket.PaymentResourceTest;
import io.quarkus.test.junit.SubstrateTest;

@SubstrateTest
public class NativePaymentResourceIT extends PaymentResourceTest {

    // Execute the same tests but in native mode.
}
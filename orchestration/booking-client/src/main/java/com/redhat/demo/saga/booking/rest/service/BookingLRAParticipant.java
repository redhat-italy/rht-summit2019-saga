package com.redhat.demo.saga.booking.rest.service;

import com.redhat.demo.saga.booking.model.Insurance;
import com.redhat.demo.saga.booking.model.InsuranceState;
import com.redhat.demo.saga.booking.model.Payment;
import com.redhat.demo.saga.booking.model.PaymentState;
import com.redhat.demo.saga.booking.model.Ticket;
import com.redhat.demo.saga.booking.model.TicketState;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.eclipse.microprofile.lra.annotation.Compensate;
import org.eclipse.microprofile.lra.annotation.Complete;
import org.eclipse.microprofile.lra.annotation.LRA;
import org.eclipse.microprofile.lra.client.LRAClient;

/**
 * Rest service controller that maps and exposes the service APIs.
 *
 * @author Mauro Vocale
 * @version 10/04/2019
 */
@Path("/book")
@ApplicationScoped
public class BookingLRAParticipant {

    private static final Logger LOG = Logger.getLogger(BookingLRAParticipant.class.getName());
    
    private Client ticketClient;

    private Client insuranceClient;
    
    private Client paymentClient;

    private WebTarget ticketTarget;

    private WebTarget insuranceTarget;
    
    private WebTarget paymentTarget;
    
    @PostConstruct
    private void init() {
        ticketClient = ClientBuilder.newClient();
        insuranceClient = ClientBuilder.newClient();
        paymentClient = ClientBuilder.newClient();
        ticketTarget = 
                ticketClient.target("http://" + System.getenv("TICKET_LRA_THORNTAIL_SERVICE_HOST") 
                        + ":" + System.getenv("TICKET_LRA_THORNTAIL_SERVICE_PORT" ) 
                        + "/tickets");
        insuranceTarget = 
                insuranceClient.target("http://" + System.getenv("INSURANCE_LRA_THORNTAIL_SERVICE_HOST") 
                        + ":" + System.getenv("INSURANCE_LRA_THORNTAIL_SERVICE_PORT" ) 
                        + "/insurances");
        paymentTarget = 
                paymentClient.target("http://" + System.getenv("PAYMENT_LRA_THORNTAIL_SERVICE_HOST") 
                        + ":" + System.getenv("PAYMENT_LRA_THORNTAIL_SERVICE_PORT" ) 
                        + "/payments");
    }

    @PreDestroy
    private void destroy() {
        ticketClient.close();
        insuranceClient.close();
        paymentClient.close();
}

    @GET
    @Produces({MediaType.TEXT_PLAIN})
    public Response status() {
        return Response.ok("Service active").build();
    }

    @POST
    @LRA(value = LRA.Type.REQUIRED,
            cancelOn = {
                Response.Status.INTERNAL_SERVER_ERROR // cancel on a 500 code
            },
            cancelOnFamily = {
                Response.Status.Family.CLIENT_ERROR // cancel on any 4xx code
            })
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    public Response book(@HeaderParam(LRAClient.LRA_HTTP_HEADER) String lraIdUrl, Ticket ticket) {
        /*
         * Perform business actions in the context of the LRA identified by the
         * value in the injected JAX-RS header. This LRA was started just before
         * the method was entered (REQUIRES_NEW) and will be closed when the
         * method finishes at which point the completeWork method below will be
         * invoked.
         */
        
        String lraId = lraIdUrl.substring(lraIdUrl.lastIndexOf('/') + 1);
        LOG.log(Level.INFO, "Value of lraIdUrl in book method {0}", lraId);
        // #### TICKET ####
        ticket.setLraId(lraId);
        LOG.log(Level.INFO, "Value of ticket in book method {0}", ticket);
        Response response
                = ticketTarget.request().post(Entity.entity(
                        ticket, MediaType.APPLICATION_JSON_TYPE));
        
        LOG.log(Level.INFO, "Ticket created with response code {0}", response.getStatus());
        Ticket readEntity = response.readEntity(Ticket.class);
        LOG.log(Level.INFO, "Value of readEntity in book method {0}", readEntity);
        
        // #### INSURANCE ####
        Insurance insurance = new Insurance(readEntity.getOrderId(), InsuranceState.INSURANCE_BOOKED_PENDING, 
                readEntity.getAccountId(), "PROTECT_ALL", readEntity.getTotalCost() / 2, readEntity.getId(), lraId);
        Response responseInsurance
                = insuranceTarget.request().post(Entity.entity(
                        insurance, MediaType.APPLICATION_JSON_TYPE));
        
        // #### PAYMENT ####
        Double orderCost = readEntity.getTotalCost() + insurance.getTotalCost();
        Payment payment = new Payment(readEntity.getOrderId(), orderCost, PaymentState.PAYMENT_INPROGRESS, 
                readEntity.getAccountId(), lraId);
        Response responsePayment
                = paymentTarget.request().post(Entity.entity(
                        payment, MediaType.APPLICATION_JSON_TYPE));
        
        if (orderCost > 100) {
            throw new WebApplicationException("Too Expensive!!!!!! NOT AVAILABLE", Response.Status.NOT_ACCEPTABLE);
        }
        
        return Response.ok(readEntity.getOrderId()).status(201).build();
    }

    @PUT
    @Path("/complete")
    @Complete
    public Response completeBook(@HeaderParam(LRAClient.LRA_HTTP_HEADER) String lraIdUrl) {
        /*
         * Free up resources allocated in the context of the LRA identified by the
         * value in the injected JAX-RS header.
         *
         * Since there is no @Status method in this class completeWork MUST be
         * idempotent and MUST return the status.
         */
        String lraId = lraIdUrl.substring(lraIdUrl.lastIndexOf('/') + 1);
        LOG.log(Level.INFO, "Value of lraId in completeBook method {0}", lraId);
        
        // #### TICKET ####
        Ticket ticket
                = ticketTarget.path("lraId/"
                        + lraId).request().get(Ticket.class);
        ticket.setState(TicketState.TICKET_BOOKED);
        
        ticketTarget.path(ticket.getId().toString()).request().put(
                Entity.entity(ticket, MediaType.APPLICATION_JSON_TYPE));
        
        // #### INSURANCE ####
        Insurance insurance
                = insuranceTarget.path("lraId/"
                        + lraId).request().get(Insurance.class);
        insurance.setState(InsuranceState.INSURANCE_BOOKED);
        
        insuranceTarget.path(insurance.getId().toString()).request().put(
                Entity.entity(insurance, MediaType.APPLICATION_JSON_TYPE));
        
        // #### PAYMENT ####
        Payment payment
                = paymentTarget.path("lraId/"
                        + lraId).request().get(Payment.class);
        payment.setState(PaymentState.PAYMENT_ACCEPTED);
        
        paymentTarget.path(payment.getId().toString()).request().put(
                Entity.entity(payment, MediaType.APPLICATION_JSON_TYPE));
        
        
        return Response.ok("Completed").build();
    }

    @PUT
    @Path("/compensate")
    @Compensate
    public Response compensateWork(@HeaderParam(LRAClient.LRA_HTTP_HEADER) String lraIdUrl) {
        /*
         * The LRA identified by the value in the injected JAX-RS header was
         * cancelled so the business logic should compensate for any actions
         * that have been performed while running in its context.
         *
         * Since there is no @Status method in this class compensateWork MUST be
         * idempotent and MUST return the status
         */
        String lraId = lraIdUrl.substring(lraIdUrl.lastIndexOf('/') + 1);
        LOG.log(Level.INFO, "Value of lraId in compensateWork method {0}", lraId);
        
        // #### TICKET ####
        Ticket ticket
                = ticketTarget.path("lraId/"
                        + lraId).request().get(Ticket.class);
        ticket.setState(TicketState.TICKET_AVAILABLE);
        
        LOG.log(Level.INFO, "Value of ticket id in compensateWork method {0}", ticket.getId());
        
        ticketTarget.path(ticket.getId().toString()).request().put(
                Entity.entity(ticket, MediaType.APPLICATION_JSON_TYPE));
        
        // #### INSURANCE ####
        Insurance insurance
                = insuranceTarget.path("lraId/"
                        + lraId).request().get(Insurance.class);
        insurance.setState(InsuranceState.INSURANCE_PAYMENT_REFUSED);
        
        insuranceTarget.path(insurance.getId().toString()).request().put(
                Entity.entity(insurance, MediaType.APPLICATION_JSON_TYPE));
        
        // #### PAYMENT ####
        Payment payment
                = paymentTarget.path("lraId/"
                        + lraId).request().get(Payment.class);
        payment.setState(PaymentState.PAYMENT_REFUSED);
        
        paymentTarget.path(payment.getId().toString()).request().put(
                Entity.entity(payment, MediaType.APPLICATION_JSON_TYPE));
        
        return Response.ok("COMPENSATED").build();
    }

}

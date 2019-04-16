package com.redhat.demo.saga.payment.rest.service;

import com.redhat.demo.saga.payment.model.Payment;
import javax.enterprise.context.ApplicationScoped;
import javax.json.Json;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.transaction.Transactional;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.eclipse.microprofile.lra.annotation.LRA;

/**
 * Rest service controller that maps and exposes the service APIs.
 * 
 * @author Mauro Vocale
 * @version 10/04/2019
 */
@Path("/payments")
@ApplicationScoped
public class PaymentService {

    @PersistenceContext(unitName = "PaymentPU")
    private EntityManager em;

    @GET
    @Produces({MediaType.APPLICATION_JSON})
    public Payment[] get() {
        return em
                .createNamedQuery("Payment.findAll", Payment.class)
                .getResultList()
                .toArray(new Payment[0]);
    }

    @GET
    @Path("/{id}")
    @Produces({MediaType.APPLICATION_JSON})
    public Payment getSingle(@PathParam("id") Integer id) {
        return em.find(Payment.class, id);
    }
    
    @GET
    @Path("lraId/{id}")
    @Produces({MediaType.APPLICATION_JSON})
    public Payment findByLraId(
            @PathParam("id") String lraId) {
        TypedQuery<Payment> query = em.createQuery(
                "SELECT p FROM Payment p WHERE p.lraId = :lraId",
                Payment.class);
        return query.setParameter("lraId", lraId).getSingleResult();
    }
    
    @GET
    @Path("orderId/{orderId}")
    @Produces({MediaType.APPLICATION_JSON})
    public Payment findByOrderId(
            @PathParam("orderId") String orderId) {
        TypedQuery<Payment> query = em.createQuery(
                "SELECT p FROM Payment p WHERE p.orderId = :orderId",
                Payment.class);
        return query.setParameter("orderId", orderId).getSingleResult();
    }


    @POST
    @Consumes("application/json")
    @Produces("application/json")
    @Transactional
    @LRA(value = LRA.Type.SUPPORTS)
    public Response create(Payment payment) {
        if (payment == null) {
            return error(415, "Invalid payload!");
        }

        if (payment.getAccountId() == null || payment.getAccountId().isEmpty()) {
            return error(422, "The account is required!");
        }

        if (payment.getId() != null) {
            return error(422, "Id was invalidly set on request.");
        }

        try {
            em.persist(payment);
        } catch (Exception e) {
            return error(500, e.getMessage());
        }
        return Response.ok(payment).status(201).build();
    }

    @PUT
    @Path("/{id}")
    @Consumes("application/json")
    @Produces("application/json")
    @Transactional
    public Response update(@PathParam("id") Integer id, Payment payment) {
        if (payment == null) {
            return error(415, "Invalid payload!");
        }

        if (payment.getAccountId() == null || payment.getAccountId().isEmpty()) {
            return error(422, "The account is required!");
        }

        try {
            Payment entity = em.find(Payment.class, id);

            if (entity == null) {
                return error(404, "Ticket with id of " + id + " does not exist.");
            }
            entity.setAccountId(payment.getAccountId());
            entity.setOrderCost(payment.getOrderCost());
            entity.setOrderId(payment.getOrderId());
            entity.setState(payment.getState());
            em.merge(entity);

            return Response.ok(entity).status(200).build();
        } catch (Exception e) {
            return error(500, e.getMessage());
        }
    }


    @DELETE
    @Path("/{id}")
    @Consumes("text/plain")
    @Transactional
    public Response delete(@PathParam("id") Integer id) {
        try {
            Payment entity = em.find(Payment.class, id);
            em.remove(entity);
        } catch (Exception e) {
            return error(500, e.getMessage());
        }
        return Response.status(204).build();
    }

    private Response error(int code, String message) {
        return Response
                .status(code)
                .entity(Json.createObjectBuilder()
                            .add("error", message)
                            .add("code", code)
                            .build()
                )
                .build();
    }
}

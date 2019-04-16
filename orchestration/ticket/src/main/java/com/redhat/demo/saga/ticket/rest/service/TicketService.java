package com.redhat.demo.saga.ticket.rest.service;

import com.redhat.demo.saga.ticket.model.Ticket;
import com.redhat.demo.saga.ticket.model.TicketState;
import java.util.logging.Level;
import java.util.logging.Logger;
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
 * @version 09/04/2019
 */
@Path("/tickets")
@ApplicationScoped
public class TicketService {
    
    private static final Logger LOG = Logger.getLogger(TicketService.class.getName());

    @PersistenceContext(unitName = "TicketPU")
    private EntityManager em;

    @GET
    @Produces("application/json")
    public Ticket[] get() {
        return em
                .createNamedQuery("Ticket.findAll", Ticket.class)
                .getResultList()
                .toArray(new Ticket[0]);
    }

    @GET
    @Path("/{id}")
    @Produces({MediaType.APPLICATION_JSON})
    public Ticket getSingle(@PathParam("id") Integer id) {
        return em.find(Ticket.class, id);
    }
    
    @GET
    @Path("lraId/{id}")
    @Produces({MediaType.APPLICATION_JSON})
    public Ticket findByLraId(
            @PathParam("id") String lraId) {
        TypedQuery<Ticket> query = em.createQuery(
                "SELECT t FROM Ticket t WHERE t.lraId = :lraId",
                Ticket.class);
        return query.setParameter("lraId", lraId).getSingleResult();
    }
    
    @GET
    @Path("orderId/{orderId}")
    @Produces({MediaType.APPLICATION_JSON})
    public Ticket findByOrderId(
            @PathParam("orderId") String orderId) {
        TypedQuery<Ticket> query = em.createQuery(
                "SELECT t FROM Ticket t WHERE t.orderId = :orderId",
                Ticket.class);
        return query.setParameter("orderId", orderId).getSingleResult();
    }

    @POST
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    @Transactional
    @LRA(value = LRA.Type.SUPPORTS)
    public Response create(Ticket ticket) {
        if (ticket == null) {
            return error(415, "Invalid payload!");
        }

        if (ticket.getName() == null || ticket.getName().trim().length() == 0) {
            return error(422, "The name is required!");
        }

        if (ticket.getId() != null) {
            return error(422, "Id was invalidly set on request.");
        }

        try {
            LOG.log(Level.INFO, "Value of ticket in create method {0}", ticket);
            LOG.log(Level.INFO, "Value of lraId in create method {0}", ticket.getLraId());
            ticket.setState(TicketState.TICKET_BOOKED_PENDING);
            em.persist(ticket);
        } catch (Exception e) {
            return error(500, e.getMessage());
        }
        return Response.ok(ticket).status(201).build();
    }

    @PUT
    @Path("/{id}")
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    @Transactional
    @LRA(LRA.Type.SUPPORTS)
    public Response update(@PathParam("id") Integer id, Ticket ticket) {
        if (ticket == null) {
            return error(415, "Invalid payload!");
        }

        if (ticket.getName() == null || ticket.getName().trim().length() == 0) {
            return error(422, "The name is required!");
        }

        try {
            Ticket entity = em.find(Ticket.class, id);

            if (entity == null) {
                return error(404, "Ticket with id of " + id + " does not exist.");
            }
            entity.setAccountId(ticket.getAccountId());
            entity.setName(ticket.getName());
            entity.setNumberOfPersons(ticket.getNumberOfPersons());
            entity.setOrderId(ticket.getOrderId());
            entity.setState(ticket.getState());
            entity.setTotalCost(ticket.getTotalCost());
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
            Ticket entity = em.find(Ticket.class, id);
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

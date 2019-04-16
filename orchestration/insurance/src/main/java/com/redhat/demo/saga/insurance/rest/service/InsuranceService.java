package com.redhat.demo.saga.insurance.rest.service;

import com.redhat.demo.saga.insurance.model.Insurance;
import com.redhat.demo.saga.insurance.model.InsuranceState;
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
@Path("/insurances")
@ApplicationScoped
public class InsuranceService {

    @PersistenceContext(unitName = "InsurancePU")
    private EntityManager em;

    @GET
    @Produces("application/json")
    public Insurance[] get() {
        return em
                .createNamedQuery("Insurance.findAll", Insurance.class)
                .getResultList()
                .toArray(new Insurance[0]);
    }

    @GET
    @Path("/{id}")
    @Produces("application/json")
    public Insurance getSingle(@PathParam("id") Integer id) {
        return em.find(Insurance.class, id);
    }
    
    @GET
    @Path("lraId/{id}")
    @Produces({MediaType.APPLICATION_JSON})
    public Insurance findByLraId(
            @PathParam("id") String lraId) {
        TypedQuery<Insurance> query = em.createQuery(
                "SELECT i FROM Insurance i WHERE i.lraId = :lraId",
                Insurance.class);
        return query.setParameter("lraId", lraId).getSingleResult();
    }
    
    @GET
    @Path("orderId/{orderId}")
    @Produces({MediaType.APPLICATION_JSON})
    public Insurance findByOrderId(
            @PathParam("orderId") String orderId) {
        TypedQuery<Insurance> query = em.createQuery(
                "SELECT i FROM Insurance i WHERE i.orderId = :orderId",
                Insurance.class);
        return query.setParameter("orderId", orderId).getSingleResult();
    }


    @POST
    @Consumes("application/json")
    @Produces("application/json")
    @Transactional
    @LRA(value = LRA.Type.SUPPORTS)
    public Response create(Insurance insurance) {
        if (insurance == null) {
            return error(415, "Invalid payload!");
        }

        if (insurance.getName() == null || insurance.getName().trim().length() == 0) {
            return error(422, "The name is required!");
        }

        if (insurance.getId() != null) {
            return error(422, "Id was invalidly set on request.");
        }

        try {
            insurance.setState(InsuranceState.INSURANCE_BOOKED_PENDING);
            em.persist(insurance);
        } catch (Exception e) {
            return error(500, e.getMessage());
        }
        return Response.ok(insurance).status(201).build();
    }

    @PUT
    @Path("/{id}")
    @Consumes("application/json")
    @Produces("application/json")
    @Transactional
    public Response update(@PathParam("id") Integer id, Insurance insurance) {
        if (insurance == null) {
            return error(415, "Invalid payload!");
        }

        if (insurance.getName() == null || insurance.getName().trim().length() == 0) {
            return error(422, "The name is required!");
        }

        try {
            Insurance entity = em.find(Insurance.class, id);

            if (entity == null) {
                return error(404, "Insurance with id of " + id + " does not exist.");
            }
            entity.setAccountId(insurance.getAccountId());
            entity.setName(insurance.getName());
            entity.setOrderId(insurance.getOrderId());
            entity.setState(insurance.getState());
            entity.setTotalCost(insurance.getTotalCost());
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
            Insurance entity = em.find(Insurance.class, id);
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

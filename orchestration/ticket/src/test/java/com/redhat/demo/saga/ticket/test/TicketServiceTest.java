package com.redhat.demo.saga.ticket.test;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.redhat.demo.saga.ticket.model.Ticket;
import com.redhat.demo.saga.ticket.model.TicketState;
import com.redhat.demo.saga.ticket.rest.RestApplication;
import com.redhat.demo.saga.ticket.rest.service.TicketService;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.ClassLoaderAsset;
import org.wildfly.swarm.jaxrs.JAXRSArchive;

/**
 * Arquillian test class needed to implement the unit tests on top of Thorntail fat
 * jar environment.
 * 
 * @author Mauro Vocale
 * @version 1.0.0 09/04/2019
 */
@RunWith(Arquillian.class)
public class TicketServiceTest {
    
    /**
     *
     * @return @throws Exception
     */
    @Deployment
    public static Archive createDeployment() throws Exception {
        JAXRSArchive deployment = ShrinkWrap.create(JAXRSArchive.class);
        deployment.addPackage(Ticket.class.getPackage());
        deployment.addPackage(TicketState.class.getPackage());
        deployment.addPackage(RestApplication.class.getPackage());
        deployment.addPackage(TicketService.class.getPackage());


        deployment.addAsWebInfResource(new ClassLoaderAsset(
                "META-INF/load.sql", TicketService.class.
                        getClassLoader()),
                "classes/META-INF/load.sql");

        deployment.addAsWebInfResource(new ClassLoaderAsset(
                "META-INF/persistence.xml", TicketService.class.
                        getClassLoader()),
                "classes/META-INF/persistence.xml");

        deployment.addAsWebInfResource(new ClassLoaderAsset(
                "project-local.yml", TicketService.class.
                        getClassLoader()),
                "classes/project-local.yml");

        deployment.addAllDependencies();
        System.out.println(deployment.toString(true));
        return deployment;
}

    @Test
    @RunAsClient
    public void test_list_tickets() throws InterruptedException {
        Client client = ClientBuilder.newClient();
        WebTarget target = client.target("http://127.0.0.1:8080")
                .path("/tickets");

        Response response = target.request(MediaType.APPLICATION_JSON).get();
        Assert.assertEquals(200, response.getStatus());
        JsonArray values = Json.parse(response.readEntity(String.class)).asArray();
        Assert.assertTrue(values.size() > 0);
    }

    @Test
    @RunAsClient
    public void test_ticket_by_id() {
        Client client = ClientBuilder.newClient();
        WebTarget target = client.target("http://localhost:8080")
                .path("/tickets")
                .path("/1"); // ticket by ID

        Response response = target.request(MediaType.APPLICATION_JSON).get();
        Assert.assertEquals(200, response.getStatus());
        JsonObject value = Json.parse(response.readEntity(String.class)).asObject();
        Assert.assertTrue(value.get("name").asString()
                .equalsIgnoreCase("Live Depeche Mode - Rome 12 june 2019"));
    }

    @Test
    @RunAsClient
    public void test_create_ticket() {
        Ticket ticket = new Ticket("127", TicketState.TICKET_BOOKED, "AA5", 
                "ELTON JOHN - Verona 29 May 2019", "1", Double.valueOf("50"), null);
        createNewTicket(ticket);
    }

    private JsonObject createNewTicket(Ticket ticket) {
        Client client = ClientBuilder.newClient();
        WebTarget target = client.target("http://localhost:8080")
                .path("/tickets");

        Response response = target.request(MediaType.APPLICATION_JSON)
                .post(Entity.entity(ticket, MediaType.APPLICATION_JSON));
        Assert.assertEquals(201, response.getStatus());
        JsonObject value = Json.parse(response.readEntity(String.class)).asObject();
        Assert.assertTrue(value.get("name").asString().equals(ticket.getName()));
        return value;
    }

    @Test
    @RunAsClient
    public void test_modify_ticket() {
        Ticket ticket = new Ticket("128", TicketState.TICKET_BOOKED, "AA6", 
                "TAKE THAT - Rome 28 Jube 2019", "1", Double.valueOf("90"), null);
        JsonObject myTicket = createNewTicket(ticket);
        Integer id = myTicket.get("id").asInt();

        Client client = ClientBuilder.newClient();
        WebTarget target = client.target("http://localhost:8080")
                .path("/tickets")
                .path(String.valueOf(id));

        ticket.setTotalCost(Double.valueOf("85"));

        Response response = target.request(MediaType.APPLICATION_JSON)
                .put(Entity.entity(
                        ticket,
                        MediaType.APPLICATION_JSON)
                );
        Assert.assertEquals(200, response.getStatus());
        JsonObject value = Json.parse(response.readEntity(String.class)).asObject();
        Assert.assertTrue(value.get("totalCost").asDouble() == 85);
        Assert.assertTrue(value.get("id").asInt() == id);
    }

}

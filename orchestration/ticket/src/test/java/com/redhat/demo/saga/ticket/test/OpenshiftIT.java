package com.redhat.demo.saga.ticket.test;

import java.io.StringReader;
import java.io.StringWriter;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonValue;
import javax.json.JsonWriter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.redhat.demo.saga.ticket.model.Ticket;
import com.redhat.demo.saga.ticket.model.TicketState;
import io.restassured.RestAssured;
import io.restassured.filter.log.LogDetail;
import io.restassured.http.ContentType;
import org.arquillian.cube.openshift.impl.enricher.AwaitRoute;
import org.arquillian.cube.openshift.impl.enricher.RouteURL;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static io.restassured.RestAssured.delete;
import static io.restassured.RestAssured.get;
import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.when;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;

/**
 * Arquillian test class needed to implement the unit tests on top of Openshift
 * environment.
 * 
 * @author Mauro Vocale
 * @version 1.0.0 09/04/2019
 */
@RunWith(Arquillian.class)
public class OpenshiftIT {

    @RouteURL(value = "${app.name}", path = "/tickets")
    @AwaitRoute(path = "/tickets")
    private String url;

    @Before
    public void setup() throws Exception {
        RestAssured.baseURI = url;

        String jsonData = when()
                .get()
                .then()
                .extract().asString();
        
        System.out.println("Valore di jsonData in setup: " + jsonData);

        JsonArray array = Json.createReader(new StringReader(jsonData)).readArray();
        array.forEach(val -> delete("/" + ((JsonObject) val).getInt("id")));
    }

    @Test
    public void testRetrieveNoTicket() {
        get()
                .then()
                .assertThat().statusCode(200)
                .body(is("[]"));
    }

    @Test
    public void testWithOneTicket() throws Exception {
        
        Ticket ticket = new Ticket("128", TicketState.TICKET_BOOKED, "AA7", 
                "Madonna - London 20 May 2020", "1", Double.valueOf("100"), null);
        createTicket(ticket);

        String payload = get()
                .then()
                .assertThat().statusCode(200)
                .extract().asString();

        JsonArray array = Json.createReader(new StringReader(payload)).readArray();

        assertThat(array).hasSize(1);
        assertThat(array.get(0).getValueType()).isEqualTo(JsonValue.ValueType.OBJECT);

        JsonObject obj = (JsonObject) array.get(0);
        assertThat(obj.getInt("id")).isNotNull().isGreaterThan(0);

        given()
                .pathParam("id", obj.getInt("id"))
                .when()
                .get("/{id}")
                .then()
                .assertThat().statusCode(200)
                .body(containsString("Madonna"));
    }

    @Test
    public void testCreateTicket() {
        
        Ticket ticket = new Ticket("129", TicketState.TICKET_BOOKED, "AA8", 
                "Kate Perry - New York 20 September 2020", "1", Double.valueOf("85"), null);
        
        String payload = given()
                .contentType(ContentType.JSON)
                .body(convert(Json.createObjectBuilder()
                        .add("orderId", ticket.getOrderId())
                        .add("state", ticket.getState().toString())
                        .add("accountId", ticket.getAccountId())
                        .add("name", ticket.getName())
                        .add("numberOfPersons", ticket.getNumberOfPersons())
                        .add("totalCost", ticket.getTotalCost())
                        .build()))
                .post()
                .then().assertThat().statusCode(201)
                .extract().asString();

        JsonObject obj = Json.createReader(new StringReader(payload)).readObject();
        assertThat(obj).isNotNull();
        assertThat(obj.getInt("id")).isNotNull().isGreaterThan(0);
        assertThat(obj.getString("name")).isNotNull().isEqualTo("Kate Perry - New York 20 September 2020");
    }

    @Test
    public void testCreateInvalidPayload() {
        given()
                .contentType(ContentType.TEXT)
                .body("")
                .post()
                .then()
                .assertThat().statusCode(415);
    }

    @Test
    public void testCreateIllegalPayload() {
        
        Ticket badTicket = new Ticket("130", TicketState.TICKET_BOOKED, "AA9", 
                null, "1", Double.valueOf("85"), null);

        String payload = given()
                .contentType(ContentType.JSON)
                .body(badTicket)
                .post()
                .then()
                .assertThat().statusCode(422)
                .extract().asString();

        JsonObject obj = Json.createReader(new StringReader(payload)).readObject();
        assertThat(obj).isNotNull();
        assertThat(obj.getString("error")).isNotNull();
        assertThat(obj.getInt("code")).isNotNull().isEqualTo(422);
    }

    @Test
    public void testUpdate() throws Exception {
        
        Ticket ticket = new Ticket("131", TicketState.TICKET_BOOKED, "AA10", 
                "Maroon 5 - Paris 20 July 2020", "1", Double.valueOf("75"), null);
        
        Ticket myTicket = createTicket(ticket);

        String response = given()
                .pathParam("id", myTicket.getId())
                .when()
                .get("/{id}")
                .then()
                .assertThat().statusCode(200)
                .extract().asString();

        myTicket = new ObjectMapper().readValue(response, Ticket.class);

        myTicket.setName("Maroon 5 - Paris 22 July 2020");

        response = given()
                .pathParam("id", myTicket.getId())
                .contentType(ContentType.JSON)
                .body(new ObjectMapper().writeValueAsString(myTicket))
                .when()
                .put("/{id}")
                .then()
                .assertThat().statusCode(200)
                .extract().asString();

        Ticket updatedTicket = new ObjectMapper().readValue(response, Ticket.class);

        assertThat(myTicket.getId()).isEqualTo(updatedTicket.getId());
        assertThat(updatedTicket.getName()).isEqualTo("Maroon 5 - Paris 22 July 2020");
    }

    @Test
    public void testUpdateWithUnknownId() throws Exception {
        Ticket badTicket = new Ticket();
        badTicket.setName("Kate Perry - New York 20 September 2020");
        badTicket.setId(Integer.valueOf("12345678"));

        given()
                .pathParam("id", badTicket.getId())
                .contentType(ContentType.JSON)
                .body(new ObjectMapper().writeValueAsString(badTicket))
                .when()
                .put("/{id}")
                .then()
                .assertThat().statusCode(404)
                .extract().asString();
    }

    @Test
    public void testUpdateInvalidPayload() {
        given()
                .contentType(ContentType.TEXT)
                .body("")
                .post()
                .then()
                .assertThat().statusCode(415);
    }

    @Test
    public void testUpdateIllegalPayload() throws Exception {
        Ticket invalidTicket = new Ticket();
        System.out.println(invalidTicket.getId());
        invalidTicket.setId(Integer.decode("1"));
        invalidTicket.setName(null);

        String payload = given()
                .pathParam("id", invalidTicket.getId())
                .contentType(ContentType.JSON)
                .body(new ObjectMapper().writeValueAsString(invalidTicket))
                .when()
                .put("/{id}")
                .then()
                .assertThat().statusCode(422)
                .extract().asString();

        JsonObject obj = Json.createReader(new StringReader(payload)).readObject();
        assertThat(obj).isNotNull();
        assertThat(obj.getString("error")).isNotNull();
        System.out.println(obj.getString("error"));
        assertThat(obj.getInt("code")).isNotNull().isEqualTo(422);
    }

    @Test
    public void testDelete() throws Exception {
         Ticket ticket = new Ticket("132", TicketState.TICKET_BOOKED, "AA11", 
                "Aerosmith - Barcelona 20 October 2020", "1", Double.valueOf("115"), null);
        Ticket myTicket = createTicket(ticket);

        delete("/" + myTicket.getId())
                .then()
                .assertThat().statusCode(204);

        get()
                .then()
                .assertThat().statusCode(200)
                .body(is("[]"));
    }

    @Test
    public void testDeleteWithUnknownId() {
        delete("/unknown")
                .then()
                .assertThat().statusCode(404);

        get()
                .then()
                .assertThat().statusCode(200)
                .body(is("[]"));
    }

    private Ticket createTicket(Ticket ticket) throws Exception {
        String payload = given()
                .contentType(ContentType.JSON)
                .body(convert(Json.createObjectBuilder()
                        .add("orderId", ticket.getOrderId())
                        .add("state", ticket.getState().toString())
                        .add("accountId", ticket.getAccountId())
                        .add("name", ticket.getName())
                        .add("numberOfPersons", ticket.getNumberOfPersons())
                        .add("totalCost", ticket.getTotalCost())
                        .build()))
                .post()
                .then().log().ifValidationFails(LogDetail.ALL)
                .assertThat().statusCode(201)
                .extract().asString();

        JsonObject obj = Json.createReader(new StringReader(payload)).readObject();
        assertThat(obj).isNotNull();
        assertThat(obj.getInt("id")).isNotNull().isGreaterThan(0);

        return new ObjectMapper().readValue(payload, Ticket.class);
    }

    private String convert(JsonObject object) {
        StringWriter stWriter = new StringWriter();
        try (JsonWriter jsonWriter = Json.createWriter(stWriter)) {
            jsonWriter.writeObject(object);
        }

        return stWriter.toString();
    }
}


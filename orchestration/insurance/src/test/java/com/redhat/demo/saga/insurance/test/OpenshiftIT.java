package com.redhat.demo.saga.insurance.test;

import java.io.StringReader;
import java.io.StringWriter;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonValue;
import javax.json.JsonWriter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.redhat.demo.saga.insurance.model.Insurance;
import com.redhat.demo.saga.insurance.model.InsuranceState;
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
 * @version 1.0.0 10/04/2019
 */
@RunWith(Arquillian.class)
public class OpenshiftIT {

    @RouteURL(value = "${app.name}", path = "/insurances")
    @AwaitRoute(path = "/insurances")
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
    public void testRetrieveNoInsurance() {
        get()
                .then()
                .assertThat().statusCode(200)
                .body(is("[]"));
    }

    @Test
    public void testWithOneInsurance() throws Exception {
        
        Insurance insurance = new Insurance("128", InsuranceState.INSURANCE_BOOKED, 
                "AA7", "PROTECT_ALL", Double.valueOf("100"), 4, null);
        
        createInsurance(insurance);

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
                .body(containsString("PROTECT_ALL"));
    }

    @Test
    public void testCreateInsurance() {
        
        Insurance insurance = new Insurance("129", InsuranceState.INSURANCE_BOOKED, "AA8", 
                "PROTECT_EVENT_DELAYED", Double.valueOf("85"), 5, null);
        
        String payload = given()
                .contentType(ContentType.JSON)
                .body(convert(Json.createObjectBuilder()
                        .add("orderId", insurance.getOrderId())
                        .add("state", insurance.getState().toString())
                        .add("accountId", insurance.getAccountId())
                        .add("name", insurance.getName())
                        .add("totalCost", insurance.getTotalCost())
                        .add("ticketId", insurance.getTicketId())
                        .build()))
                .post()
                .then().assertThat().statusCode(201)
                .extract().asString();

        JsonObject obj = Json.createReader(new StringReader(payload)).readObject();
        assertThat(obj).isNotNull();
        assertThat(obj.getInt("id")).isNotNull().isGreaterThan(0);
        assertThat(obj.getString("name")).isNotNull().isEqualTo("PROTECT_EVENT_DELAYED");
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
        
        Insurance badInsurance = new Insurance("130", InsuranceState.INSURANCE_BOOKED, "AA9", 
                null, Double.valueOf("85"), 6, null);

        String payload = given()
                .contentType(ContentType.JSON)
                .body(badInsurance)
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
        
        Insurance insurance = new Insurance("131", InsuranceState.INSURANCE_BOOKED, "AA10", 
                "PROTECT_EVENT_DELAYED", Double.valueOf("75"), 7, null);
        
        Insurance myTicket = createInsurance(insurance);

        String response = given()
                .pathParam("id", myTicket.getId())
                .when()
                .get("/{id}")
                .then()
                .assertThat().statusCode(200)
                .extract().asString();

        myTicket = new ObjectMapper().readValue(response, Insurance.class);

        myTicket.setName("PROTECT_EVENT_DELAYED_PLATINUM");

        response = given()
                .pathParam("id", myTicket.getId())
                .contentType(ContentType.JSON)
                .body(new ObjectMapper().writeValueAsString(myTicket))
                .when()
                .put("/{id}")
                .then()
                .assertThat().statusCode(200)
                .extract().asString();

        Insurance updatedTicket = new ObjectMapper().readValue(response, Insurance.class);

        assertThat(myTicket.getId()).isEqualTo(updatedTicket.getId());
        assertThat(updatedTicket.getName()).isEqualTo("PROTECT_EVENT_DELAYED_PLATINUM");
    }

    @Test
    public void testUpdateWithUnknownId() throws Exception {
        Insurance badInsurance = new Insurance();
        badInsurance.setName("PROTECT_EVENT_DELAYED");
        badInsurance.setId(Integer.valueOf("12345678"));

        given()
                .pathParam("id", badInsurance.getId())
                .contentType(ContentType.JSON)
                .body(new ObjectMapper().writeValueAsString(badInsurance))
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
        Insurance invalidInsurance = new Insurance();
        System.out.println(invalidInsurance.getId());
        invalidInsurance.setId(Integer.decode("1"));
        invalidInsurance.setName(null);

        String payload = given()
                .pathParam("id", invalidInsurance.getId())
                .contentType(ContentType.JSON)
                .body(new ObjectMapper().writeValueAsString(invalidInsurance))
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
         Insurance insurance = new Insurance("132", InsuranceState.INSURANCE_BOOKED, "AA11", 
                "PROTECT_ALL", Double.valueOf("115"), 8, null);
        Insurance myTicket = createInsurance(insurance);

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

    private Insurance createInsurance(Insurance insurance) throws Exception {
        String payload = given()
                .contentType(ContentType.JSON)
                .body(convert(Json.createObjectBuilder()
                        .add("orderId", insurance.getOrderId())
                        .add("state", insurance.getState().toString())
                        .add("accountId", insurance.getAccountId())
                        .add("name", insurance.getName())
                        .add("totalCost", insurance.getTotalCost())
                        .add("ticketId", insurance.getTicketId())
                        .build()))
                .post()
                .then().log().ifValidationFails(LogDetail.ALL)
                .assertThat().statusCode(201)
                .extract().asString();

        JsonObject obj = Json.createReader(new StringReader(payload)).readObject();
        assertThat(obj).isNotNull();
        assertThat(obj.getInt("id")).isNotNull().isGreaterThan(0);

        return new ObjectMapper().readValue(payload, Insurance.class);
    }

    private String convert(JsonObject object) {
        StringWriter stWriter = new StringWriter();
        try (JsonWriter jsonWriter = Json.createWriter(stWriter)) {
            jsonWriter.writeObject(object);
        }

        return stWriter.toString();
    }
}


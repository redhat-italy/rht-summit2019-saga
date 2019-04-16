package com.redhat.demo.saga.payment.test;

import java.io.StringReader;
import java.io.StringWriter;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonValue;
import javax.json.JsonWriter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.redhat.demo.saga.payment.model.Payment;
import com.redhat.demo.saga.payment.model.PaymentState;
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

    @RouteURL(value = "${app.name}", path = "/payments")
    @AwaitRoute(path = "/payments")
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
    public void testRetrieveNoPayment() {
        get()
                .then()
                .assertThat().statusCode(200)
                .body(is("[]"));
    }

    @Test
    public void testWithOnePayment() throws Exception {
        
        Payment payment = new Payment("128",  Double.valueOf("100"), PaymentState.PAYMENT_ACCEPTED, "AA7", null);
        
        createPayment(payment);

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
                .body(containsString("AA7"));
    }

    @Test
    public void testCreatePayment() {
        
        Payment payment = new Payment("129", Double.valueOf("85"), PaymentState.PAYMENT_ACCEPTED, "AA8", null);
        
        String payload = given()
                .contentType(ContentType.JSON)
                .body(convert(Json.createObjectBuilder()
                        .add("orderId", payment.getOrderId())
                        .add("orderCost", payment.getOrderCost())
                        .add("state", payment.getState().toString())
                        .add("accountId", payment.getAccountId())
                        .build()))
                .post()
                .then().assertThat().statusCode(201)
                .extract().asString();

        JsonObject obj = Json.createReader(new StringReader(payload)).readObject();
        assertThat(obj).isNotNull();
        assertThat(obj.getInt("id")).isNotNull().isGreaterThan(0);
        assertThat(obj.getString("accountId")).isNotNull().isEqualTo("AA8");
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
        
        Payment badPayment = new Payment("130",  Double.valueOf("85"), PaymentState.PAYMENT_ACCEPTED, null, null);

        String payload = given()
                .contentType(ContentType.JSON)
                .body(badPayment)
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
        
        Payment payment = new Payment("131", Double.valueOf("75"), PaymentState.PAYMENT_ACCEPTED, "AA10", null);
        
        Payment myPayment = createPayment(payment);

        String response = given()
                .pathParam("id", myPayment.getId())
                .when()
                .get("/{id}")
                .then()
                .assertThat().statusCode(200)
                .extract().asString();

        myPayment = new ObjectMapper().readValue(response, Payment.class);

        myPayment.setAccountId("AA11");

        response = given()
                .pathParam("id", myPayment.getId())
                .contentType(ContentType.JSON)
                .body(new ObjectMapper().writeValueAsString(myPayment))
                .when()
                .put("/{id}")
                .then()
                .assertThat().statusCode(200)
                .extract().asString();

        Payment updatedPayment = new ObjectMapper().readValue(response, Payment.class);

        assertThat(myPayment.getId()).isEqualTo(updatedPayment.getId());
        assertThat(updatedPayment.getAccountId()).isEqualTo(myPayment.getAccountId());
    }

    @Test
    public void testUpdateWithUnknownId() throws Exception {
        Payment badPayment = new Payment();
        badPayment.setAccountId("AA12");
        badPayment.setId(Integer.valueOf("12345678"));

        given()
                .pathParam("id", badPayment.getId())
                .contentType(ContentType.JSON)
                .body(new ObjectMapper().writeValueAsString(badPayment))
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
        Payment invalidPayment = new Payment();
        invalidPayment.setId(Integer.decode("1"));
        invalidPayment.setAccountId(null);

        String payload = given()
                .pathParam("id", invalidPayment.getId())
                .contentType(ContentType.JSON)
                .body(new ObjectMapper().writeValueAsString(invalidPayment))
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
        Payment payment = new Payment("132", Double.valueOf("115"), PaymentState.PAYMENT_ACCEPTED, "AA15", null);
        Payment myTicket = createPayment(payment);

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

    private Payment createPayment(Payment payment) throws Exception {
        String payload = given()
                .contentType(ContentType.JSON)
                .body(convert(Json.createObjectBuilder()
                        .add("orderId", payment.getOrderId())
                        .add("orderCost", payment.getOrderCost())
                        .add("state", payment.getState().toString())
                        .add("accountId", payment.getAccountId())
                        .build()))
                .post()
                .then().log().ifValidationFails(LogDetail.ALL)
                .assertThat().statusCode(201)
                .extract().asString();

        JsonObject obj = Json.createReader(new StringReader(payload)).readObject();
        assertThat(obj).isNotNull();
        assertThat(obj.getInt("id")).isNotNull().isGreaterThan(0);

        return new ObjectMapper().readValue(payload, Payment.class);
    }

    private String convert(JsonObject object) {
        StringWriter stWriter = new StringWriter();
        try (JsonWriter jsonWriter = Json.createWriter(stWriter)) {
            jsonWriter.writeObject(object);
        }

        return stWriter.toString();
    }
}


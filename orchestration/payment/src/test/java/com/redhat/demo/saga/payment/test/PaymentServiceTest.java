package com.redhat.demo.saga.payment.test;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.redhat.demo.saga.payment.model.Payment;
import com.redhat.demo.saga.payment.model.PaymentState;
import com.redhat.demo.saga.payment.rest.RestApplication;
import com.redhat.demo.saga.payment.rest.service.PaymentService;
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
 * @version 1.0.0 10/04/2019
 */
@RunWith(Arquillian.class)
public class PaymentServiceTest {
    
    /**
     *
     * @return @throws Exception
     */
    @Deployment
    public static Archive createDeployment() throws Exception {
        JAXRSArchive deployment = ShrinkWrap.create(JAXRSArchive.class);
        deployment.addPackage(Payment.class.getPackage());
        deployment.addPackage(PaymentState.class.getPackage());
        deployment.addPackage(RestApplication.class.getPackage());
        deployment.addPackage(PaymentService.class.getPackage());


        deployment.addAsWebInfResource(new ClassLoaderAsset(
                "META-INF/load.sql", PaymentService.class.
                        getClassLoader()),
                "classes/META-INF/load.sql");

        deployment.addAsWebInfResource(new ClassLoaderAsset(
                "META-INF/persistence.xml", PaymentService.class.
                        getClassLoader()),
                "classes/META-INF/persistence.xml");

        deployment.addAsWebInfResource(new ClassLoaderAsset(
                "project-local.yml", PaymentService.class.
                        getClassLoader()),
                "classes/project-local.yml");

        deployment.addAllDependencies();
        System.out.println(deployment.toString(true));
        return deployment;
}

    @Test
    @RunAsClient
    public void test_list_payments() throws InterruptedException {
        Client client = ClientBuilder.newClient();
        WebTarget target = client.target("http://127.0.0.1:8080")
                .path("/payments");

        Response response = target.request(MediaType.APPLICATION_JSON).get();
        Assert.assertEquals(200, response.getStatus());
        JsonArray values = Json.parse(response.readEntity(String.class)).asArray();
        Assert.assertTrue(values.size() > 0);
    }

    @Test
    @RunAsClient
    public void test_payment_by_id() {
        Client client = ClientBuilder.newClient();
        WebTarget target = client.target("http://localhost:8080")
                .path("/payments")
                .path("/1"); // payment by ID

        Response response = target.request(MediaType.APPLICATION_JSON).get();
        Assert.assertEquals(200, response.getStatus());
        JsonObject value = Json.parse(response.readEntity(String.class)).asObject();
        Assert.assertTrue(value.get("accountId").asString()
                .equalsIgnoreCase("AA2"));
    }

    @Test
    @RunAsClient
    public void test_create_payment() {
        Payment payment = new Payment("127",  Double.valueOf("50"), PaymentState.PAYMENT_ACCEPTED, "AA5", null);
        createNewPayment(payment);
    }

    private JsonObject createNewPayment(Payment insurance) {
        Client client = ClientBuilder.newClient();
        WebTarget target = client.target("http://localhost:8080")
                .path("/payments");

        Response response = target.request(MediaType.APPLICATION_JSON)
                .post(Entity.entity(insurance, MediaType.APPLICATION_JSON));
        Assert.assertEquals(201, response.getStatus());
        JsonObject value = Json.parse(response.readEntity(String.class)).asObject();
        Assert.assertTrue(value.get("accountId").asString().equals(insurance.getAccountId()));
        return value;
    }

    @Test
    @RunAsClient
    public void test_modify_payment() {
        Payment payment = new Payment("128", Double.valueOf("90"), PaymentState.PAYMENT_ACCEPTED, "AA6", null);
        JsonObject myPayment = createNewPayment(payment);
        Integer id = myPayment.get("id").asInt();

        Client client = ClientBuilder.newClient();
        WebTarget target = client.target("http://localhost:8080")
                .path("/payments")
                .path(String.valueOf(id));

        payment.setOrderCost(Double.valueOf("85"));

        Response response = target.request(MediaType.APPLICATION_JSON)
                .put(Entity.entity(payment,
                        MediaType.APPLICATION_JSON)
                );
        Assert.assertEquals(200, response.getStatus());
        JsonObject value = Json.parse(response.readEntity(String.class)).asObject();
        Assert.assertTrue(value.get("orderCost").asDouble() == 85);
        Assert.assertTrue(value.get("id").asInt() == id);
    }

}

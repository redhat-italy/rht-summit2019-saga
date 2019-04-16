package com.redhat.demo.saga.insurance.test;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.redhat.demo.saga.insurance.model.Insurance;
import com.redhat.demo.saga.insurance.model.InsuranceState;
import com.redhat.demo.saga.insurance.rest.RestApplication;
import com.redhat.demo.saga.insurance.rest.service.InsuranceService;
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
public class InsuranceServiceTest {
    
    /**
     *
     * @return @throws Exception
     */
    @Deployment
    public static Archive createDeployment() throws Exception {
        JAXRSArchive deployment = ShrinkWrap.create(JAXRSArchive.class);
        deployment.addPackage(Insurance.class.getPackage());
        deployment.addPackage(InsuranceState.class.getPackage());
        deployment.addPackage(RestApplication.class.getPackage());
        deployment.addPackage(InsuranceService.class.getPackage());


        deployment.addAsWebInfResource(new ClassLoaderAsset(
                "META-INF/load.sql", InsuranceService.class.
                        getClassLoader()),
                "classes/META-INF/load.sql");

        deployment.addAsWebInfResource(new ClassLoaderAsset(
                "META-INF/persistence.xml", InsuranceService.class.
                        getClassLoader()),
                "classes/META-INF/persistence.xml");

        deployment.addAsWebInfResource(new ClassLoaderAsset(
                "project-local.yml", InsuranceService.class.
                        getClassLoader()),
                "classes/project-local.yml");

        deployment.addAllDependencies();
        System.out.println(deployment.toString(true));
        return deployment;
}

    @Test
    @RunAsClient
    public void test_list_insurances() throws InterruptedException {
        Client client = ClientBuilder.newClient();
        WebTarget target = client.target("http://127.0.0.1:8080")
                .path("/insurances");

        Response response = target.request(MediaType.APPLICATION_JSON).get();
        Assert.assertEquals(200, response.getStatus());
        JsonArray values = Json.parse(response.readEntity(String.class)).asArray();
        Assert.assertTrue(values.size() > 0);
    }

    @Test
    @RunAsClient
    public void test_insurance_by_id() {
        Client client = ClientBuilder.newClient();
        WebTarget target = client.target("http://localhost:8080")
                .path("/insurances")
                .path("/1"); // insurance by ID

        Response response = target.request(MediaType.APPLICATION_JSON).get();
        Assert.assertEquals(200, response.getStatus());
        JsonObject value = Json.parse(response.readEntity(String.class)).asObject();
        Assert.assertTrue(value.get("name").asString()
                .equalsIgnoreCase("PROTECT_ALL"));
    }

    @Test
    @RunAsClient
    public void test_create_insurance() {
        Insurance insurance = new Insurance("127", InsuranceState.INSURANCE_BOOKED, "AA5", 
                "PROTECT_ALL", Double.valueOf("50"), 7, null);
        createNewInsurance(insurance);
    }

    private JsonObject createNewInsurance(Insurance insurance) {
        Client client = ClientBuilder.newClient();
        WebTarget target = client.target("http://localhost:8080")
                .path("/insurances");

        Response response = target.request(MediaType.APPLICATION_JSON)
                .post(Entity.entity(insurance, MediaType.APPLICATION_JSON));
        Assert.assertEquals(201, response.getStatus());
        JsonObject value = Json.parse(response.readEntity(String.class)).asObject();
        Assert.assertTrue(value.get("name").asString().equals(insurance.getName()));
        return value;
    }

    @Test
    @RunAsClient
    public void test_modify_insurance() {
        Insurance insurance = new Insurance("128", InsuranceState.INSURANCE_BOOKED, "AA6", 
                "PROTECT_ALL", Double.valueOf("90"), 8, null);
        JsonObject myInsurance = createNewInsurance(insurance);
        Integer id = myInsurance.get("id").asInt();

        Client client = ClientBuilder.newClient();
        WebTarget target = client.target("http://localhost:8080")
                .path("/insurances")
                .path(String.valueOf(id));

        insurance.setTotalCost(Double.valueOf("85"));

        Response response = target.request(MediaType.APPLICATION_JSON)
                .put(Entity.entity(insurance,
                        MediaType.APPLICATION_JSON)
                );
        Assert.assertEquals(200, response.getStatus());
        JsonObject value = Json.parse(response.readEntity(String.class)).asObject();
        Assert.assertTrue(value.get("totalCost").asDouble() == 85);
        Assert.assertTrue(value.get("id").asInt() == id);
    }

}

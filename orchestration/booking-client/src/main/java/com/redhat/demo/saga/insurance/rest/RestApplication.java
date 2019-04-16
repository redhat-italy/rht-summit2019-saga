package com.redhat.demo.saga.insurance.rest;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

/**
 * Application class needed to enable the JAX-RS APIs exposure.
 * 
 * @author Mauro Vocale
 * @version 1.0.0 10/04/2019
 */
@ApplicationPath("/")
public class RestApplication extends Application {
}

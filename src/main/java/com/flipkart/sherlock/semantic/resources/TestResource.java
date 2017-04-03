package com.flipkart.sherlock.semantic.resources;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

/**
 * Created by anurag.laddha on 02/04/17.
 */

//TODO: delete. Only for sample
@Path("test")
public class TestResource {

    @GET
    @Path("hello")
    @Produces(MediaType.TEXT_PLAIN)
    public String helloWorld() {
        return "Hello, world!";
    }

    @GET
    @Path("param")
    @Produces(MediaType.TEXT_PLAIN)
    public String paramMethod(@QueryParam("name") String name) {
        return "Hello, " + name;
    }

    @GET
    @Path("path/{var}")
    @Produces(MediaType.TEXT_PLAIN)
    public String pathMethod(@PathParam("var") String name) {
        return "Hello, " + name;
    }
}

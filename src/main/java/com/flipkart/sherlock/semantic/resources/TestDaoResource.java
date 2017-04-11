package com.flipkart.sherlock.semantic.resources;

import com.flipkart.sherlock.semantic.dao.mysql.SearchConfigsDao;
import com.google.inject.Inject;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

/**
 * Created by anurag.laddha on 03/04/17.
 */

/**
 * DI for resource
 */
//TODO: delete. Only for sample
@Path("db")
public class TestDaoResource {
    private SearchConfigsDao searchConfigsDao;

    @Inject
    public TestDaoResource(SearchConfigsDao searchConfigsDao) {
        this.searchConfigsDao = searchConfigsDao;
    }

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
        return this.searchConfigsDao.getConfig(name, "");
    }
}

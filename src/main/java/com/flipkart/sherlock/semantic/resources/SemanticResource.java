package com.flipkart.sherlock.semantic.resources;

import com.flipkart.sherlock.semantic.core.augment.representation.AugmentationResponse;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by anurag.laddha on 07/05/17.
 */

@Path("/semantic")
public class SemanticResource {
    /**
     * Accepts /augment GET request
     * store can contain alphanumeric characters, '/' and '-'
     * @param uriInfo
     * @return
     */
    @GET
    @Path("{store: [.\\w/-]+ }/augment")
    @Produces(MediaType.APPLICATION_JSON)
    public AugmentationResponse augment(@Context UriInfo uriInfo){

        //Extract all request params
        Map<String, List<String>> pathParams = new HashMap<>();
        Map<String, List<String>> queryParams = new HashMap<>();

        uriInfo.getPathParameters().keySet().forEach(k -> pathParams.put(k, uriInfo.getPathParameters().get(k)));
        uriInfo.getQueryParameters().keySet().forEach(k -> queryParams.put(k, uriInfo.getQueryParameters().get(k)));

       return null;
    }
}
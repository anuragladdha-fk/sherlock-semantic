package com.flipkart.sherlock.semantic.autosuggest.views;

import com.flipkart.sherlock.semantic.autosuggest.dao.AutoSuggestCacheRefresher;
import com.flipkart.sherlock.semantic.autosuggest.flow.ParamsHandler;
import com.flipkart.sherlock.semantic.autosuggest.flow.ProductRequestHandler;
import com.flipkart.sherlock.semantic.autosuggest.flow.QueryRequestHandler;
import com.flipkart.sherlock.semantic.autosuggest.helpers.MarketAnalyzer;
import com.flipkart.sherlock.semantic.autosuggest.models.*;
import com.flipkart.sherlock.semantic.autosuggest.utils.JsonSeDe;
import com.flipkart.sherlock.semantic.common.config.SearchConfigProvider;
import com.flipkart.sherlock.semantic.common.dao.mysql.ConfigsDao;
import com.flipkart.sherlock.semantic.common.solr.SolrServerProvider;
import com.flipkart.sherlock.semantic.core.search.SolrSearchServer;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;
import java.util.Map;

/**
 * Created by dhruv.pancholi on 30/05/17.
 */
@Slf4j
@Path("/")
@Singleton
public class AutoSuggestView {

    @Inject
    private JsonSeDe jsonSeDe;

    @Inject
    private MarketAnalyzer marketAnalyzer;

    @Inject
    private ConfigsDao configsDao;

    @Inject
    private AutoSuggestCacheRefresher autoSuggestCacheRefresher;

    @Inject
    private SolrServerProvider solrServerProvider;

    @Inject
    private SearchConfigProvider searchConfigProvider;

    @Inject
    private SolrSearchServer solrSearchServer;

    @Inject
    private QueryRequestHandler queryRequestHandler;

    @Inject
    private ProductRequestHandler productRequestHandler;

    @Inject
    private ParamsHandler paramsHandler;


    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String serverRunning() {
        return "Server is up and running!";
    }


    @GET
    @Path("/sherlock/stores/{store : .+}/autosuggest")
    @Produces(MediaType.APPLICATION_JSON)
    public String pathMethod(@PathParam("store") String store, @Context UriInfo uriInfo) {

        Params params = paramsHandler.getParams(store, uriInfo);

        QueryResponse queryResponse = queryRequestHandler.getQuerySuggestions(params.getQuery(), new QueryRequest(params, null));

        ProductResponse productResponse = productRequestHandler.getProductSuggestions(params.getQuery(), new ProductRequest(params, queryResponse.getAutoSuggestSolrResponse()));

        AutoSuggestResponse autoSuggestResponse = new AutoSuggestResponse(
                queryResponse.getQuerySuggestions(),
                productResponse.getProductSuggestions(),
                params.isDebug() ? params : null,
                params.isDebug() ? queryResponse.getAutoSuggestSolrResponse().getSolrQuery() : null,
                params.isDebug() ? productResponse.getAutoSuggestSolrResponse().getSolrQuery() : null,
                params.isDebug() ? productResponse.getAutoSuggestSolrResponse().getAutoSuggestDocs() : null);

        return jsonSeDe.writeValueAsString(autoSuggestResponse);
    }

    @GET
    @Path("sherlock/cacherefresh")
    @Produces(MediaType.APPLICATION_JSON)
    public String cacheRefresh(@QueryParam("dao") String dao) {
        Map<String, Integer> cachedMaps = autoSuggestCacheRefresher.refreshCache(dao);
        return jsonSeDe.writeValueAsString(cachedMaps);
    }

    @GET
    @Path("sherlock/cacheget")
    @Produces(MediaType.APPLICATION_JSON)
    public String cacheGet(@QueryParam("dao") String dao, @QueryParam("keys") String keys) {
        return jsonSeDe.writeValueAsString(autoSuggestCacheRefresher.cacheGet(dao, keys));
    }
}

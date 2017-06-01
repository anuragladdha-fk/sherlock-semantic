package com.flipkart.sherlock.semantic.autosuggest.views;

import com.flipkart.sherlock.semantic.autosuggest.dao.AbConfigDoa;
import com.flipkart.sherlock.semantic.autosuggest.dao.AutoSuggestCacheRefresher;
import com.flipkart.sherlock.semantic.autosuggest.helpers.AutoSuggestQueryAnalyzer;
import com.flipkart.sherlock.semantic.autosuggest.helpers.MarketAnalyzer;
import com.flipkart.sherlock.semantic.autosuggest.models.AbParams;
import com.flipkart.sherlock.semantic.autosuggest.utils.JsonSeDe;
import com.flipkart.sherlock.semantic.common.dao.mysql.ConfigsDao;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.Data;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static com.flipkart.sherlock.semantic.autosuggest.helpers.MarketAnalyzer.DEFAULT_MARKET_PLACE_IDS;

/**
 * Created by dhruv.pancholi on 30/05/17.
 */
@Path("")
@Singleton
public class AutoSuggestView {

    @Inject
    private JsonSeDe jsonSeDe;

    @Inject
    private MarketAnalyzer marketAnalyzer;

    @Inject
    private ConfigsDao configsDao;

    @Inject
    private AbConfigDoa abConfigDoa;

    @Inject
    private AutoSuggestQueryAnalyzer autoSuggestQueryAnalyzer;

    @Inject
    private AutoSuggestCacheRefresher autoSuggestCacheRefresher;


    @GET
    @Path("")
    @Produces(MediaType.TEXT_PLAIN)
    public String serverRunning() {
        return "Server is up and running!";
    }

    @Data
    private static class Params {
        private static List<String> DEFAULT_COMPLETION_TYPES = Arrays.asList("query", "product");
        private static List<String> DEFAULT_STORE_NODES = Arrays.asList("search.flipkart.com");
        private static AbParams DEFAULT_AB_PARAMS = new AbParams();

        private String query = "";
        private List<String> completionTypes = DEFAULT_COMPLETION_TYPES;
        private List<String> storeNodes = DEFAULT_STORE_NODES;
        private String leafNode = "search.flipkart.com";
        private List<String> marketPlaceIds = DEFAULT_MARKET_PLACE_IDS;
        private boolean queryDisabled = false;
        private boolean productDisabled = false;
        private AbParams abParams = DEFAULT_AB_PARAMS;
    }


    @GET
    @Path("sherlock/stores/{store : .+}/autosuggest")
    @Produces(MediaType.APPLICATION_JSON)
    public String pathMethod(@PathParam("store") String store, @Context UriInfo uriInfo) {

        Params params = new Params();
        params.setStoreNodes(Arrays.asList(store.split("/")));

        String query = uriInfo.getQueryParameters().getFirst("q");
        query = (query == null) ? "" : query;
        String types = uriInfo.getQueryParameters().getFirst("types");
        if (types != null) params.setCompletionTypes(Arrays.asList(types.split(",")));

        params.setQuery(query);
        params.setLeafNode(params.getStoreNodes().get(params.getStoreNodes().size() - 1));
        params.setMarketPlaceIds(MarketAnalyzer.getMarketPlaceIds(uriInfo.getQueryParameters().getFirst("groceryContext"), uriInfo.getQueryParameters().getFirst("marketplaceId")));
        params.setQueryDisabled(!params.getCompletionTypes().contains("query") || autoSuggestQueryAnalyzer.isDisabled(query));
        params.setProductDisabled(!params.getCompletionTypes().contains("product") || MarketAnalyzer.removeProducts(query, params.getMarketPlaceIds()));
        params.setAbParams(abConfigDoa.getCached());

        return jsonSeDe.writeValueAsString(params);
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

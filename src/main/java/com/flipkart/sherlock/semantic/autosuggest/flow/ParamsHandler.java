package com.flipkart.sherlock.semantic.autosuggest.flow;

import com.flipkart.sherlock.semantic.autosuggest.dao.ConfigsDao;
import com.flipkart.sherlock.semantic.autosuggest.helpers.AutoSuggestQueryAnalyzer;
import com.flipkart.sherlock.semantic.autosuggest.helpers.MarketAnalyzer;
import com.flipkart.sherlock.semantic.autosuggest.models.Config;
import com.flipkart.sherlock.semantic.autosuggest.models.Params;
import com.google.inject.Inject;

import javax.ws.rs.core.UriInfo;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by dhruv.pancholi on 02/06/17.
 */
public class ParamsHandler {

    @Inject
    private ConfigsDao configsDao;

    @Inject
    private AutoSuggestQueryAnalyzer autoSuggestQueryAnalyzer;

    @Inject
    private MarketAnalyzer marketAnalyzer;

    public Params getParams(String store, UriInfo uriInfo) {
        Params params = new Params();

        store = StoreHandler.removeAllStores(store);
        params.setStore(store);
        params.setStoreNodes(Arrays.asList(store.split("/")));
        params.setLeafNode(params.getStoreNodes().get(params.getStoreNodes().size() - 1));

        params.setDebug(uriInfo.getQueryParameters().getFirst("debug") != null);

        String query = uriInfo.getQueryParameters().getFirst("q");
        query = (query == null) ? "" : query;
        params.setQuery(query);

        String types = uriInfo.getQueryParameters().getFirst("types");
        if (types != null) params.setCompletionTypes(Arrays.asList(types.split(",")));

        params.setMarketPlaceIds(marketAnalyzer.getMarketPlaceIds(store, uriInfo.getQueryParameters().getFirst("groceryContext"), uriInfo.getQueryParameters().getFirst("marketplaceId")));
        params.setQueryDisabled(!params.getCompletionTypes().contains("query") || autoSuggestQueryAnalyzer.isDisabled(query));
        params.setProductDisabled(!params.getCompletionTypes().contains("product") || MarketAnalyzer.removeProducts(query, params.getMarketPlaceIds()));


        String abParamsString = uriInfo.getQueryParameters().getFirst("ab-params");
        List<String> abParams = (abParamsString != null && !abParamsString.isEmpty()) ? Arrays.asList(abParamsString.split(",")) : null;

        String bucket = uriInfo.getQueryParameters().getFirst("bucket");
        bucket = (bucket == null) ? "default" : bucket;

        Config config = configsDao.getCached().get(bucket);

        String solrHost = uriInfo.getQueryParameters().getFirst("solrHost");
        params.setSolrHost(solrHost == null ? config.getSolrHost() : solrHost);

        String solrPort = uriInfo.getQueryParameters().getFirst("solrPort");
        params.setSolrPort(solrPort == null ? config.getSolrPort() : Integer.parseInt(solrPort));

        String solrCore = uriInfo.getQueryParameters().getFirst("solrCore");
        params.setSolrCore(solrCore == null ? config.getSolrCore() : solrCore);

        String queryField = uriInfo.getQueryParameters().getFirst("queryField");
        params.setQueryField(queryField == null ? config.getQueryField() : queryField);

        String prefixEdgyField = uriInfo.getQueryParameters().getFirst("prefixEdgyField");
        params.setPrefixEdgyField(prefixEdgyField == null ? config.getPrefixEdgyField() : prefixEdgyField);

        String phraseEdgyField = uriInfo.getQueryParameters().getFirst("phraseEdgyField");
        params.setPhraseEdgyField(phraseEdgyField == null ? config.getPhraseEdgyField() : phraseEdgyField);

        String phraseBoost = uriInfo.getQueryParameters().getFirst("phraseBoost");
        params.setPhraseBoost(phraseBoost == null ? config.getPhraseBoost() : Integer.parseInt(phraseBoost));

        String phraseField = uriInfo.getQueryParameters().getFirst("phraseField");
        params.setPhraseField(phraseField == null ? config.getPhraseEdgyField() : phraseField);

        String boostFunction = uriInfo.getQueryParameters().getFirst("boostFunction");
        params.setBoostFunction(boostFunction == null ? config.getBoostFunction() : boostFunction);

        String sortFunctionString = uriInfo.getQueryParameters().getFirst("sortFunction");
        params.setSortFunctions(sortFunctionString == null ? Arrays.asList(config.getSortFunctionString().split(",")) : Arrays.asList(sortFunctionString.split(",")));

        String rows = uriInfo.getQueryParameters().getFirst("rows");
        params.setRows(rows == null ? config.getRows() : Integer.parseInt(rows));

        String ctrThreshold = uriInfo.getQueryParameters().getFirst("ctrThreshold");
        params.setCtrThreshold(ctrThreshold == null ? config.getCtrThreshold() : Double.parseDouble(ctrThreshold));

        String ctrField = uriInfo.getQueryParameters().getFirst("ctrField");
        params.setCtrField(ctrField == null ? config.getCtrField() : ctrField);

        String fqs = uriInfo.getQueryParameters().getFirst("fqs");
        params.setFqs(fqs == null ? (config.getFqsString() == null || config.getFqsString().isEmpty() ? new ArrayList<>() : Arrays.asList(config.getFqsString().split(","))) : Arrays.asList(fqs.split(",")));

        String maxNumberOfStorePerQuery = uriInfo.getQueryParameters().getFirst("maxNumberOfStorePerQuery");
        params.setMaxNumberOfStorePerQuery(maxNumberOfStorePerQuery == null ? config.getMaxNumberOfStorePerQuery() : Integer.parseInt(maxNumberOfStorePerQuery));

        String solrSpellCorrection = uriInfo.getQueryParameters().getFirst("solrSpellCorrection");
        params.setSolrSpellCorrection(solrSpellCorrection == null ? config.isSolrSpellCorrection() : Boolean.parseBoolean(solrSpellCorrection));

        return params;
    }
}

package com.flipkart.sherlock.semantic.autosuggest.models;

import lombok.Data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.flipkart.sherlock.semantic.autosuggest.helpers.MarketAnalyzer.DEFAULT_MARKET_PLACE_IDS;

/**
 * Created by dhruv.pancholi on 01/06/17.
 */
@Data
public class Params {
    private static List<String> DEFAULT_COMPLETION_TYPES = Arrays.asList("query", "product");
    private static List<String> DEFAULT_STORE_NODES = Arrays.asList("search.flipkart.com");

    private boolean debug = false;

    private String query = "";

    // Path variables
    private List<String> completionTypes = DEFAULT_COMPLETION_TYPES;
    private String store = "search.flipkart.com";
    private List<String> storeNodes = DEFAULT_STORE_NODES;
    private String leafNode = "search.flipkart.com";

    // Grocery Params
    private List<String> marketPlaceIds = DEFAULT_MARKET_PLACE_IDS;

    // Flow enablers
    private boolean queryDisabled = false;
    private boolean productDisabled = false;
    private String bucket = "default";

    // Solr variables
    private String solrHost = "localhost";
    private int solrPort = 25280;
    private String solrCore = "autosuggest";

    private String queryField = "text";
    private String prefixEdgyField = "prefix_edgytext";
    private String phraseEdgyField = "text_edgytext_phrase";
    private int phraseBoost = 1;
    private String phraseField = "text";
    private String boostFunction = "min(div(log(field(impressions_sint)),log(10)),10.0)^1";
    private List<String> sortFunctions = Arrays.asList("score desc", "ranking-score_sfloat desc", "p-hits_sfloat desc", "ctr_sfloat desc");
    private int rows = 10;
    private double ctrThreshold = 0.05;
    private String ctrField = "ctr_float";
    private List<String> fqs = new ArrayList<>();
    private int maxNumberOfStorePerQuery = 3;
    private boolean solrSpellCorrection = true;

    // Add Params here

}

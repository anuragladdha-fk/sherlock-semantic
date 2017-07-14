package com.flipkart.sherlock.semantic.autosuggest.models;

import lombok.Getter;

/**
 * Created by dhruv.pancholi on 04/07/17.
 */
@Getter
public class Config {

    private String solrHost = "localhost";
    private int solrPort = 25280;
    private String solrCore = "autosuggest";

    private String queryField = "text";
    private String prefixEdgyField = "prefix_edgytext";
    private String phraseEdgyField = "text_edgytext_phrase";
    private int phraseBoost = 1;
    private String phraseField = "text";
    private String boostFunction = "min(div(log(field(impressions_sint)),log(10)),10.0)^1";
    private String sortFunctionString = "score desc,ranking-score_sfloat desc,p-hits_sfloat desc,ctr_sfloat desc";
    private int rows = 10;
    private double ctrThreshold = 0.05;
    private String ctrField = "ctr_float";
    private String fqsString = "";
    private int maxNumberOfStorePerQuery = 3;
    private boolean solrSpellCorrection = true;

}

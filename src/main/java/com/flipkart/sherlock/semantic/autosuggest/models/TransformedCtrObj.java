package com.flipkart.sherlock.semantic.autosuggest.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

/**
 * Created by dhruv.pancholi on 01/06/17.
 */
@Getter
public class TransformedCtrObj {

    @JsonProperty("impressions")
    private int impressions;

    @JsonProperty("p-hits")
    private double pHits;

    @JsonProperty("state-hits")
    private double stateHits;

    @JsonProperty("ctr")
    private double ctr;
}

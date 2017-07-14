package com.flipkart.sherlock.semantic.autosuggest.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

/**
 * Created by dhruv.pancholi on 06/10/16.
 */
@Getter
@AllArgsConstructor
public class DecayedProductObj {

    @JsonProperty("dd-contrib")
    private double ddContrib;

    @JsonProperty("contrib")
    private double contrib;

    @JsonProperty("product-id")
    private String productId;

    @JsonProperty("count")
    private int count;

    @JsonProperty("dd-count")
    private double ddCount;

    @JsonProperty("dayCount")
    private Integer dayCount;

    @JsonProperty("leafPaths")
    private List<String> leafPaths;

    @JsonProperty("store")
    private String store;
}

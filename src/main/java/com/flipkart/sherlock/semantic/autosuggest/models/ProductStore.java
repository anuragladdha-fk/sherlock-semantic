package com.flipkart.sherlock.semantic.autosuggest.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Created by dhruv.pancholi on 25/04/17.
 */
@Getter
@AllArgsConstructor
public class ProductStore {

    @JsonProperty("count")
    private int count;

    @JsonProperty("dd-count")
    private double ddCount;

    @JsonProperty("contrib")
    private double contrib;

    @JsonProperty("dd-contrib")
    private double ddContrib;

    @JsonProperty("store")
    private String store;
}

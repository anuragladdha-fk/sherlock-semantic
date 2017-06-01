package com.flipkart.sherlock.semantic.autosuggest.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

/**
 * Created by dhruv.pancholi on 31/05/17.
 */
@Getter
public class AbParams {

    @JsonProperty("qSpell")
    private boolean qSpell = true;

    @JsonProperty("disable-new-as")
    private boolean disableNewAs = true;

    @JsonProperty("enable-spell")
    private boolean enableSpell = true;

    @JsonProperty("only-top-stores")
    private boolean onlyTopStores = false;

    @JsonProperty("only-secondlevel-stores")
    private boolean onlySecondlevelStores = false;

    @JsonProperty("new-mapi-response")
    private boolean newMapiResponse = true;
}

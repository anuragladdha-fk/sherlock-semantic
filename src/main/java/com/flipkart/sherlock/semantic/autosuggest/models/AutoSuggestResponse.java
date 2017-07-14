package com.flipkart.sherlock.semantic.autosuggest.models;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

/**
 * Created by dhruv.pancholi on 02/06/17.
 */
@Getter
@AllArgsConstructor
public class AutoSuggestResponse {
    private List<QuerySuggestion> querySuggestions;
    private List<ProductSuggestion> productSuggestions;
    private Params params;
    private String querySolrQuery;
    private String productSolrQuery;
    private List<AutoSuggestDoc> autoSuggestDocs;
}

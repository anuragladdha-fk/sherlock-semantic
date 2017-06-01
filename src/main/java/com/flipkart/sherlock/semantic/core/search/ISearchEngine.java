package com.flipkart.sherlock.semantic.core.search;

import java.util.Map;

/**
 * Created by anurag.laddha on 23/05/17.
 */
public interface ISearchEngine {
    SearchResponse query(SearchRequest request, String collection, Map<String, String> params);
}

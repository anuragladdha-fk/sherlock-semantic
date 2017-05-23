package com.flipkart.sherlock.semantic.core.search;

import com.flipkart.sherlock.semantic.core.solr.SolrServerProvider;
import com.google.inject.Inject;

import java.util.Map;

/**
 * Created by anurag.laddha on 23/05/17.
 */
public class SolrSearchServer implements ISearchEngine {
    private SolrServerProvider solrServerProvider;

    @Inject
    public SolrSearchServer(SolrServerProvider solrServerProvider) {
        this.solrServerProvider = solrServerProvider;
    }

    @Override
    public SearchResponse query(SearchRequest request, Map<String, String> params) {

        /**
         * from  SearchRequest construct SolrQuery
         * Get SolrServer from SolrServerProvider based on core
         * Get response and convert to  SearchResponse object
         */

        return null;
    }
}

package com.flipkart.sherlock.semantic.core.search;

import com.flipkart.sherlock.semantic.common.solr.Core;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.QueryResponse;

import java.util.Map;

/**
 * Created by anurag.laddha on 23/05/17.
 */
public interface ISearchEngine {
    SearchResponse query(SearchRequest request, String collection, Map<String, String> params);

    SearchResponse query(SearchRequest request, Core core);

    SpellResponse querySpell(SearchRequest request, Core core);

    QueryResponse query(SolrQuery solrQuery, String solrQueryString, Core core);
}

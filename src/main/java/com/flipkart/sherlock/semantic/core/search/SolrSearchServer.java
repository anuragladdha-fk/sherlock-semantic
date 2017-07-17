package com.flipkart.sherlock.semantic.core.search;

import com.flipkart.sherlock.semantic.autosuggest.utils.JsonSeDe;
import com.flipkart.sherlock.semantic.common.solr.AutoSuggestSolrServerProvider;
import com.flipkart.sherlock.semantic.common.solr.Core;
import com.flipkart.sherlock.semantic.common.solr.SolrServerProvider;
import com.flipkart.sherlock.semantic.core.search.SearchRequest.Param;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.SpellCheckResponse;
import org.apache.solr.client.solrj.response.SpellCheckResponse.Suggestion;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.*;
import java.util.Map.Entry;

/**
 * Created by anurag.laddha on 23/05/17.
 */
@Slf4j
@Singleton
public class SolrSearchServer implements ISearchEngine {

    @Inject
    private JsonSeDe jsonSeDe;

    @Inject
    private SolrServerProvider solrServerProvider;

    @Inject
    private AutoSuggestSolrServerProvider autoSuggestSolrServerProvider;


    @Override
    public SearchResponse query(SearchRequest request, String collection, Map<String, String> params) {
        String experiment = "";
        if (params.containsKey("experiment"))
            experiment = params.get("experiment");
        SolrQuery solrQuery = getSolrQueryFromSearchReq(request);
        SolrServer solrServer = solrServerProvider.getSolrServer(collection, experiment);
        try {
            QueryResponse queryResponse = solrServer.query(solrQuery);
            return getSearchResponseFromSolrResponse(jsonSeDe.writeValueAsString(solrQuery), queryResponse);
        } catch (SolrServerException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public SearchResponse query(SearchRequest request, Core core) {

        SolrQuery solrQuery = getSolrQueryFromSearchReq(request);
        String solrQueryString = getSolrQueryString(solrQuery);

        QueryResponse queryResponse = query(solrQuery, solrQueryString, core);

        if (queryResponse == null) return null;

        return getSearchResponseFromSolrResponse(solrQueryString, queryResponse);
    }

    @Override
    public SpellResponse querySpell(SearchRequest request, Core core) {
        SolrQuery solrQuery = getSolrQueryFromSearchReq(request);
        String solrQueryString = getSolrQueryString(solrQuery);
        QueryResponse queryResponse = query(solrQuery, solrQueryString, core);
        return getSpellResponseFromSolrResponse(solrQueryString, queryResponse);
    }

    private SpellResponse getSpellResponseFromSolrResponse(String solrQueryString, QueryResponse queryResponse) {
        if (queryResponse == null) return null;
        SpellCheckResponse spellCheckResponse = queryResponse.getSpellCheckResponse();
        if (spellCheckResponse == null) return null;
        List<Suggestion> suggestions = spellCheckResponse.getSuggestions();
        if (suggestions == null || suggestions.size() == 0) return null;
        return new SpellResponse(solrQueryString, queryResponse.getSpellCheckResponse().getSuggestions().get(0).getAlternatives());
    }

    public String getSolrQueryString(SolrQuery solrQuery) {
        String solrQueryString = solrQuery.toString();
        try {
            solrQueryString = URLDecoder.decode(solrQuery.toString(), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            log.error("Can't decode the solr request: {}", solrQuery.toString());
        }
        return solrQueryString;
    }

    /**
     * @param solrQuery
     * @param solrQueryString Only for the logging purpose and to avoid recomputing it for debugging
     * @param core
     * @return
     */
    @Override
    public QueryResponse query(SolrQuery solrQuery, String solrQueryString, Core core) {

        SolrServer solrServer = autoSuggestSolrServerProvider.getSolrServer(core);

        try {
            QueryResponse queryResponse = solrServer.query(solrQuery);
            log.info("{} Status={} NumDocs={} QTime={}", solrQueryString, queryResponse.getStatus(), queryResponse.getResults().size(), queryResponse.getQTime());
            return queryResponse;
        } catch (SolrServerException e) {
            log.error("Cannot query SolrServer correctly for request: {}", solrQueryString, e);
        }

        return null;
    }

    private SearchResponse getSearchResponseFromSolrResponse(String solrQuery, QueryResponse queryResponse) {
        SolrDocumentList solrDocs = queryResponse.getResults();
        SearchResponse searchResponse = new SearchResponse(solrQuery, new ArrayList<>());

        if (solrDocs == null || solrDocs.isEmpty()) return searchResponse;

        for (SolrDocument solrDoc : solrDocs) {
            Map<String, Object> docMap = new HashMap<>();
            solrDoc.forEach(docMap::put);
            searchResponse.addDoc(docMap);
        }
        return searchResponse;
    }


    private SolrQuery getSolrQueryFromSearchReq(SearchRequest request) {
        SolrQuery solrQuery = new SolrQuery();
        Set<Entry<Param, ArrayList<String>>> entries = request.getRequestParams().entrySet();
        for (Entry<Param, ArrayList<String>> entry : entries) {
            solrQuery.setParam(entry.getKey().getParamName(), entry.getValue().toArray(new String[entry.getValue().size()]));
        }
        return solrQuery;
    }

}

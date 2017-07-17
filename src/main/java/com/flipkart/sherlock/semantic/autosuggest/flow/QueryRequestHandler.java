package com.flipkart.sherlock.semantic.autosuggest.flow;

import com.flipkart.sherlock.semantic.autosuggest.helpers.QuerySanitizer.QueryPrefix;
import com.flipkart.sherlock.semantic.autosuggest.models.*;
import com.flipkart.sherlock.semantic.core.search.SolrSearchServer;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

import static com.flipkart.sherlock.semantic.autosuggest.helpers.QuerySanitizer.getQueryPrefix;

/**
 * Created by dhruv.pancholi on 01/06/17.
 */
@Slf4j
@Singleton
public class QueryRequestHandler {

    @Inject
    private SolrSearchServer solrSearchServer;

    @Inject
    private SolrRequestHandler solrRequestHandler;

    @Inject
    private StoreHandler storeHandler;

    public QueryResponse getQuerySuggestions(String query, QueryRequest queryRequest) {

        Params params = queryRequest.getParams();

        QueryPrefix queryPrefix = getQueryPrefix(query);

        AutoSuggestSolrResponse autoSuggestSolrResponse = (queryRequest.getAutoSuggestSolrResponse() == null) ? solrRequestHandler.getAutoSuggestSolrResponse(queryPrefix, params) : queryRequest.getAutoSuggestSolrResponse();

        List<AutoSuggestDoc> autoSuggestDocs = autoSuggestSolrResponse.getAutoSuggestDocs();

        if (autoSuggestDocs.size() == 0 && params.isSolrSpellCorrection()) {
            AutoSuggestSpellResponse autoSuggestSpellResponse = solrRequestHandler.getAutoSuggestSolrSpellCorrectionResponse(queryPrefix, params);
            if (autoSuggestSpellResponse != null) {
                queryPrefix = getQueryPrefix(autoSuggestSpellResponse.getCorrectedQuery());
                autoSuggestSolrResponse = solrRequestHandler.getAutoSuggestSolrResponse(queryPrefix, params);
                autoSuggestDocs = autoSuggestSolrResponse.getAutoSuggestDocs();
            }
        }

        List<QuerySuggestion> querySuggestions = new ArrayList<>();

        int numberOfDocsWithStores = storeHandler.getNumberOfDocsWithStores(autoSuggestDocs);

        int processCount = 0;

        for (AutoSuggestDoc autoSuggestDoc : autoSuggestDocs) {
            String correctedQuery = autoSuggestDoc.getCorrectedQuery();
            List<Store> stores = (processCount < numberOfDocsWithStores) ? storeHandler.getStoresFromProductStore(
                    autoSuggestDoc.getProductStores(),
                    params.getMaxNumberOfStorePerQuery(),
                    params.getStore(),
                    params.getMarketPlaceIds()) : new ArrayList<>();
            querySuggestions.add(new QuerySuggestion(correctedQuery, stores));
            processCount++;
        }

        return new QueryResponse(autoSuggestSolrResponse, querySuggestions);
    }
}

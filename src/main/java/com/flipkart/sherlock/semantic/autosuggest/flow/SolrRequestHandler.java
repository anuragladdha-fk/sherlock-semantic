package com.flipkart.sherlock.semantic.autosuggest.flow;

import com.fasterxml.jackson.core.type.TypeReference;
import com.flipkart.sherlock.semantic.autosuggest.helpers.QuerySanitizer.QueryPrefix;
import com.flipkart.sherlock.semantic.autosuggest.models.AutoSuggestDoc;
import com.flipkart.sherlock.semantic.autosuggest.models.*;
import com.flipkart.sherlock.semantic.autosuggest.utils.JsonSeDe;
import com.flipkart.sherlock.semantic.common.solr.Core;
import com.flipkart.sherlock.semantic.core.search.SearchRequest;
import com.flipkart.sherlock.semantic.core.search.SearchResponse;
import com.flipkart.sherlock.semantic.core.search.SolrSearchServer;
import com.flipkart.sherlock.semantic.core.search.SpellResponse;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static com.flipkart.sherlock.semantic.autosuggest.helpers.MarketAnalyzer.FLIP_KART;
import static com.flipkart.sherlock.semantic.autosuggest.helpers.MarketAnalyzer.FLIP_MART;
import static com.flipkart.sherlock.semantic.autosuggest.models.AutoSuggestDoc.*;

/**
 * Created by dhruv.pancholi on 01/06/17.
 */
@Singleton
public class SolrRequestHandler {

    @Inject
    private JsonSeDe jsonSeDe;

    @Inject
    private SolrSearchServer solrSearchServer;

    private static final AutoSuggestSolrResponse DEFAULT_AUTO_SUGGEST_SOLR_RESPONSE = new AutoSuggestSolrResponse(null, new ArrayList<>());

    public AutoSuggestSolrResponse getAutoSuggestSolrResponse(QueryPrefix queryPrefix, Params params) {

        SearchRequest searchRequest = createSearchRequest(queryPrefix, params);
        SearchResponse searchResponse = solrSearchServer.query(searchRequest, new Core(params.getSolrHost(), params.getSolrPort(), params.getSolrCore()));

        if (searchResponse == null) return DEFAULT_AUTO_SUGGEST_SOLR_RESPONSE;

        return transformSearchResponse(searchResponse);
    }

    public AutoSuggestSpellResponse getAutoSuggestSolrSpellCorrectionResponse(QueryPrefix queryPrefix, Params params) {
        SearchRequest spellRequest = getSpellRequest(queryPrefix, params);
        SpellResponse spellResponse = solrSearchServer.querySpell(spellRequest, new Core(params.getSolrHost(), params.getSolrPort(), params.getSolrCore()));
        return transformSpellResponse(spellResponse);
    }

    private AutoSuggestSpellResponse transformSpellResponse(SpellResponse spellResponse) {
        if (spellResponse == null) return null;
        if (spellResponse.getResults() == null || spellResponse.getResults().size() == 0) return null;
        return new AutoSuggestSpellResponse(spellResponse.getSolrQuery(), spellResponse.getResults().get(0));
    }

    private SearchRequest createSearchRequest(QueryPrefix queryPrefix, Params params) {

        SearchRequest searchRequest = new SearchRequest();

        searchRequest.addParam(SearchRequest.Param.QT, "dismax");
        searchRequest.addParam(SearchRequest.Param.Q, queryPrefix.getQuery());
        searchRequest.addParam(SearchRequest.Param.FL, Arrays.asList(LOGGED_QC_QUERY, CORRECTED_QUERY, CTR_OBJ, PRODUCT_OBJECT, PRODUCT_STORE));

        List<String> fqs = params.getFqs();
        fqs.add(params.getCtrField() + ":[" + params.getCtrThreshold() + " TO *]");
        fqs.add(params.getPrefixEdgyField() + ":\"" + queryPrefix.getPrefix() + "\"");

        if (params.getMarketPlaceIds().size() == 1
                && (params.getMarketPlaceIds().get(0).equals(FLIP_KART)
                || params.getMarketPlaceIds().get(0).equals(FLIP_MART))) {
            fqs.add("market_smstring:\"" + params.getMarketPlaceIds().get(0) + "\"");
        }

        searchRequest.addParam(SearchRequest.Param.FQ, fqs);
        searchRequest.addParam(SearchRequest.Param.BF, params.getBoostFunction());
        searchRequest.addParam(SearchRequest.Param.QF, params.getQueryField());
        searchRequest.addParam(SearchRequest.Param.PF, params.getPhraseField());
        searchRequest.addParam(SearchRequest.Param.ALTQ, "*:*");
        searchRequest.addParam(SearchRequest.Param.BQ, params.getPhraseEdgyField() + ":\"" + (queryPrefix.getOriginalQuery().equals("") ? "*:*" : queryPrefix.getOriginalQuery()) + "\"^" + params.getPhraseBoost());
        searchRequest.addParam(SearchRequest.Param.SORT, params.getSortFunctions());
        searchRequest.addParam(SearchRequest.Param.ROWS, String.valueOf(params.getRows()));

        return searchRequest;
    }

    /**
     * At the verge of removal, thus not getting the parameters from params object
     *
     * @param queryPrefix
     * @param params
     * @return
     */
    private SearchRequest getSpellRequest(QueryPrefix queryPrefix, Params params) {

        SearchRequest searchRequest = new SearchRequest();

        searchRequest.addParam(SearchRequest.Param.ROWS, "0");
        searchRequest.addParam(SearchRequest.Param.SPELLCHECK, "on");
        searchRequest.addParam(SearchRequest.Param.Q, queryPrefix.getOriginalQuery());
        searchRequest.addParam(SearchRequest.Param.QT, "spell_as");
        searchRequest.addParam(SearchRequest.Param.DEFTYPE, "dismax");
        searchRequest.addParam(SearchRequest.Param.MM, "100");
        searchRequest.addParam(SearchRequest.Param.SPELLCHECK_ACCURACY, "0.70");
        searchRequest.addParam(SearchRequest.Param.SPELLCHECK_Q, queryPrefix.getOriginalQuery());
        searchRequest.addParam(SearchRequest.Param.SPELLCHECK_COUNT, "20");
        searchRequest.addParam(SearchRequest.Param.SPELLCHECK_COLLATE, "true");
        searchRequest.addParam(SearchRequest.Param.SPELLCHECK_MAX_COLLATIONS, "3");
        searchRequest.addParam(SearchRequest.Param.SPELLCHECK_MAX_COLLATION_TRIES, "5");
        searchRequest.addParam(SearchRequest.Param.SPELLCHECK_COLLATE_EXTENDED_RESULTS, "true");
        searchRequest.addParam(SearchRequest.Param.SPELLCHECK_ONLY_MORE_POPULAR, "true");

        return searchRequest;
    }

    private AutoSuggestSolrResponse transformSearchResponse(SearchResponse searchResponse) {
        List<Map<String, Object>> solrDocs = searchResponse.getResults();

        if (solrDocs == null) return DEFAULT_AUTO_SUGGEST_SOLR_RESPONSE;

        List<AutoSuggestDoc> autoSuggestDocs = new ArrayList<>();

        for (Map<String, Object> solrDoc : solrDocs) {

            String ctrObjString = (String) solrDoc.get(CTR_OBJ);
            if (ctrObjString == null || ctrObjString.isEmpty()) continue;
            TransformedCtrObj ctrObj = jsonSeDe.readValue(ctrObjString, TransformedCtrObj.class);
            if (ctrObj == null) continue;


            String productObjectString = (String) solrDoc.get(PRODUCT_OBJECT);
            if (productObjectString == null || productObjectString.isEmpty()) continue;
            List<DecayedProductObj> decayedProductObjs = jsonSeDe.readValue(productObjectString, new TypeReference<List<DecayedProductObj>>() {
            });
            if (decayedProductObjs == null) continue;

            String productStoreString = (String) solrDoc.get(PRODUCT_STORE);
            if (productStoreString == null || productStoreString.isEmpty()) continue;
            List<ProductStore> productStores = jsonSeDe.readValue(productStoreString, new TypeReference<List<ProductStore>>() {
            });
            if (productStores == null) continue;


            autoSuggestDocs.add(new AutoSuggestDoc(
                    (String) solrDoc.get(LOGGED_QC_QUERY),
                    (String) solrDoc.get(CORRECTED_QUERY),
                    ctrObj,
                    decayedProductObjs,
                    productStores));
        }

        return new AutoSuggestSolrResponse(searchResponse.getSolrQuery(), autoSuggestDocs);
    }


}

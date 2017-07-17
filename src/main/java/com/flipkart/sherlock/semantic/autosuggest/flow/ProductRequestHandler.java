package com.flipkart.sherlock.semantic.autosuggest.flow;

import com.flipkart.sherlock.semantic.autosuggest.dao.StorePathCanonicalTitleDao;
import com.flipkart.sherlock.semantic.autosuggest.helpers.MarketAnalyzer;
import com.flipkart.sherlock.semantic.autosuggest.models.*;
import com.flipkart.sherlock.semantic.autosuggest.utils.JsonSeDe;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import java.util.*;

import static com.flipkart.sherlock.semantic.autosuggest.helpers.QuerySanitizer.QueryPrefix;
import static com.flipkart.sherlock.semantic.autosuggest.helpers.QuerySanitizer.getQueryPrefix;

/**
 * Created by dhruv.pancholi on 27/04/17.
 */
@Singleton
public class ProductRequestHandler {

    @Inject
    private JsonSeDe jsonSeDe;

    @Inject
    private MarketAnalyzer marketAnalyzer;

    @Inject
    private StorePathCanonicalTitleDao storePathCanonicalTitleDao;

    @Inject
    private SolrRequestHandler solrRequestHandler;

    public ProductResponse getProductSuggestions(String query, ProductRequest productRequest) {

        Params params = productRequest.getParams();

        QueryPrefix queryPrefix = getQueryPrefix(query);

        AutoSuggestSolrResponse autoSuggestSolrResponse = (productRequest.getAutoSuggestSolrResponse() == null) ? solrRequestHandler.getAutoSuggestSolrResponse(queryPrefix, params) : productRequest.getAutoSuggestSolrResponse();

        List<AutoSuggestDoc> autoSuggestDocs = autoSuggestSolrResponse.getAutoSuggestDocs();

        List<List<DecayedProductObj>> decayedProductObjsList = getDecayedProductObjsList(autoSuggestDocs);

        List<ProductSuggestion> productSuggestions = getProductSuggestions(decayedProductObjsList);

        return new ProductResponse(autoSuggestSolrResponse, productSuggestions);
    }

    private List<List<DecayedProductObj>> getDecayedProductObjsList(List<AutoSuggestDoc> autoSuggestDocs) {

        List<List<DecayedProductObj>> decayedProductObjsList = new ArrayList<>(autoSuggestDocs.size());

        List<DecayedProductObj> decayedProductObjs = null;

        for (AutoSuggestDoc autoSuggestDoc : autoSuggestDocs) {
            decayedProductObjs = autoSuggestDoc.getDecayedProductObjs();
            decayedProductObjsList.add((decayedProductObjs.size() > 15) ? decayedProductObjs.subList(0, 15) : decayedProductObjs);
        }

        return decayedProductObjsList;
    }


    private List<ProductSuggestion> getProductSuggestions(List<List<DecayedProductObj>> decayedProductObjectsList) {

        Map<String, Double> distribution = new HashMap<>();

        for (List<DecayedProductObj> decayedProductObjs : decayedProductObjectsList) {
            for (DecayedProductObj decayedProductObj : decayedProductObjs) {

                if (!distribution.containsKey(decayedProductObj.getProductId()))
                    distribution.put(decayedProductObj.getProductId(), decayedProductObj.getDdCount());
                else
                    distribution.put(decayedProductObj.getProductId(), distribution.get(decayedProductObj.getProductId()) + decayedProductObj.getDdCount());
            }
        }

        List<Map.Entry<String, Double>> entries = new ArrayList<>(distribution.entrySet());
        Collections.sort(entries, (o1, o2) -> Double.compare(o2.getValue(), o1.getValue()));

        List<ProductSuggestion> productSuggestions = new ArrayList<>();

        int count = 0;
        for (Map.Entry<String, Double> entry : entries) {
            if (count++ > 10) break;
            productSuggestions.add(new ProductSuggestion(entry.getKey()));
        }
        return productSuggestions;
    }

}

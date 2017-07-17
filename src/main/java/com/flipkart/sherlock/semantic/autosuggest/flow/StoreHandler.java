package com.flipkart.sherlock.semantic.autosuggest.flow;

import com.flipkart.sherlock.semantic.autosuggest.dao.StorePathCanonicalTitleDao;
import com.flipkart.sherlock.semantic.autosuggest.helpers.MarketAnalyzer;
import com.flipkart.sherlock.semantic.autosuggest.models.AutoSuggestDoc;
import com.flipkart.sherlock.semantic.autosuggest.models.ProductStore;
import com.flipkart.sherlock.semantic.autosuggest.models.Store;
import com.flipkart.sherlock.semantic.autosuggest.utils.JsonSeDe;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.flipkart.sherlock.semantic.autosuggest.helpers.MarketAnalyzer.DEFAULT_MARKET_PLACE_IDS;

/**
 * Created by dhruv.pancholi on 27/04/17.
 */
@Singleton
public class StoreHandler {

    @Inject
    private JsonSeDe jsonSeDe;

    @Inject
    private MarketAnalyzer marketAnalyzer;

    @Inject
    private StorePathCanonicalTitleDao storePathCanonicalTitleDao;

    private static final List<Store> EMPTY_STORE_LIST = new ArrayList<>();

    private static final List<String> allStores = Arrays.asList("search.flipkart.com", "all", "m.flipkart.com", "flipkart.com");

    /**
     * @param productStores   A string field retrieved from meta
     * @param maxStores       Maximum number of stores to be returned
     * @param contextualStore Return only the stores, which starts with this store as root
     * @param marketPlaceIds  Filter out stores, whose marketPlaceId is present in this list
     * @return
     */
    public List<Store> getStoresFromProductStore(List<ProductStore> productStores, int maxStores, String contextualStore, List<String> marketPlaceIds) {

        if (productStores == null) return EMPTY_STORE_LIST;

        productStores.sort((o1, o2) -> Double.compare(o2.getDdCount(), o1.getDdCount()));

        if (contextualStore == null) contextualStore = "";
        if (marketPlaceIds == null) marketPlaceIds = DEFAULT_MARKET_PLACE_IDS;

        double cumulativeContrib = 0;
        List<Store> stores = new ArrayList<>();

        for (ProductStore productStore : productStores) {
            if (cumulativeContrib > 90) break;
            cumulativeContrib += productStore.getDdContrib();

            if (productStore.getDdContrib() <= 3) continue;
            String store = productStore.getStore();
            if (store == null) continue;
            store = removeAllStores(store);
            if (!store.startsWith(contextualStore)) continue;

            String marketPlaceId = marketAnalyzer.getMarketPlaceId(store);
            if (!marketPlaceIds.contains(marketPlaceId)) continue;
            String canonicalTitle = storePathCanonicalTitleDao.getCanonicalTitle(store);
            if (canonicalTitle == null) continue;
            stores.add(new Store(store, canonicalTitle, marketPlaceId));
        }

        stores = (stores.size() > maxStores) ? stores.subList(0, maxStores) : stores;

        return stores;
    }

    public int getNumberOfDocsWithStores(List<AutoSuggestDoc> autoSuggestDocs) {
        if (autoSuggestDocs.size() < 2) return autoSuggestDocs.size();
        AutoSuggestDoc firstDoc = autoSuggestDocs.get(0);
        AutoSuggestDoc secondDoc = autoSuggestDocs.get(1);
        return (secondDoc.getCtrObj().getPHits() >= 0.25 * firstDoc.getCtrObj().getPHits()) ? 2 : 1;
    }

    public static String removeAllStores(String store) {
        if (store == null) return "";
        String[] nodes = store.split("/");
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < nodes.length; i++) {
            if (allStores.contains(nodes[i])) continue;
            if (i == nodes.length - 1) sb.append(nodes[i]);
            else sb.append(nodes[i] + "/");
        }
        return sb.toString();
    }
}

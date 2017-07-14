package com.flipkart.sherlock.semantic.autosuggest.helpers;

import com.flipkart.sherlock.semantic.autosuggest.dao.StoreMarketPlaceDao;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import java.util.*;

/**
 * Created by dhruv.pancholi on 27/04/17.
 */
@Singleton
public class MarketAnalyzer {

    /**
     * Enum values, based on which the whole grocery filtering is based on
     */
    public static final String FLIP_KART = "flipkart";
    public static final String FLIP_MART = "flipmart";
    public static final String FLIP_FRESH = "flipfresh";

    public static final List<String> DEFAULT_MARKET_PLACE_IDS = Arrays.asList(FLIP_KART);

    @Inject
    private StoreMarketPlaceDao storeMarketPlaceDao;


    /**
     * Pre-computing the market place ids, in order to filter out documents required for grocery or not
     *
     * @param storeNodes
     * @return
     */
    public List<String> getMarketPlaceIds(Set<String> storeNodes) {

        Set<String> marketPlaceIds = new TreeSet<>();

        for (String store : storeNodes) {
            marketPlaceIds.add(getMarketPlaceId(store));
        }

        return new ArrayList<>(marketPlaceIds);
    }

    /**
     * Checks for all the paths, if it belongs to the grocery market place or not
     *
     * @param paths List of paths, not necessarily leafs
     * @return
     */
    public boolean containsGroceryPath(List<String> paths) {

        if (paths == null) return false;

        for (String leafPath : paths) {
            if (FLIP_MART.equals(getMarketPlaceId(leafPath))) return true;
        }

        return false;
    }

    /**
     * For a given path/store, identify if it belongs to flipkart/flipmart/flipfresh
     *
     * @param store
     * @return
     */
    public String getMarketPlaceId(String store) {
        if (store == null) return FLIP_KART;
        String[] nodes = store.split("/");
        return storeMarketPlaceDao.getMarketPlaceId(nodes[0]);
    }

    /**
     * Parse the params that comes from hudson and generate the list of market places
     *
     * @param groceryContext
     * @param marketPlaceId
     * @return
     */
    public List<String> getMarketPlaceIds(String contextStore, String groceryContext, String marketPlaceId) {
        if (groceryContext == null) groceryContext = "false";
        if (groceryContext.toLowerCase().equals("true")) return Arrays.asList(FLIP_MART);

        marketPlaceId = (marketPlaceId == null) ? FLIP_KART : marketPlaceId.toLowerCase();
        if (marketPlaceId.equals("grocery") || FLIP_MART.equals(marketPlaceId))
            return Arrays.asList(FLIP_KART, FLIP_MART);

        return Arrays.asList(FLIP_KART);
    }

    /**
     * For the given input query and market-place-ids, check if to remove the products from call
     *
     * @param query
     * @param marketPlaceIds
     * @return
     */
    public static boolean removeProducts(String query, List<String> marketPlaceIds) {
        return query == null || marketPlaceIds == null || query.length() < 4 || !marketPlaceIds.contains(FLIP_MART);
    }
}

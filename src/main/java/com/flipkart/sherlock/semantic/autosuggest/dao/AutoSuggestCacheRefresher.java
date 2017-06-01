package com.flipkart.sherlock.semantic.autosuggest.dao;

import com.google.inject.Inject;

import java.util.*;

/**
 * Created by dhruv.pancholi on 31/05/17.
 */
public class AutoSuggestCacheRefresher {

    private Map<String, AbstractReloadableCache> abstractReloadableCaches;

    @Inject
    public AutoSuggestCacheRefresher(RedirectionStoreDao redirectionStoreDao, AutoSuggestDisabledQueriesDao autoSuggestDisabledQueriesDao) {
        abstractReloadableCaches = new HashMap<>();
        abstractReloadableCaches.put(redirectionStoreDao.getClass().getSimpleName(), redirectionStoreDao);
        abstractReloadableCaches.put(autoSuggestDisabledQueriesDao.getClass().getSimpleName(), autoSuggestDisabledQueriesDao);
    }

    public Map<String, Integer> refreshCache(String daos) {
        Set<String> daoList = abstractReloadableCaches.keySet();
        if (daos != null && !daos.isEmpty()) daoList = new HashSet<>(Arrays.asList(daos.split(",")));

        Map<String, Integer> countMap = new HashMap<>();

        for (String dao : daoList) {
            if (abstractReloadableCaches.containsKey(dao)) {
                AbstractReloadableCache abstractReloadableCache = abstractReloadableCaches.get(dao);
                abstractReloadableCache.reloadCache();
                if (!(abstractReloadableCache instanceof AbstractReloadableMapCache)) continue;
                countMap.put(dao, ((AbstractReloadableMapCache) abstractReloadableCache).size());
            }
        }
        return countMap;
    }

    public Map<String, Object> cacheGet(String dao, String keys) {

        if (dao == null || dao.isEmpty()) throw new RuntimeException("Please provide a dao to access values for.");
        if (keys == null) throw new RuntimeException("Please add a parameter keys, leave empty to fetch all keys.");

        Set<String> keySet = new HashSet<>(Arrays.asList(keys.split(",")));
        keySet.remove("");

        AbstractReloadableCache abstractReloadableCache = abstractReloadableCaches.get(dao);
        if (abstractReloadableCache == null) throw new RuntimeException("Dao specified in request not found.");

        Map<String, Object> returnMap = new HashMap<>();

        Map<String, Object> daoGenericMap = (abstractReloadableCache instanceof AbstractReloadableMapCache) ? ((AbstractReloadableMapCache) abstractReloadableCache).getGenericMap() : new HashMap<>();

        if (keySet.isEmpty()) keySet = daoGenericMap.keySet();
        for (String key : keySet) returnMap.put(key, daoGenericMap.get(key));

        return returnMap;
    }
}

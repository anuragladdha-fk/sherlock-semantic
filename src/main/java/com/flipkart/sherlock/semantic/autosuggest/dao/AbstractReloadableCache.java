package com.flipkart.sherlock.semantic.autosuggest.dao;

import com.flipkart.sherlock.semantic.autosuggest.utils.JsonSeDe;
import com.flipkart.sherlock.semantic.common.dao.mysql.CompleteTableDao;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * Created by dhruv.pancholi on 31/05/17.
 * For all the classes which extends this class, make sure to register them with AutoSuggestCacheRefresher
 */
public abstract class AbstractReloadableCache<Type> {

    protected final CompleteTableDao completeTableDao;
    protected final JsonSeDe jsonSeDe;

    private LoadingCache<String, Type> cache;
    private static String KEY = "key";

    public AbstractReloadableCache(CompleteTableDao completeTableDao, JsonSeDe jsonSeDe, int refreshTime, TimeUnit unit) {
        this.completeTableDao = completeTableDao;
        this.jsonSeDe = jsonSeDe;
        cache = CacheBuilder.newBuilder()
                .maximumSize(1)
                .refreshAfterWrite(refreshTime, unit).build(new CacheLoader<String, Type>() {
                    @Override
                    public Type load(String s) throws Exception {
                        return getFromDB();
                    }
                });
        try {
            cache.get(KEY);
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }

    protected abstract Type getFromDB();

    public Type getCached() {
        try {
            return cache.get(KEY);
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void reloadCache() {
        for (String key : cache.asMap().keySet()) {
            cache.refresh(key);
        }
    }
}

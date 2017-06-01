package com.flipkart.sherlock.semantic.autosuggest.dao;

import com.flipkart.sherlock.semantic.autosuggest.utils.JsonSeDe;
import com.flipkart.sherlock.semantic.common.dao.mysql.CompleteTableDao;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Created by dhruv.pancholi on 31/05/17.
 */
public abstract class AbstractReloadableMapCache<ValueType> extends AbstractReloadableCache<Map<String, ValueType>> {

    public AbstractReloadableMapCache(CompleteTableDao completeTableDao, JsonSeDe jsonSeDe, int refreshTime, TimeUnit unit) {
        super(completeTableDao, jsonSeDe, refreshTime, unit);
    }

    public ValueType get(String key) {
        return getCached().get(key);
    }

    public int size() {
        return getCached().size();
    }

    public Map<String, ValueType> getMap() {
        return getCached();
    }

    public Map<String, Object> getGenericMap() {
        Map<String, Object> genericMap = new HashMap<>();
        Map<String, ValueType> map = getCached();
        Set<Entry<String, ValueType>> entrySet = map.entrySet();
        for (Entry<String, ValueType> stringValueTypeEntry : entrySet) {
            genericMap.put(stringValueTypeEntry.getKey(), stringValueTypeEntry.getValue());
        }
        return genericMap;
    }
}

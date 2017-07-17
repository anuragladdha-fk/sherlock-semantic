package com.flipkart.sherlock.semantic.autosuggest.dao;

import com.flipkart.sherlock.semantic.autosuggest.utils.JsonSeDe;
import com.flipkart.sherlock.semantic.common.dao.mysql.CompleteTableDao;
import com.flipkart.sherlock.semantic.common.dao.mysql.CompleteTableDao.AutoSuggestDisabled;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created by dhruv.pancholi on 31/05/17.
 */
@Singleton
public class AutoSuggestDisabledQueriesDao extends AbstractReloadableMapCache<String> {

    @Inject
    public AutoSuggestDisabledQueriesDao(CompleteTableDao completeTableDao, JsonSeDe jsonSeDe) {
        super(completeTableDao, jsonSeDe, 1, TimeUnit.DAYS);
    }

    @Override
    protected Map<String, String> getFromSource() {
        List<AutoSuggestDisabled> autoSuggestDisabledQueries = completeTableDao.getAutoSuggestDisabledQueries();
        Map<String, String> prefixMap = new HashMap<>();
        for (AutoSuggestDisabled autoSuggestDisabledQuery : autoSuggestDisabledQueries) {
            String prefix = autoSuggestDisabledQuery.getPrefix();
            if (prefix == null || prefix.isEmpty()) continue;
            prefixMap.put(prefix, null);
        }
        return prefixMap;
    }
}

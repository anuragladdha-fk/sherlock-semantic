package com.flipkart.sherlock.semantic.autosuggest.helpers;

import com.flipkart.sherlock.semantic.autosuggest.dao.AutoSuggestDisabledQueriesDao;
import com.google.inject.Inject;

import java.util.Map;

/**
 * Created by dhruv.pancholi on 31/05/17.
 */
public class AutoSuggestQueryAnalyzer {

    @Inject
    private AutoSuggestDisabledQueriesDao autoSuggestDisabledQueriesDao;

    public boolean isDisabled(String query) {
        if (query == null) return false;
        query = query.toLowerCase();

        Map<String, String> negativeMap = autoSuggestDisabledQueriesDao.getMap();
        if (negativeMap == null) return false;

        boolean isDisabled = false;
        for (int i = 1; i <= query.length(); i++) {
            String prefix = query.substring(0, i);
            isDisabled = negativeMap.containsKey(prefix.toLowerCase());
            if (isDisabled) break;
        }
        return isDisabled;
    }
}

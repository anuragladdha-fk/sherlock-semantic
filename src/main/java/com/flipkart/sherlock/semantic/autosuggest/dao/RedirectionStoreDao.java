package com.flipkart.sherlock.semantic.autosuggest.dao;

import com.flipkart.sherlock.semantic.autosuggest.utils.JsonSeDe;
import com.flipkart.sherlock.semantic.common.dao.mysql.CompleteTableDao;
import com.flipkart.sherlock.semantic.common.dao.mysql.CompleteTableDao.StorePathRedirect;
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
public class RedirectionStoreDao extends AbstractReloadableMapCache<String> {

    @Inject
    public RedirectionStoreDao(CompleteTableDao completeTableDao, JsonSeDe jsonSeDe) {
        super(completeTableDao, jsonSeDe, 1, TimeUnit.DAYS);
    }

    @Override
    protected Map<String, String> getFromSource() {
        List<StorePathRedirect> storePathRedirectList = completeTableDao.getStorePathRedirect();
        Map<String, String> storePathRedirectMap = new HashMap<>();
        for (StorePathRedirect storePathRedirect : storePathRedirectList) {
            String oldPath = storePathRedirect.getOldPath();
            String newPath = storePathRedirect.getNewPath();
            if (oldPath == null || oldPath.isEmpty()) continue;
            if (newPath == null || newPath.isEmpty()) continue;
            if (oldPath.startsWith("/")) oldPath = oldPath.substring(1);
            if (newPath.startsWith("/")) newPath = newPath.substring(1);
            storePathRedirectMap.put(oldPath, newPath);
        }
        return storePathRedirectMap;
    }
}

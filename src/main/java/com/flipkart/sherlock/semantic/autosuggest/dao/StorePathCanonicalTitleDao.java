package com.flipkart.sherlock.semantic.autosuggest.dao;

import com.fasterxml.jackson.core.type.TypeReference;
import com.flipkart.sherlock.semantic.autosuggest.utils.JsonSeDe;
import com.flipkart.sherlock.semantic.common.dao.mysql.CompleteTableDao;
import com.flipkart.sherlock.semantic.common.dao.mysql.CompleteTableDao.StorePathMetaData;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created by dhruv.pancholi on 30/05/17.
 */
@Singleton
public class StorePathCanonicalTitleDao extends AbstractReloadableMapCache<String> {


    private static TypeReference<Map<String, Object>> metadataTypereference = new TypeReference<Map<String, Object>>() {
    };

    @Inject
    public StorePathCanonicalTitleDao(CompleteTableDao completeTableDao, JsonSeDe jsonSeDe) {
        super(completeTableDao, jsonSeDe, 1, TimeUnit.DAYS);
    }

    @Override
    protected Map<String, String> getFromSource() {
        Map<String, String> storePathCanonicalTitleMap = new HashMap<>();

        List<StorePathMetaData> storePathMetaDatas = completeTableDao.getStorePathCanonicalTitles();
        for (StorePathMetaData storePathMetaData : storePathMetaDatas) {
            String metadata = storePathMetaData.getMetadata();
            if (metadata == null || metadata.isEmpty()) continue;
            Map<String, Object> metadataMap = jsonSeDe.readValue(metadata, metadataTypereference);
            if (!metadataMap.containsKey("canonical-title")) continue;
            String canonicalTitle = (String) metadataMap.get("canonical-title");
            if (canonicalTitle == null || canonicalTitle.isEmpty()) continue;

            String storePath = storePathMetaData.getStorePath();
            if (storePath == null || storePath.isEmpty()) continue;
            if (storePath.startsWith("/")) storePath = storePath.substring(1);

            storePathCanonicalTitleMap.put(storePath, canonicalTitle);
        }

        return storePathCanonicalTitleMap;
    }

    public String getCanonicalTitle(String storePath) {
        return getCached().get(storePath);
    }
}

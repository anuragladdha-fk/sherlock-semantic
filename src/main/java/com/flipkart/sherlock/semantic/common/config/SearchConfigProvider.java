package com.flipkart.sherlock.semantic.common.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.flipkart.sherlock.semantic.common.dao.mysql.ConfigsDao;
import com.flipkart.sherlock.semantic.common.dao.mysql.entity.SearchConfig;
import com.flipkart.sherlock.semantic.common.util.SerDeUtils;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListenableFutureTask;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by anurag.laddha on 15/04/17.
 */

@Singleton
public class SearchConfigProvider {

    private static final Logger log = LoggerFactory.getLogger(SearchConfigProvider.class);
    private static final String empty = "";
    private static final String DUMMY_KEY = "dummy";

    private LoadingCache<String, Map<Key, String>> searchConfigCache;
    private ConfigsDao configsDao;
    private ExecutorService executorService;
    private int cacheExpireSec;

    @Inject
    public SearchConfigProvider(ConfigsDao configsDao,
                                @Named(Constants.GUICE_LOCAL_CACHE_LOADING_EXECUTOR_SERVICE) ExecutorService executorService,
                                @Named(Constants.GUICE_LOCAL_CACHE_EXPIRY) int cacheExpireSec) {
        this.configsDao = configsDao;
        this.executorService = executorService;
        this.cacheExpireSec = cacheExpireSec;
        // todo - read maximumSize from config
        this.searchConfigCache = CacheBuilder.newBuilder().maximumSize(100).refreshAfterWrite(cacheExpireSec, TimeUnit.SECONDS)
            .build(new SearchConfigLoader(configsDao, executorService));
    }

    public <T> T getSearchConfig(String key, Class<T> type) {
        return getSearchConfigHelper(key, null, type);
    }


    public <T> T getSearchConfig(String key, String experiment, Class<T> type) {
        return getSearchConfigHelper(key, experiment, type);
    }

    //TODO this is non deterministic behavior
    public <T> T getSearchConfig(String key, Iterable<String> experiments, Class<T> type) {
        if (experiments != null){
            for (String currExperiment : experiments) {
                T configValue = getSearchConfig(key, currExperiment, type);
                if (configValue != null){
                    return configValue;
                }
            }
        }
        return getSearchConfig(key, empty, type);
    }


    public <T> T getSearchConfig(String key, TypeReference<T> typeReference) {
        return getSearchConfigHelper(key, null, typeReference);
    }

    public <T> T getSearchConfig(String key, String experiment, TypeReference<T> typeReference) {
        return getSearchConfigHelper(key, experiment, typeReference);
    }

    //TODO this is non deterministic behavior.
    public <T> T getSearchConfig(String key, Iterable<String> experiments, TypeReference<T> typeReference) {
        if (experiments != null){
            for (String currExperiment : experiments) {
                T configValue = getSearchConfig(key, currExperiment, typeReference);
                if (configValue != null){
                    return configValue;
                }
            }
        }
        return getSearchConfig(key, empty, typeReference);
    }


    private <T> T getSearchConfigHelper(String key, String experiment, Class<T> type) {
        if (!StringUtils.isBlank(key)) {
            Map<Key, String> cachedConfigs = null;
            try {
                cachedConfigs = this.searchConfigCache.get(DUMMY_KEY);
            } catch (Exception ex) {
                //TODO metrics and alert
                log.error("Could not load cache configs", ex);
            }

            if (cachedConfigs != null) {
                Key cacheKey = new Key(key, convertToBucket(experiment));
                String cachedValue = cachedConfigs.get(cacheKey);
                if (cachedValue != null) {
                    try {
                        return SerDeUtils.cast(cachedValue, type);
                    } catch (Exception ex) {
                        //TODO metrics and alert
                        log.error("Could not convert string: {} to type: {}", cachedValue, type.getName(), ex);
                    }
                }
            }
        }
        return null;
    }


    private <T> T getSearchConfigHelper(String key, String experiment, TypeReference<T> typeReference) {
        if (!StringUtils.isBlank(key)) {
            Map<Key, String> cachedConfigs = null;
            try {
                cachedConfigs = this.searchConfigCache.get(DUMMY_KEY);
            } catch (Exception ex) {
                //TODO metrics and alert
                log.error("Could not load cache configs", ex);
            }

            if (cachedConfigs != null) {
                Key cacheKey = new Key(key, convertToBucket(experiment));
                String cachedValue = cachedConfigs.get(cacheKey);
                if (cachedValue != null) {
                    try {
                        return SerDeUtils.castToGeneric(cachedValue, typeReference);
                    } catch (Exception ex) {
                        //TODO metrics and alert
                        log.error("Could not convert string: {} to type: {}", cachedValue, typeReference.getType().getTypeName(), ex);
                    }
                }
            }
        }
        return null;
    }


    private static String convertToBucket(String bucket){
        return StringUtils.isBlank(bucket) ? empty : bucket;
    }

    /**
     * Cache key. Config name and bucket forms the cache key
     */
    @Getter
    @AllArgsConstructor
    @EqualsAndHashCode
    @ToString
    static class Key {
        /**
         * Config name
         */
        private String config;
        /**
         * Bucket or experiment name
         */
        private String bucket;
    }


    static class SearchConfigLoader extends CacheLoader<String, Map<Key, String>> {

        private ConfigsDao configsDao;
        private ExecutorService executorService;

        public SearchConfigLoader(ConfigsDao configsDao, ExecutorService executorService) {
            this.configsDao = configsDao;
            this.executorService = executorService;
        }

        @Override
        public Map<Key, String> load(String s) throws Exception {
            log.info("Loading search configs");
            Map<Key, String> allSearchConfigs =  getAllSearchConfigs();
            log.info("Done loading search configs. Number of elements: {}", allSearchConfigs != null ? allSearchConfigs.size() : 0);
            return allSearchConfigs;
        }

        @Override
        public ListenableFuture<Map<Key, String>> reload(String key, Map<Key, String> oldValue) throws Exception {
            log.info("Queued loading search configs async");
            ListenableFutureTask<Map<Key, String>> task = ListenableFutureTask.create(() ->{
                log.info("Started loading search configs async");
                Map<Key, String> allSearchConfigs = getAllSearchConfigs();
                log.info("Finished fetching search configs async on diff thread. Number of elements : {}",
                    allSearchConfigs != null ? allSearchConfigs.size() : 0);
                return allSearchConfigs;
            });
            this.executorService.submit(task);
            return task;
        }

        @VisibleForTesting
        Map<Key, String> getAllSearchConfigs(){
            List<SearchConfig> allSearchConfigs = this.configsDao.getAllSearchConfigs();
            if (allSearchConfigs != null && allSearchConfigs.size() > 0){

                Map<Key, String> configToValueMap = new HashMap<>();
                Map<Key, Long> keyToValueModifiedTs = new HashMap<>();
                Key key;
                String bucket;

                for (SearchConfig currConfig : allSearchConfigs) {
                    bucket = convertToBucket(currConfig.getBucket());
                    key = new Key(currConfig.getName(), bucket);

                    if (configToValueMap.containsKey(key)){
                        //overwrite existing value if new value timestamp is higher
                        if (keyToValueModifiedTs.get(key) < currConfig.getLastModifiedTs()){
                            configToValueMap.put(key, currConfig.getConfigValue());
                            keyToValueModifiedTs.put(key, currConfig.getLastModifiedTs());
                        }
                    }
                    else{
                        configToValueMap.put(key, currConfig.getConfigValue());
                        keyToValueModifiedTs.put(key, currConfig.getLastModifiedTs());
                    }
                }
                return configToValueMap;
            }
            return null;
        }
    }
}

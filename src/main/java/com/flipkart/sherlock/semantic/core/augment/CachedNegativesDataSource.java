package com.flipkart.sherlock.semantic.core.augment;

import com.flipkart.sherlock.semantic.common.config.Constants;
import com.flipkart.sherlock.semantic.common.config.SearchConfigProvider;
import com.flipkart.sherlock.semantic.dao.mysql.AugmentationDao;
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
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Created by anurag.laddha on 18/04/17.
 */

@Singleton
public class CachedNegativesDataSource implements IDataNegatives {

    private static final Logger log = LoggerFactory.getLogger(CachedNegativesDataSource.class);

    private LoadingCache<String, Set<String>> negativeTermsCache;

    @Inject
    public CachedNegativesDataSource(AugmentationDao augmentationDao,
                                     SearchConfigProvider searchConfigProvider,
                                     @Named(Constants.GUICE_LOCAL_CACHE_LOADING_EXECUTOR_SERVICE) ExecutorService executorService,
                                     @Named(Constants.GUICE_LOCAL_CACHE_EXPIRY) int cacheExpireSec) {

        NegativeTermsDataLoader negativeTermsDataLoader = new NegativeTermsDataLoader(augmentationDao, executorService, searchConfigProvider);
        this.negativeTermsCache = CacheBuilder.newBuilder().maximumSize(10).refreshAfterWrite(cacheExpireSec, TimeUnit.SECONDS)
            .build(negativeTermsDataLoader);
    }

    @Override
    public boolean containsNegative(String term){
        if (!StringUtils.isBlank(term)) {
            Set<String> allNegatives = getNegativesFromCache();
            return allNegatives != null && allNegatives.contains(convertToKey(term));
        }
        return false;
    }

    public Set<String> getAllNegatives(){
        return getNegativesFromCache();
    }

    private Set<String> getNegativesFromCache(){
        Set<String> allNegatives = null;
        try {
            allNegatives = this.negativeTermsCache.get(Constants.DUMMY_KEY);
        }
        catch(Exception ex){
            log.error("Error in fetching negatives list", ex);
            //todo metric + alert
        }
        return allNegatives;
    }

    private String convertToKey(String term){
        return term != null ? term.trim().toLowerCase() : null;
    }

    @AllArgsConstructor
    static class NegativeTermsDataLoader extends CacheLoader<String, Set<String>> {

        private AugmentationDao augmentationDao;
        private ExecutorService executorService;
        private SearchConfigProvider searchConfigProvider;

        @Override
        public Set<String> load(String s) throws Exception {
            log.info("Fetching negative terms");
            Set<String> allNegatives = getAllNegativeTerms();
            log.info("Done fetching negative terms");
            return allNegatives;
        }

        @Override
        public ListenableFuture<Set<String>> reload(String key, Set<String> oldValue) throws Exception {
            log.info("Queued loading negative terms async");
            ListenableFutureTask<Set<String>> task = ListenableFutureTask.create(() ->{
                log.info("Started loading negative terms async");
                Set<String> allNegatives = getAllNegativeTerms();
                log.info("Finished fetching negative terms async on diff thread. Number of elements : {}",
                    allNegatives != null ? allNegatives.size() : 0);
                return allNegatives;
            });
            this.executorService.submit(task);
            return task;
        }

        Set<String> getAllNegativeTerms(){
            Set<String> allNegatives = new HashSet<>();

            try {
                addToSet(allNegatives, getStopwords());
                addToSet(allNegatives, loadNegatives());
            }
            catch(Exception ex){
                log.error("Could not fetch negatives", ex);
                //todo metrics, alerts
            }

            return allNegatives.stream().map(term -> term.trim().toLowerCase()).collect(Collectors.toSet());
        }

        private void addToSet(Set<String> copyTo, Set<String> copyFrom){
            if (copyFrom != null && copyFrom.size() > 0){
                copyTo.addAll(copyFrom);
            }
        }

        @VisibleForTesting
        Set<String> getStopwords(){
            boolean shouldNegate = SerDeUtils.getValueOrDefault(this.searchConfigProvider.getSearchConfig("shouldNegateStopWordsInAugment",
                Boolean.class), Boolean.FALSE);
            return shouldNegate ? Constants.stopWordsSet : null;
        }

        @VisibleForTesting
        Set<String> loadNegatives(){
            try {
                return this.augmentationDao.getNegatives();
            }
            catch(Exception e){
                log.error("Exception while getting negative list", e);
            }
            return null;
        }
    }
}

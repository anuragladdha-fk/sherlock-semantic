package com.flipkart.sherlock.semantic.core.augment;

import com.flipkart.sherlock.semantic.dao.mysql.AugmentationDao;
import com.flipkart.sherlock.semantic.dao.mysql.RawQueriesDao;
import com.flipkart.sherlock.semantic.dao.mysql.entity.AugmentationEntities.*;
import com.flipkart.sherlock.semantic.util.MapUtils;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Sets;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.inject.Inject;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by anurag.laddha on 12/04/17.
 */

//TODO data source can have interface
public class LocalCachedAugmentDataSource {

    private LoadingCache<String, TermAlternativesWrapper> augmentCache;
    private AugmentationDao augmentationDao;
    private RawQueriesDao rawQueriesDao;
    private ExecutorService executorService;

    @Inject
    public LocalCachedAugmentDataSource(AugmentationDao augmentationDao, RawQueriesDao rawQueriesDao, ExecutorService executorService,
                                        int cacheExpireSec) {
        this.augmentationDao = augmentationDao;
        this.rawQueriesDao = rawQueriesDao;
        this.executorService = executorService;
        DataLoader dataLoader = new DataLoader(augmentationDao, rawQueriesDao, executorService);
        this.augmentCache = CacheBuilder.newBuilder().maximumSize(10).refreshAfterWrite(cacheExpireSec, TimeUnit.SECONDS).build(dataLoader);
    }

    /**
     * Data coming from multiple tables, for different type of replacements, prevent map merges
     */

    static class TermAlternativesWrapper {
        @Getter
        private Map<String, Set<AugmentAlternative>> termToAlternativesMap;
        @Getter
        private Map<String, Set<AugmentAlternative>> queryToAlternativesMap;

        public TermAlternativesWrapper() {
            this.termToAlternativesMap = new HashMap<>();
            this.queryToAlternativesMap = new HashMap<>();
        }

        public void addTermAlternatives(String term, Set<AugmentAlternative> alternatives){
            MapUtils.addEntriesToTargetMapValueSet(this.termToAlternativesMap, convertToKey(term), alternatives);
        }

        public void addQueryAlternatives(String query, Set<AugmentAlternative> alternatives){
            MapUtils.addEntriesToTargetMapValueSet(this.queryToAlternativesMap, convertToKey(query), alternatives);
        }

        public Set<AugmentAlternative> getTermAlternatives(String term){
            return this.termToAlternativesMap != null ? this.termToAlternativesMap.get(convertToKey(term)) : null;
        }

        public Set<AugmentAlternative> getQueryAlternatives(String query){
            return this.queryToAlternativesMap != null ? this.queryToAlternativesMap.get(convertToKey(query)) : null;
        }
    }


    /**
     * Type of augmentation data
     * Need not be visible to the external world
     */
    private static enum AugInfoType {
        TermToTerm, QueryToQuery, Negatives
    }

    /**
     * Cache data loader
     * Need not be visible to the external world
     */
    static class DataLoader extends CacheLoader<String, TermAlternativesWrapper>{

        private AugmentationDao augmentationDao;
        private RawQueriesDao rawQueriesDao;
        private ExecutorService executorService;

        //TODO this can come from config
        private static final Set<String> stopWordsSet = Sets.newHashSet("&", "a", "an", "and", "are", "as", "at", "be", "but", "by", "for", "if", "in", "into", "is", "it", "no",
            "not", "of", "on", "or", "such", "that", "the", "their", "then", "there", "these", "they", "this", "to", "was", "will", "with");

        public DataLoader(AugmentationDao augmentationDao, RawQueriesDao rawQueriesDao, ExecutorService executorService) {
            this.augmentationDao = augmentationDao;
            this.rawQueriesDao = rawQueriesDao;
            this.executorService = executorService;
        }

        @Override
        public TermAlternativesWrapper load(String s) throws Exception {
            return null;
        }

        @Override
        public ListenableFuture<TermAlternativesWrapper> reload(String key, TermAlternativesWrapper oldValue) throws Exception {
            return super.reload(key, oldValue);
        }


        private Set<String> getStopwords(){
            //TODO get from search config if we need to 'shouldNegateStopWordsInAugment'.
            return stopWordsSet;
        }

        @VisibleForTesting
        TermAlternativesWrapper getSynonymAugmentations(AugmentationDao augmentationDao) {
            /**
             * Replace type synonym
             *      term --> rep1, rep2
             *      Cache:
             *      term --> rep1, rep2
             *
             * for non-replace synonym types (term, query), original term and alternatives form the complete alternative set
             * and secondly, original term and synonyms form each others alternatives
             * Example
             *       termA as synonym as syn1 and syn2
             *       All synonym alternatives: termA, syn1, syn2
             *       Cache:
             *          termA --> termA, syn1, syn2
             *          syn1 --> termA, syn1, syn2
             *          syn2 --> termA, syn1, syn2
             */
            if (augmentationDao != null) {
                List<Synonym> allSynonyms = augmentationDao.getSynonyms();

                if (allSynonyms != null && allSynonyms.size() > 0) {
                    TermAlternativesWrapper termAlternativesWrapper = new TermAlternativesWrapper();
                    Set<String> currItemSynonyms;
                    Set<String> augmentations;

                    for (Synonym currSynonym : allSynonyms) {
                        if (!StringUtils.isBlank(currSynonym.getSynonyms())) {

                            augmentations = new HashSet<String>();
                            currItemSynonyms = Sets.newHashSet(currSynonym.getSynonyms().split(","));

                            if (!currSynonym.getSynType().equals(Synonym.Type.replace)) {
                                currItemSynonyms.add(currSynonym.getTerm());;
                            }

                            for (String synonym : currItemSynonyms) {
                                synonym = synonym.replaceAll("\\s+", " ").trim();
                                // in case of replace, mostly we have query to query replace and putting an extra braces is not what we want.
                                if (synonym.contains(" ") && !currSynonym.getSynType().equals(Synonym.Type.replace)) {
                                    augmentations.add("(" + synonym + ")");
                                } else {
                                    augmentations.add(synonym);
                                }
                            }

                            if (currSynonym.getSynType().equals(Synonym.Type.replace)) {//query to query alternative
                                Set<AugmentAlternative> augmentAlternatives = createAugmentationEntries(currSynonym.getTerm(),
                                    augmentations, AugmentationConstants.CONTEXT_DEFAULT, AugmentAlternative.Type.Synonym, 0);
                                //add to query to query map
                                termAlternativesWrapper.addQueryAlternatives(currSynonym.getTerm(), augmentAlternatives);
                            }
                            else {//term to term alternatives
                                for (String singleSynonym : currItemSynonyms) {
                                    //All alternatives are added to each of the synonym
                                    Set<AugmentAlternative> currAlternatives = createAugmentationEntries(singleSynonym,
                                        augmentations, AugmentationConstants.CONTEXT_DEFAULT, AugmentAlternative.Type.Synonym, 0);
                                    termAlternativesWrapper.addTermAlternatives(singleSynonym, currAlternatives);
                                }
                            }
                        }
                    }
                    return termAlternativesWrapper;
                }
            }
            return null;
        }

        @VisibleForTesting
        Set<AugmentAlternative> createAugmentationEntries(String original, Set<String> augmentationSet, String context,
                                                          AugmentAlternative.Type type, float confidence) {
            if (!StringUtils.isBlank(original) && augmentationSet != null && augmentationSet.size() > 0
                && context != null && type != null){
                Set<AugmentAlternative> augmentAlternatives = new HashSet<>();
                for (String currAug : augmentationSet) {
                    augmentAlternatives.add(new AugmentAlternative(original, currAug, context, type, confidence));
                }
                return augmentAlternatives;
            }
            return null;
        }
    }

    private static String convertToKey(String text) {
        return StringUtils.lowerCase(StringUtils.trimToEmpty(text));
    }
}

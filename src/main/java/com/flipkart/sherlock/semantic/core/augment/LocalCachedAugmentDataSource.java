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
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by anurag.laddha on 12/04/17.
 */

//TODO data source can have interface

@Slf4j
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
        TermAlternativesWrapper getSynonymAugmentations() {
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
            if (this.augmentationDao != null) {
                List<Synonym> allSynonyms = this.augmentationDao.getSynonyms();

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
                                addQueryAlternatives(termAlternativesWrapper, currSynonym.getTerm(), augmentations,
                                    AugmentationConstants.CONTEXT_DEFAULT, AugmentAlternative.Type.Synonym, 0);
                            }else {//term to term alternatives
                                for (String singleSynonym : currItemSynonyms) {
                                    //All alternatives are added to each of the synonym
                                    addTermAlternatives(termAlternativesWrapper, singleSynonym, augmentations, AugmentationConstants.CONTEXT_DEFAULT,
                                        AugmentAlternative.Type.Synonym, 0);
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
        TermAlternativesWrapper loadCompoundWords(boolean useNewCompounds) {

            List<BiCompound> biCompoundList = null;
            if (useNewCompounds) {
                EntityMeta compoundTableMeta = this.augmentationDao.getEntityMeta("bi_compound");
                log.info("Fetching data from compound table {} having base name: {}", compoundTableMeta.getLatestEntityTable(),
                    compoundTableMeta.getBaseTable());
                biCompoundList = this.rawQueriesDao.getAugmentationCompounds(compoundTableMeta.getLatestEntityTable());
            }
            else{
                biCompoundList = this.augmentationDao.getOldCompounds();
            }

            if (biCompoundList != null && biCompoundList.size() > 0) {
                TermAlternativesWrapper termAlternativesWrapper = new TermAlternativesWrapper();

                for (BiCompound currBiCompound : biCompoundList) {

                    String unigram = currBiCompound.getUnigram();
                    String bigram = currBiCompound.getBigram();
                    String correct = currBiCompound.getCorrect();

                    float conf = getConfidenceForCompoundWordEntry(correct, currBiCompound.getUnigramPHits(),
                        currBiCompound.getUnigramSHits(), currBiCompound.getBigramPHits(), currBiCompound.getBigramSHits());

                    Set<String> augmentations = new HashSet<String>(Arrays.asList(unigram, "(" + bigram + ")"));
                    if ("both".equalsIgnoreCase(correct)) {
                        if (isValid(unigram, bigram)) {
                            addTermAlternatives(termAlternativesWrapper, bigram, augmentations, AugmentationConstants.CONTEXT_DEFAULT,
                                AugmentAlternative.Type.CompundWord, conf);
                        }
                        addTermAlternatives(termAlternativesWrapper, unigram, augmentations, AugmentationConstants.CONTEXT_DEFAULT,
                            AugmentAlternative.Type.CompundWord, conf);
                    }
                    else if ("unigram".equals(correct)) {
                        if (isValid(unigram, bigram)) {
                            addTermAlternatives(termAlternativesWrapper, bigram, augmentations, AugmentationConstants.CONTEXT_DEFAULT,
                                AugmentAlternative.Type.CompundWord, conf);
                        }
                    }
                    else if ("bigram".equals(correct)) {
                        addTermAlternatives(termAlternativesWrapper, unigram, augmentations, AugmentationConstants.CONTEXT_DEFAULT,
                            AugmentAlternative.Type.CompundWord, conf);
                    }
                }
                return termAlternativesWrapper;
            }
            return null;
        }


        private void addTermAlternatives(TermAlternativesWrapper termAlternativesWrapper, String original, Set<String> augmentationSet,
                                         String context, AugmentAlternative.Type type, float confidence){
            Set<AugmentAlternative> augmentAlternatives = createAugmentationEntries(original,
                augmentationSet, context, type, confidence);
            //add to term to term map
            termAlternativesWrapper.addTermAlternatives(original, augmentAlternatives);
        }

        private void addQueryAlternatives(TermAlternativesWrapper termAlternativesWrapper, String original, Set<String> augmentationSet,
                                          String context, AugmentAlternative.Type type, float confidence){
            Set<AugmentAlternative> augmentAlternatives = createAugmentationEntries(original,
                augmentationSet, context, type, confidence);
            //add to query to query map
            termAlternativesWrapper.addQueryAlternatives(original, augmentAlternatives);
        }

        private boolean isValid(String unigram, String bigram) {

//            if (!StringUtils.isBlank(unigram) && !StringUtils.isBlank(bigram)){
//                Set<String> bigramTokens = Lists.newArrayList(StringUtils.split(bigram, ",")).stream().map(String::trim).collect(Collectors.toSet());
//                return !(stopWordsSet.containsAll(bigramTokens));
//            }
//            return false;

            Set<String> bigramTokens = new HashSet<String>(Arrays.asList(StringUtils.split(bigram, " ,")));
            if (stopWordsSet.containsAll(bigramTokens)) {
                return false;
            } else {
                return true;
            }
        }


        //Copied as-is from previous version of semantic
        private float getConfidenceForCompoundWordEntry(String correct, float uni_phits, float uni_shits, float bi_phits,
                                                        float bi_shits) {

            float conf = 0;

            float correct_phits = 0.0f;
            float correct_shits = 0.0f;
            float incorrect_phits = 0.0f;
            float incorrect_shits = 0.0f;

            if (correct.equalsIgnoreCase("unigram")) {
                correct_phits = uni_phits;
                correct_shits = uni_shits;
                incorrect_phits = bi_phits;
                incorrect_shits = bi_shits;
            }
            else if (correct.equalsIgnoreCase("bigram")) {
                correct_phits = bi_phits;
                correct_shits = bi_shits;
                incorrect_phits = uni_phits;
                incorrect_shits = uni_shits;
            }
            else { //if its unspecified or both are correct.
                return 0;
            }

            // the incorrect guys occurs with some frequency, the correct guy has decent phits.
            // and the correct is order of magnitude better than incorrect.
            if (incorrect_shits > 5 && correct_phits > 50 &&
                correct_shits > 5*incorrect_shits &&
                correct_phits > 5*incorrect_phits) {
                conf = correct_phits/ (incorrect_phits < 1 ? 1: incorrect_phits);
            }

            return conf;
        }


        private Set<AugmentAlternative> createAugmentationEntries(String original, Set<String> augmentationSet, String context,
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

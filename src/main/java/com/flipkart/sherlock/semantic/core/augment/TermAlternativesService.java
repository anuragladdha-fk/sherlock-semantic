package com.flipkart.sherlock.semantic.core.augment;

import com.flipkart.sherlock.semantic.common.config.Constants;
import com.flipkart.sherlock.semantic.common.QueryContainer;
import com.flipkart.sherlock.semantic.common.lucene.FKTokenFilter;
import com.flipkart.sherlock.semantic.common.lucene.FkTokenizerFactory;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Created by anurag.laddha on 22/04/17.
 */

@Singleton
public class TermAlternativesService {

    private static final Set<AugmentAlternative> emptyAlternativeSet = new HashSet<>();

    private final AugmentationConfigProvider augmentationConfigProvider;
    private final AugmentDataAlgoFactory augmentDataAlgoFactory;

    static final Pattern ALPHANUM_PATTERN = Pattern.compile("[0-9]{2,}[a-z]{2,}|[a-z]{2,}[0-9]{2,}", Pattern.CASE_INSENSITIVE);  //atleast 2 numbers and 2 alphabets
    static final Pattern SPLIT_ALPHABETS_NUMBERS_PATTERN = Pattern.compile("([0-9]+[.][0-9]+|[0-9]+|[a-z]+)", Pattern.CASE_INSENSITIVE);  //helps extract either number or letter or number with decimal

    @Inject
    public TermAlternativesService(AugmentationConfigProvider augmentationConfigProvider,
                                   AugmentDataAlgoFactory augmentDataAlgoFactory) {
        this.augmentationConfigProvider = augmentationConfigProvider;
        this.augmentDataAlgoFactory = augmentDataAlgoFactory;
    }

    @VisibleForTesting
    Set<AugmentAlternative> getTermAlternativesHelper(String term, String contextKey){
        /**
         * Get term alternatives from suitable data source and drop the ones that are from disabled context
         */
        if (!StringUtils.isBlank(term) && shouldAugment(term)){
            Set<AugmentAlternative> filteredTermAlternatives = new HashSet<>();

            IDataTermAlternatives termAlternativesDS = this.augmentDataAlgoFactory.getTermAlternativesDataSource(null);
            if (termAlternativesDS != null) {
                Set<AugmentAlternative> termAlternatives = termAlternativesDS.getTermAlternatives(term);
                Set<String> disabledContext = this.augmentationConfigProvider.getAllDisabledContext(contextKey);

                //Remove alternatives from disabled context
                if (disabledContext != null && disabledContext.size() > 0) {
                    for (AugmentAlternative currTermAlt : termAlternatives) {
                        if (StringUtils.isBlank(currTermAlt.getContext())
                            || !disabledContext.contains(currTermAlt.getContext().trim().toLowerCase())) {
                            filteredTermAlternatives.add(currTermAlt);
                        }
                    }
                } else {
                    filteredTermAlternatives = termAlternatives;
                }
                return filteredTermAlternatives;
            }
        }
        return emptyAlternativeSet;
    }

    @VisibleForTesting
    Set<AugmentAlternative> getQueryAlternativesHelper(String query, String contextKey){
        /**
         * Get query alternatives from suitable data source and drop the ones that are from disabled context
         */
        if (!StringUtils.isBlank(query) && shouldAugment(query)){
            Set<AugmentAlternative> filteredQueryAlternatives = new HashSet<>();

            IDataTermAlternatives termAlternativesDS = this.augmentDataAlgoFactory.getTermAlternativesDataSource(null);
            if (termAlternativesDS != null) {
                Set<AugmentAlternative> queryAlternatives = termAlternativesDS.getQueryAlternatives(query);
                Set<String> disabledContext = this.augmentationConfigProvider.getAllDisabledContext(contextKey);

                //Remove alternatives from disabled context
                if (disabledContext != null && disabledContext.size() > 0) {
                    for (AugmentAlternative currQueryAlt : queryAlternatives) {
                        if (StringUtils.isBlank(currQueryAlt.getContext())
                            || !disabledContext.contains(currQueryAlt.getContext().trim().toLowerCase())) {
                            filteredQueryAlternatives.add(currQueryAlt);
                        }
                    }
                } else {
                    filteredQueryAlternatives = queryAlternatives;
                }
                return filteredQueryAlternatives;
            }
        }
        return emptyAlternativeSet;
    }

    public boolean shouldAugment(String term){
        IDataNegatives negativesDS = this.augmentDataAlgoFactory.getNegativesDataSource(null);
        if (negativesDS != null){
            return !negativesDS.containsNegative(term);
        }
        return true;
    }

    /**
     * Looks for query to query alternatives (only)
     */
    public QueryContainer getQueryAlternatives(String query, String contextKey){  //equivalent of AugmenterImpl.augmentQueryToQuery

        QueryContainer queryAugmentInfo = new QueryContainer(query);
        if (contextKey == null) contextKey = Constants.CONTEXT_DEFAULT;  //todo check for blank instead?

        if (shouldAugment(query)){
            query = StringUtils.replace(query, ":", " ");
            Set<AugmentAlternative> augmentEntries = this.getQueryAlternativesHelper(query, contextKey);
            if (augmentEntries != null && augmentEntries.size() > 0){
                queryAugmentInfo.setType(getAugmentationTypes(augmentEntries));
                AugmentAlternative firstEntry = augmentEntries.iterator().next();      //query to query will always have 1 alternative only
                queryAugmentInfo.setIdentifiedBestQuery(firstEntry.getAugmentation());
                queryAugmentInfo.setModified(true);
                //we don't want to show the change to user.
                boolean replaceNoShow = AugmentAlternative.Type.replaceNoShow.name().equalsIgnoreCase(firstEntry.getType());
                queryAugmentInfo.setShowAugmentation(!replaceNoShow);
                queryAugmentInfo.setLuceneQuery(orTerms(query, getAllAugmentations(augmentEntries)));
            }
        }
        return queryAugmentInfo;
    }

    /**
     * Split given string into tokens
     * For each position, try with largest set of consecutive terms for which there is term alternative available, else keep reducing window size
     * @param qstr
     * @param contextKey
     * @return
     */
    //todo:note not porting preProcessOr
    public QueryContainer splitTermsAndGetAlternatives(String qstr, String contextKey) {  //equivalent of AugmenterImpl.doTermWiseSynonymsAndSpellCorrection
        QueryContainer queryAugmentInfo = new QueryContainer(qstr);

        if(shouldAugment(qstr)){
            Set<AugmentAlternative> augmentEntriesForPhrases = new HashSet<AugmentAlternative>();
            StringBuilder sb = new StringBuilder();
            String[] terms = getTokens(qstr);
            for (int startIndex = 0; startIndex < terms.length; ) {
                boolean foundAlternative = false;
                //For given start position, create window of size 3 and look for alternatives, else reduce window size and repeat.
                for (int numTerms = 3; numTerms > 0; numTerms--) {
                    if (getTermRangeAlternatives(terms, queryAugmentInfo, sb, augmentEntriesForPhrases,
                        startIndex, numTerms, contextKey)){
                        startIndex = startIndex + numTerms; //we have found alternative, skip these terms and start with term at next index
                        foundAlternative = true;
                        break;
                    }
                }
                if (!foundAlternative) {
                    sb.append(" ").append(terms[startIndex]);  //If no match found, use original term as is
                    startIndex++;
                }
            }
            String luceneQuery = org.apache.velocity.util.StringUtils.collapseSpaces(sb.toString());
            queryAugmentInfo.setType(getAugmentationTypes(augmentEntriesForPhrases));
            queryAugmentInfo.setLuceneQuery(queryAugmentInfo.isModified()? luceneQuery : queryAugmentInfo.getOriginalQuery());
        }
        return queryAugmentInfo;
    }


    /**
     * For terms in given range, determine if there is term alternative available.
     * if not, if term is alphanumeric, split numbers and alphanets and determine alternatives
     * @param terms: list of terms
     * @param queryAugmentInfo
     * @param sb: append to creation of lucene query
     * @param allAugmentations: set to append to if augmentations found
     * @param startIndex: start index from list of terms
     * @param numElements: number of elements to consider from list of terms, beginning from startindex
     * @param contextKey
     * @return
     */
    //TODO fix method params and return type. Dont modify state of params. Create a new type and return all modified aspects.
    public boolean getTermRangeAlternatives(String[] terms, QueryContainer queryAugmentInfo,
                                            StringBuilder sb, //to create lucene query
                                            Set<AugmentAlternative> allAugmentations, //adds to alteratives
                                            int startIndex, int numElements, String contextKey) {   //equivalent of AugmenterImpl.augmentShingle
        boolean foundAlternatives = false;
        if (startIndex + numElements <= terms.length) {
            String gram = StringUtils.join(terms, ' ', startIndex, startIndex + numElements);
            Set<String> currAugmentations = null;
            Set<AugmentAlternative> augmentEntries = null;

            augmentEntries = getTermAlternativesHelper(gram, contextKey);  //determine term to term alternatives
            currAugmentations = getAllAugmentations(augmentEntries);

            if (currAugmentations != null && !currAugmentations.isEmpty()) {
                queryAugmentInfo.addAugmentation(gram, currAugmentations);
                queryAugmentInfo.setModified(true);
                sb.append(" ").append(orTerms(gram, currAugmentations));
                foundAlternatives = true;
                if (augmentEntries != null) {
                    allAugmentations.addAll(augmentEntries);
                }
            }
            //If single term has no alternatives, evaluate if it has numbers and alphabets mixed. Find alternatives separately for them
            else if (numElements == 1 && isAlphaNum(gram)) {
                List<String> tokens = splitAlphabetsAndNumbers(gram);
                String[] nestedTerms = tokens.toArray(new String[tokens.size()]);
                StringBuilder alternateQuery = new StringBuilder();
                alternateQuery.append("(");
                int i = 0;
                for (String t : tokens) {
                    QueryContainer qInfo = new QueryContainer(t);
                    getTermRangeAlternatives(nestedTerms, qInfo, alternateQuery, allAugmentations, i, 1, contextKey);
                    if (qInfo.isModified()) {
                        queryAugmentInfo.setAugmentations(qInfo.getAugmentations());
                        queryAugmentInfo.setModified(true);
                        foundAlternatives = true;
                    } else {
                        alternateQuery.append(" ").append(t);   //if alternatives are not found, append token as-is
                    }
                    i++;
                }
                alternateQuery.append(")");
                if (foundAlternatives) {
                    sb.append(" ").append(orTerms(gram, new HashSet<String>(Arrays.asList(gram, alternateQuery.toString()))));  //add original term to alternatives from parts created
                }
            }
        }
        return foundAlternatives;
    }


    private Set<String> getAllAugmentations(Set<AugmentAlternative> augmentEntries) {
        return augmentEntries != null
            ? augmentEntries.stream().map(AugmentAlternative::getAugmentation).collect(Collectors.toSet())
            : new HashSet<>();
    }


    @VisibleForTesting
    String getAugmentationTypes(Set<AugmentAlternative> augmentations) { //in previous version "augment" used to be returned as 1st type always (sorting didnt consider that alement)
        String type = "augment";

        if (augmentations != null && augmentations.size() > 0) {
            // get all the unique types.
            HashSet<String> types = new HashSet<String>();
            types.add(type);
            for (AugmentAlternative entry : augmentations) {
                if (!types.contains(entry.getType())) {
                    types.add(entry.getType());
                }
            }

            List<String> sortedList = Lists.newArrayList(types);
            Collections.sort(sortedList); // sort the types alphabetically.
            type = String.join(",", sortedList);
        }
        return type;
    }

    @VisibleForTesting
    String orTerms(String originalTerm, Set<String> terms) {
        if (terms == null || terms.isEmpty()) {
            return originalTerm;
        } else if (terms.size() == 1) {
            return terms.iterator().next();
        }
        return "(" + String.join(" OR ", terms) + ")";
    }

    boolean isAlphaNum(String term) {
        return ALPHANUM_PATTERN.matcher(term).find();
    }

    /**
     * Separates numbers and alphabets
     */
    List<String> splitAlphabetsAndNumbers(String term) {
        Matcher m = SPLIT_ALPHABETS_NUMBERS_PATTERN.matcher(term);
        List<String> groups = new LinkedList<String>();
        while (m.find()) {
            String g1 = m.group(1);
            groups.add(g1);
        }
        return groups;
    }


    /**
     * Tokenises string as per lucene's standard tokeniser
     */
    String[] getTokens(String qstr) {  //todo why array and not a list?
        try {
            FKTokenFilter tokenizer = new FKTokenFilter(FkTokenizerFactory.standardTokenizer(qstr));
            List<String> terms = tokenizer.getTokens();
            IOUtils.closeQuietly(tokenizer);
            return terms.toArray(new String[terms.size()]);
        } catch (IOException e) {
            return new String[] { qstr };
        }
    }
}

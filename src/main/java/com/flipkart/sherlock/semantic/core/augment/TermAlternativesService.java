package com.flipkart.sherlock.semantic.core.augment;

import com.flipkart.sherlock.semantic.config.Constants;
import com.flipkart.sherlock.semantic.core.common.QueryContainer;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by anurag.laddha on 22/04/17.
 */
@Singleton
public class TermAlternativesService {

    private static final Set<AugmentAlternative> emptyAlternativeSet = new HashSet<>();

    private AugmentationConfigProvider augmentationConfigProvider;
    private LocalCachedTermAlternativesDataSource localCachedTermAlternativesDataSource;
    private CachedNegativesDataSource cachedNegativesDataSource;

    @Inject
    public TermAlternativesService(AugmentationConfigProvider augmentationConfigProvider,
                                   LocalCachedTermAlternativesDataSource localCachedTermAlternativesDataSource,
                                   CachedNegativesDataSource cachedNegativesDataSource) {
        this.augmentationConfigProvider = augmentationConfigProvider;
        this.localCachedTermAlternativesDataSource = localCachedTermAlternativesDataSource;
        this.cachedNegativesDataSource = cachedNegativesDataSource;
    }

    @VisibleForTesting
    Set<AugmentAlternative> getTermAlternativesHelper(String term, String contextKey){
        /**
         * Get term alternatives from suitable data source and drop the ones that are from disabled context
         */
        if (!StringUtils.isBlank(term) && shouldAugment(term)){
            Set<AugmentAlternative> filteredTermAlternatives = new HashSet<>();

            Set<AugmentAlternative> termAlternatives = this.localCachedTermAlternativesDataSource.getTermAlternatives(term);
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
        return emptyAlternativeSet;
    }

    @VisibleForTesting
    Set<AugmentAlternative> getQueryAlternativesHelper(String query, String contextKey){
        /**
         * Get query alternatives from suitable data source and drop the ones that are from disabled context
         */
        if (!StringUtils.isBlank(query) && shouldAugment(query)){
            Set<AugmentAlternative> filteredQueryAlternatives = new HashSet<>();

            Set<AugmentAlternative> queryAlternatives = this.localCachedTermAlternativesDataSource.getQueryAlternatives(query);
            Set<String> disabledContext = this.augmentationConfigProvider.getAllDisabledContext(contextKey);

            //Remove alternatives from disabled context
            if (disabledContext != null && disabledContext.size() > 0) {
                for (AugmentAlternative currQueryAlt : queryAlternatives) {
                    if (StringUtils.isBlank(currQueryAlt.getContext())
                        || !disabledContext.contains(currQueryAlt.getContext().trim().toLowerCase())) {
                        filteredQueryAlternatives.add(currQueryAlt);
                    }
                }
            }
            else {
                filteredQueryAlternatives = queryAlternatives;
            }
            return filteredQueryAlternatives;
        }
        return emptyAlternativeSet;
    }

    public boolean shouldAugment(String term){
        return !this.cachedNegativesDataSource.containsNegative(term);
    }


    public QueryContainer getQueryAlterntaives(String qstr, String contextKey){  //equivalent of AugmenterImpl.augmentQueryToQuery

        QueryContainer queryAugmentInfo = new QueryContainer(qstr);
        if (contextKey == null) contextKey = Constants.CONTEXT_DEFAULT;  //todo check for blank instead?

        if (shouldAugment(qstr)){
            qstr = StringUtils.replace(qstr, ":", " ");
            Set<AugmentAlternative> augmentEntries = this.getQueryAlternativesHelper(qstr, contextKey);
            if (augmentEntries != null && augmentEntries.size() > 0){
                queryAugmentInfo.setType(getAugmentationTypes(augmentEntries));
                AugmentAlternative firstEntry = augmentEntries.iterator().next();      //query to query will always have 1 alternative only
                queryAugmentInfo.setIdentifiedBestQuery(firstEntry.getAugmentation());
                queryAugmentInfo.setModified(true);
                //we don't want to show the change to user.
                boolean replaceNoShow = AugmentAlternative.Type.replaceNoShow.name().equalsIgnoreCase(firstEntry.getType());
                queryAugmentInfo.setShowAugmentation(!replaceNoShow);
                queryAugmentInfo.setLuceneQuery(orTerms(qstr, getAllAugmentations(augmentEntries)));
            }
        }
        return queryAugmentInfo;
    }


    Set<String> getAllAugmentations(Set<AugmentAlternative> augmentEntries) {
        return augmentEntries != null
            ? augmentEntries.stream().map(AugmentAlternative::getAugmentation).collect(Collectors.toSet())
            : new HashSet<>();
    }


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

    //from previous version
    String orTerms(String originalTerm, Set<String> terms) {
        if (terms == null || terms.isEmpty()) {
            return originalTerm;
        } else if (terms.size() == 1) {
            return terms.iterator().next();
        }
        return "(" + String.join(" OR ", terms) + ")";
    }
}

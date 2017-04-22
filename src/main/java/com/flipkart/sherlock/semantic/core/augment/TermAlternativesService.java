package com.flipkart.sherlock.semantic.core.augment;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.apache.commons.lang3.StringUtils;

import java.util.HashSet;
import java.util.Set;

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

    public Set<AugmentAlternative> getTermAlternatives(String term, String contextKey){
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

    public Set<AugmentAlternative> getQueryAlternatives(String query, String contextKey){
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
}

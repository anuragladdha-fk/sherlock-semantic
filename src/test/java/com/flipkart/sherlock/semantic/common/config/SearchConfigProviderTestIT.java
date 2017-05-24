package com.flipkart.sherlock.semantic.common.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.flipkart.sherlock.semantic.common.util.TestContext;
import junit.framework.Assert;
import org.junit.Test;

import java.util.Map;
import java.util.Set;

/**
 * Created by anurag.laddha on 16/04/17.
 */

/**
 * Integration test for search config provider.
 * File name must end in 'IT'
 */
public class SearchConfigProviderTestIT {

    @Test
    public void testGetValues() throws Exception{
        SearchConfigProvider searchConfigProvider = TestContext.getInstance(SearchConfigProvider.class);

        String topStoreList = searchConfigProvider.getSearchConfig("TopStoreList", String.class);
        System.out.println(topStoreList);
        Assert.assertNotNull(topStoreList);

        Double maxNormalizedScoreForTextualIntents = searchConfigProvider.getSearchConfig("maxNormalizedScoreForTextualIntents", Double.class);
        System.out.println(maxNormalizedScoreForTextualIntents);
        Assert.assertTrue(maxNormalizedScoreForTextualIntents != 0.0);

        Boolean wrapUnKnownFacetIntents = searchConfigProvider.getSearchConfig("wrapUnKnownFacetIntents", Boolean.class);
        System.out.println(wrapUnKnownFacetIntents);
        Assert.assertNotNull(wrapUnKnownFacetIntents);

        Set<String> solrKeyWordsSet = searchConfigProvider.getSearchConfig("solr-caps-keywords", new TypeReference<Set<String>>() {});
        System.out.println(solrKeyWordsSet);
        Assert.assertTrue(solrKeyWordsSet.size() > 0);

        Map<String, String> autoSuggestScoreConfig = searchConfigProvider.getSearchConfig("AutoSuggestScoreConfig", new TypeReference<Map<String, String>>() {
        });
        System.out.println(autoSuggestScoreConfig);
        Assert.assertTrue(autoSuggestScoreConfig.size() > 0);
    }
}
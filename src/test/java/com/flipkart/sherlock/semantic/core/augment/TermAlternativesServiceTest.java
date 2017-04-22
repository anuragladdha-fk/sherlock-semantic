package com.flipkart.sherlock.semantic.core.augment;

import com.google.common.collect.Sets;
import junit.framework.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.HashSet;
import java.util.Set;

import static org.mockito.Mockito.*;

/**
 * Created by anurag.laddha on 22/04/17.
 */

@RunWith(MockitoJUnitRunner.class)
public class TermAlternativesServiceTest {

    @Mock
    AugmentationConfigProvider augmentationConfigProviderMock;
    @Mock
    LocalCachedTermAlternativesDataSource localCachedTermAlternativesDataSourceMock;
    @Mock
    CachedNegativesDataSource cachedNegativesDataSourceMock;

    @Test
    public void testNegativeTermNotAugmented(){
        /**
         * Validate that negative terms and queries are not augmented
         */
        when(cachedNegativesDataSourceMock.containsNegative(anyString())).thenReturn(true);  //all terms are considered negative
        TermAlternativesService termAlternativesService = new TermAlternativesService(augmentationConfigProviderMock,
            localCachedTermAlternativesDataSourceMock, cachedNegativesDataSourceMock);

        //Verify no augmentations for query and term since term and query is considered negative
        Assert.assertEquals(0, termAlternativesService.getTermAlternatives("term", "").size());
        Assert.assertEquals(0, termAlternativesService.getQueryAlternatives("query", "").size());

        //Verify alternatives were never fetched and call was returned immediately
        verify(localCachedTermAlternativesDataSourceMock, never()).getTermAlternatives(anyString());
        verify(localCachedTermAlternativesDataSourceMock, never()).getQueryAlternatives(anyString());
    }

    @Test
    public void testTermAndTermAugmentations(){
        /**
         * Verify term and query augmentation can be retrieved from underlying data source
         */
        Set<AugmentAlternative> mockAlternatives = new HashSet<>();
        mockAlternatives.add(new AugmentAlternative("term", "alt1", "default", "type1"));
        mockAlternatives.add(new AugmentAlternative("term", "alt2", "default", "type2"));

        //setup mock behaviors
        when(cachedNegativesDataSourceMock.containsNegative(anyString())).thenReturn(false); //No negative terms
        when(localCachedTermAlternativesDataSourceMock.getTermAlternatives(anyString())).thenReturn(mockAlternatives);
        when(localCachedTermAlternativesDataSourceMock.getQueryAlternatives(anyString())).thenReturn(mockAlternatives);

        TermAlternativesService termAlternativesService = new TermAlternativesService(augmentationConfigProviderMock,
            localCachedTermAlternativesDataSourceMock, cachedNegativesDataSourceMock);

        //Fetch term alternatives and verify if we are receiving expected alternatives
        Set<AugmentAlternative> termAlts = termAlternativesService.getTermAlternatives("term", "");
        System.out.println(termAlts);
        Assert.assertTrue(mockAlternatives.equals(termAlts));

        //Fetch query alternatives and verify if we are receiving expected alternatives
        Set<AugmentAlternative> queryAlts = termAlternativesService.getQueryAlternatives("query", "");
        System.out.println(queryAlts);
        Assert.assertTrue(mockAlternatives.equals(queryAlts));
    }

    @Test
    public void testDroppingAlternativesFromDisabledSources(){
        /**
         * Verify that term and query alternatives are dropped from disabled sources
         */
        Set<AugmentAlternative> mockAlternatives = new HashSet<>();
        mockAlternatives.add(new AugmentAlternative("term", "alt1", "context1", "type1"));
        mockAlternatives.add(new AugmentAlternative("term", "alt2", "context2", "type2"));
        mockAlternatives.add(new AugmentAlternative("term", "alt3", "context3", "type2"));
        mockAlternatives.add(new AugmentAlternative("term", "alt4", "context4", "type2"));

        //setup mock behaviors
        when(cachedNegativesDataSourceMock.containsNegative(anyString())).thenReturn(false); //No negative terms
        when(localCachedTermAlternativesDataSourceMock.getTermAlternatives(anyString())).thenReturn(mockAlternatives);
        when(localCachedTermAlternativesDataSourceMock.getQueryAlternatives(anyString())).thenReturn(mockAlternatives);
        //These contexts are disabled. So its alternatives should be dropped
        when(augmentationConfigProviderMock.getAllDisabledContext(anyString())).thenReturn(Sets.newHashSet("context1", "context2"));

        TermAlternativesService termAlternativesService = new TermAlternativesService(augmentationConfigProviderMock,
            localCachedTermAlternativesDataSourceMock, cachedNegativesDataSourceMock);

        //Alternatives only from context3, context4 are expected
        Set<AugmentAlternative> expectedAlternatives = new HashSet<>();
        expectedAlternatives.add(new AugmentAlternative("term", "alt3", "context3", "type2"));
        expectedAlternatives.add(new AugmentAlternative("term", "alt4", "context4", "type2"));

        /**
         * Validate for term alternatives, options from context1, context2 are dropped since they are from disabled contexts
         */
        Set<AugmentAlternative> termAlts = termAlternativesService.getTermAlternatives("term", "abc");
        System.out.println(termAlts);
        Assert.assertTrue(expectedAlternatives.equals(termAlts));

        /**
         * Validate for query alternatives, options from context1, context2 are dropped since they are from disabled contexts
         */
        Set<AugmentAlternative> queryAlts = termAlternativesService.getQueryAlternatives("query", "abc");
        System.out.println(queryAlts);
        Assert.assertTrue(expectedAlternatives.equals(queryAlts));
    }
}
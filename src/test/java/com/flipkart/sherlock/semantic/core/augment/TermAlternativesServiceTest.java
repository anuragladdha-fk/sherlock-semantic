package com.flipkart.sherlock.semantic.core.augment;

import com.flipkart.sherlock.semantic.core.common.QueryContainer;
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
        Assert.assertEquals(0, termAlternativesService.getTermAlternativesHelper("term", "").size());
        Assert.assertEquals(0, termAlternativesService.getQueryAlternativesHelper("query", "").size());

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
        Set<AugmentAlternative> termAlts = termAlternativesService.getTermAlternativesHelper("term", "");
        System.out.println(termAlts);
        Assert.assertTrue(mockAlternatives.equals(termAlts));

        //Fetch query alternatives and verify if we are receiving expected alternatives
        Set<AugmentAlternative> queryAlts = termAlternativesService.getQueryAlternativesHelper("query", "");
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
        Set<AugmentAlternative> termAlts = termAlternativesService.getTermAlternativesHelper("term", "abc");
        System.out.println(termAlts);
        Assert.assertTrue(expectedAlternatives.equals(termAlts));

        /**
         * Validate for query alternatives, options from context1, context2 are dropped since they are from disabled contexts
         */
        Set<AugmentAlternative> queryAlts = termAlternativesService.getQueryAlternativesHelper("query", "abc");
        System.out.println(queryAlts);
        Assert.assertTrue(expectedAlternatives.equals(queryAlts));
    }


    @Test
    public void testCreateOrTerms(){
        TermAlternativesService termAlternativesService = new TermAlternativesService(augmentationConfigProviderMock,
            localCachedTermAlternativesDataSourceMock, cachedNegativesDataSourceMock);

        String origTerm = "abc";
        Set<String> terms = Sets.newHashSet("pqr", "xyz");

        Assert.assertEquals(origTerm, termAlternativesService.orTerms(origTerm, Sets.newHashSet()));
        Assert.assertEquals("pqr", termAlternativesService.orTerms(origTerm, Sets.newHashSet("pqr")));
        Assert.assertEquals("(pqr OR xyz)", termAlternativesService.orTerms(origTerm, Sets.newHashSet("pqr", "xyz")));
    }

    @Test
    public void testAugmentationTypes(){
        TermAlternativesService termAlternativesService = new TermAlternativesService(augmentationConfigProviderMock,
            localCachedTermAlternativesDataSourceMock, cachedNegativesDataSourceMock);

        Assert.assertEquals("augment", termAlternativesService.getAugmentationTypes(Sets.newHashSet()));
        Assert.assertEquals("abc,augment,pqr", termAlternativesService.getAugmentationTypes(Sets.newHashSet(
            new AugmentAlternative("orig", "aug", "default", "pqr"),
            new AugmentAlternative("orig2", "aug2", "default", "pqr"),
            new AugmentAlternative("orig3", "aug3", "default", "abc"))));
    }

    @Test
    public void testGetQueryAlternatives(){

        when(cachedNegativesDataSourceMock.containsNegative(anyString())).thenReturn(false); //No negative terms

        TermAlternativesService termAlternativesService = new TermAlternativesService(augmentationConfigProviderMock,
            localCachedTermAlternativesDataSourceMock, cachedNegativesDataSourceMock);

        TermAlternativesService spy = spy(termAlternativesService);

        /**
         * Stub response for spy and evaluate if we are getting expected response for normal query --> query replacement
         */
        when(spy.getQueryAlternativesHelper(anyString(), anyString()))
            .thenReturn(Sets.newHashSet(new AugmentAlternative("orig", "replacement", "abc", "query")));

        QueryContainer queryContainer = spy.getQueryAlterntaives("abc", "default");
        Assert.assertTrue(queryContainer.isShowAugmentation());
        Assert.assertTrue(queryContainer.isModified());
        Assert.assertEquals("replacement", queryContainer.getLuceneQuery());
        Assert.assertEquals("replacement", queryContainer.getIdentifiedBestQuery());
        Assert.assertEquals("augment,query", queryContainer.getType());

        /**
         * Evaluate if we are getting expected response for replaceNoShow query --> query replacement
         */
        when(spy.getQueryAlternativesHelper(anyString(), anyString()))
            .thenReturn(Sets.newHashSet(new AugmentAlternative("orig", "replacement", "abc", "replaceNoShow")));

        queryContainer = spy.getQueryAlterntaives("abc", "default");
        Assert.assertFalse(queryContainer.isShowAugmentation()); // this should be false
        Assert.assertTrue(queryContainer.isModified());
        Assert.assertEquals("replacement", queryContainer.getLuceneQuery());
        Assert.assertEquals("replacement", queryContainer.getIdentifiedBestQuery());
        Assert.assertEquals("augment,replaceNoShow", queryContainer.getType());

        /**
         * When there are no alternatives, original query is lucenequery and bestidentified query
         */
        when(spy.getQueryAlternativesHelper(anyString(), anyString())).thenReturn(Sets.newHashSet());
        queryContainer = spy.getQueryAlterntaives("abc", "default");
        System.out.println(queryContainer);
        Assert.assertEquals("abc", queryContainer.getOriginalQuery());
        Assert.assertEquals(queryContainer.getOriginalQuery(), queryContainer.getLuceneQuery());
        Assert.assertEquals(queryContainer.getOriginalQuery(), queryContainer.getIdentifiedBestQuery());
    }

}
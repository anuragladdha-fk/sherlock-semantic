package com.flipkart.sherlock.semantic.core.augment;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.flipkart.sherlock.semantic.common.config.SearchConfigProvider;
import com.flipkart.sherlock.semantic.dao.mysql.AugmentationDao;
import com.flipkart.sherlock.semantic.dao.mysql.RawQueriesDao;
import com.flipkart.sherlock.semantic.core.augment.LocalCachedTermAlternativesDataSource.*;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Sets;
import junit.framework.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;

import static org.mockito.Mockito.*;

/**
 * Created by anurag.laddha on 18/04/17.
 */

@RunWith(PowerMockRunner.class)
public class LocalCachedTermAlternativesDataSourceTest {

    @Mock
    AugmentationDao augmentationDaoMock;
    @Mock
    RawQueriesDao rawQueriesDaoMock;
    @Mock
    ExecutorService executorServiceMock;
    @Mock
    SearchConfigProvider searchConfigProviderMock;
    @Mock
    LoadingCache<String, TermAlternativesWrapper> augmentCacheMock;

    ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void testFetchingAlternatives() throws Exception{

        /**
         * Setup mock response that will come from loading cache
         * Evalute if we are getting term and query alternatives as expected
         */

        TermAlternativesWrapper expectedAlternative = new TermAlternativesWrapper();
        Map<String, Set<AugmentAlternative>> expectedTermAlternatives = new HashMap<>();

        //Setup expected term alternatives for few terms
        Set<AugmentAlternative> term1Alternatives = Sets.newHashSet(new AugmentAlternative("term1", "rep1", "default", "abc"),
            new AugmentAlternative("term1", "rep2", "default", "abc"));
        Set<AugmentAlternative> term2Alternatives =  Sets.newHashSet(new AugmentAlternative("term2", "(another term)", "default", "abc"),
            new AugmentAlternative("term2", "rep4", "default", "abc"));
        expectedTermAlternatives.put("term1", term1Alternatives);
        expectedTermAlternatives.put("term2", term2Alternatives);

        //Setup expected query alternatives for few queries
        Set<AugmentAlternative> query1Alternatives = Sets.newHashSet(new AugmentAlternative("query1", "query rep1", "default", "abc"),
            new AugmentAlternative("query1", "query rep2", "default", "abc"));
        Set<AugmentAlternative> query2Alternatives = Sets.newHashSet(new AugmentAlternative("query2", "query rep3", "default", "abc"),
            new AugmentAlternative("query2", "query rep3", "default", "abc"));
        Map<String, Set<AugmentAlternative>> expectedQueryAlternatives = new HashMap<>();
        expectedQueryAlternatives.put("query1", query1Alternatives);
        expectedQueryAlternatives.put("query2", query2Alternatives);

        //Prepare the complete alternatives object
        expectedAlternative.addTermAlternatives(expectedTermAlternatives);
        expectedAlternative.addQueryAlternatives(expectedQueryAlternatives);

        //setup mock response
        when(augmentCacheMock.get(anyString())).thenReturn(expectedAlternative);

        //Initialise data source with mocked objects
        LocalCachedTermAlternativesDataSource dataSource = new LocalCachedTermAlternativesDataSource(augmentationDaoMock, rawQueriesDaoMock,
            executorServiceMock, objectMapper, 30);

        //swap internal loading cache with mocked cache - now loading cache will return what we have set it up with
        Whitebox.setInternalState(dataSource, "augmentCache", augmentCacheMock);

        //Evalute if we are getting alternatives as expected
        Assert.assertTrue(term1Alternatives.equals(dataSource.getTermAlternatives("term1")));
        Assert.assertTrue(query1Alternatives.equals(dataSource.getQueryAlternatives("query1")));

        //Ensure term and query alternatives are not getting mixed with each other
        Assert.assertNull(dataSource.getTermAlternatives("query1"));
        Assert.assertNull(dataSource.getQueryAlternatives("term1"));
    }


    @Test
    public void testTermWrapperAlternatives_termAlternatives(){
        /**
         * Test TermAlternativesWrapper functionality for term alternatives
         * - add single term alternative
         * - add map of alternatives
         * - ensure they are all available as part of term alternatives
         * - ensure they are not going to query alternatives
         */

        TermAlternativesWrapper alternativesWrapper = new TermAlternativesWrapper();
        Map<String, Set<AugmentAlternative>> termAlternatives = new HashMap<>();

        //Create map of alternatives for few terms
        Set<AugmentAlternative> term1Alternatives = Sets.newHashSet(new AugmentAlternative("term1", "rep1", "default", "abc"),
            new AugmentAlternative("term1", "rep2", "default", "abc"));
        Set<AugmentAlternative> term2Alternatives =  Sets.newHashSet(new AugmentAlternative("term2", "(another term)", "default", "abc"),
            new AugmentAlternative("term2", "rep4", "default", "abc"));
        termAlternatives.put("term1", term1Alternatives);
        termAlternatives.put("term2", term2Alternatives);

        //single term alternatives
        Set<AugmentAlternative> term3Alternatives = Sets.newHashSet(new AugmentAlternative("term3", "rep1", "default", "abc"));

        alternativesWrapper.addTermAlternatives("term3", term3Alternatives);
        alternativesWrapper.addTermAlternatives(termAlternatives);

        //ensure all alternatives are for term, not query
        Assert.assertEquals(0, alternativesWrapper.getQueryToAlternativesMap().size());
        Assert.assertEquals(3, alternativesWrapper.getTermToAlternativesMap().size());

        //Validate alternatives for one of the terms
        Assert.assertTrue(term1Alternatives.equals(alternativesWrapper.getTermAlternatives("term1")));
    }

    @Test
    public void testTermWrapperAlternatives_queryAlternatives(){
        /**
         * Test TermAlternativesWrapper functionality for query alternatives
         * - add single query alternative
         * - add map of query alternatives
         * - ensure they are all available as part of query alternatives
         * - ensure they are not going to term alternatives
         */

        TermAlternativesWrapper alternativesWrapper = new TermAlternativesWrapper();
        Map<String, Set<AugmentAlternative>> termAlternatives = new HashMap<>();

        //Create map of alternatives for few queries
        Set<AugmentAlternative> query1Alternatives = Sets.newHashSet(new AugmentAlternative("term1", "rep1", "default", "abc"),
            new AugmentAlternative("term1", "rep2", "default", "abc"));
        Set<AugmentAlternative> query2Alternatives =  Sets.newHashSet(new AugmentAlternative("term2", "(another term)", "default", "abc"),
            new AugmentAlternative("term2", "rep4", "default", "abc"));
        termAlternatives.put("query1", query1Alternatives);
        termAlternatives.put("query2", query2Alternatives);

        //single query alternatives
        Set<AugmentAlternative> term3Alternatives = Sets.newHashSet(new AugmentAlternative("term3", "rep3", "default", "abc"));

        alternativesWrapper.addQueryAlternatives("query3", term3Alternatives);
        alternativesWrapper.addQueryAlternatives(termAlternatives);

        //ensure all alternatives are for query, not term
        Assert.assertEquals(0, alternativesWrapper.getTermToAlternativesMap().size());
        Assert.assertEquals(3, alternativesWrapper.getQueryToAlternativesMap().size());

        //Validate alternatives for one of the terms
        Assert.assertTrue(query1Alternatives.equals(alternativesWrapper.getQueryAlternatives("query1")));
    }
}
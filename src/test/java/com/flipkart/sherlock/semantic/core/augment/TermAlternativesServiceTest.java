package com.flipkart.sherlock.semantic.core.augment;

import com.flipkart.sherlock.semantic.common.QueryContainer;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import junit.framework.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import static org.mockito.Mockito.*;

/**
 * Created by anurag.laddha on 22/04/17.
 */

@RunWith(MockitoJUnitRunner.class)
public class TermAlternativesServiceTest {

    @Mock
    AugmentationConfigProvider augmentationConfigProviderMock;
    @Mock
    AugmentDataAlgoFactory augmentDataAlgoFactoryMock;
    @Mock
    IDataNegatives negativesDSMock;
    @Mock
    IDataTermAlternatives termAlternativesMock;


    @Test
    public void testNegativeTermNotAugmented(){
        /**
         * Validate that negative terms and queries are not augmented
         */


        when(augmentDataAlgoFactoryMock.getNegativesDataSource(Matchers.<Map<String, String>>any())).thenReturn(negativesDSMock);
        when(negativesDSMock.containsNegative(anyString())).thenReturn(true);  //all terms are considered negative
        TermAlternativesService termAlternativesService = new TermAlternativesService(augmentationConfigProviderMock,
            augmentDataAlgoFactoryMock);

        //Verify no augmentations for query and term since term and query is considered negative
        Assert.assertEquals(0, termAlternativesService.getTermAlternativesHelper("term", "").size());
        Assert.assertEquals(0, termAlternativesService.getQueryAlternativesHelper("query", "").size());

        //Verify alternatives were never fetched and call was returned immediately
        verify(termAlternativesMock, never()).getTermAlternatives(anyString());
        verify(termAlternativesMock, never()).getQueryAlternatives(anyString());
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
        when(augmentDataAlgoFactoryMock.getNegativesDataSource(Matchers.<Map<String, String>>any())).thenReturn(negativesDSMock);
        when(augmentDataAlgoFactoryMock.getTermAlternativesDataSource(Matchers.<Map<String, String>>any())).thenReturn(termAlternativesMock);
        when(negativesDSMock.containsNegative(anyString())).thenReturn(false); //No negative terms
        when(termAlternativesMock.getTermAlternatives(anyString())).thenReturn(mockAlternatives);
        when(termAlternativesMock.getQueryAlternatives(anyString())).thenReturn(mockAlternatives);

        TermAlternativesService termAlternativesService = new TermAlternativesService(augmentationConfigProviderMock,
            augmentDataAlgoFactoryMock);

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
        when(augmentDataAlgoFactoryMock.getNegativesDataSource(Matchers.<Map<String, String>>any())).thenReturn(negativesDSMock);
        when(augmentDataAlgoFactoryMock.getTermAlternativesDataSource(Matchers.<Map<String, String>>any())).thenReturn(termAlternativesMock);
        when(negativesDSMock.containsNegative(anyString())).thenReturn(false); //No negative terms
        when(termAlternativesMock.getTermAlternatives(anyString())).thenReturn(mockAlternatives);
        when(termAlternativesMock.getQueryAlternatives(anyString())).thenReturn(mockAlternatives);
        //These contexts are disabled. So its alternatives should be dropped
        when(augmentationConfigProviderMock.getAllDisabledContext(anyString())).thenReturn(Sets.newHashSet("context1", "context2"));

        TermAlternativesService termAlternativesService = new TermAlternativesService(augmentationConfigProviderMock,
            augmentDataAlgoFactoryMock);

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
            augmentDataAlgoFactoryMock);

        String origTerm = "abc";
        Set<String> terms = Sets.newHashSet("pqr", "xyz");

        Assert.assertEquals(origTerm, termAlternativesService.orTerms(origTerm, Sets.newHashSet()));
        Assert.assertEquals("pqr", termAlternativesService.orTerms(origTerm, Sets.newHashSet("pqr")));
        Assert.assertEquals("(pqr OR xyz)", termAlternativesService.orTerms(origTerm, Sets.newHashSet("pqr", "xyz")));
    }

    @Test
    public void testAugmentationTypes(){
        TermAlternativesService termAlternativesService = new TermAlternativesService(augmentationConfigProviderMock,
            augmentDataAlgoFactoryMock);

        Assert.assertEquals("augment", termAlternativesService.getAugmentationTypes(Sets.newHashSet()));
        Assert.assertEquals("abc,augment,pqr", termAlternativesService.getAugmentationTypes(Sets.newHashSet(
            new AugmentAlternative("orig", "aug", "default", "pqr"),
            new AugmentAlternative("orig2", "aug2", "default", "pqr"),
            new AugmentAlternative("orig3", "aug3", "default", "abc"))));
    }

    @Test
    public void testGetQueryAlternatives(){

        when(negativesDSMock.containsNegative(anyString())).thenReturn(false); //No negative terms

        TermAlternativesService termAlternativesService = new TermAlternativesService(augmentationConfigProviderMock,
            augmentDataAlgoFactoryMock);

        TermAlternativesService spy = spy(termAlternativesService);

        /**
         * Stub response for spy and evaluate if we are getting expected response for normal query --> query replacement
         */
        when(spy.getQueryAlternativesHelper(anyString(), anyString()))
            .thenReturn(Sets.newHashSet(new AugmentAlternative("orig", "replacement", "abc", "query")));

        QueryContainer queryContainer = spy.getQueryAlternatives("abc", "default");
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

        queryContainer = spy.getQueryAlternatives("abc", "default");
        Assert.assertFalse(queryContainer.isShowAugmentation()); // this should be false
        Assert.assertTrue(queryContainer.isModified());
        Assert.assertEquals("replacement", queryContainer.getLuceneQuery());
        Assert.assertEquals("replacement", queryContainer.getIdentifiedBestQuery());
        Assert.assertEquals("augment,replaceNoShow", queryContainer.getType());

        /**
         * When there are no alternatives, original query is lucenequery and bestidentified query
         */
        when(spy.getQueryAlternativesHelper(anyString(), anyString())).thenReturn(Sets.newHashSet());
        queryContainer = spy.getQueryAlternatives("abc", "default");
        System.out.println(queryContainer);
        Assert.assertEquals("abc", queryContainer.getOriginalQuery());
        Assert.assertEquals(queryContainer.getOriginalQuery(), queryContainer.getLuceneQuery());
        Assert.assertEquals(queryContainer.getOriginalQuery(), queryContainer.getIdentifiedBestQuery());
    }

    @Test
    public void testAlphanum(){
        Pattern pattern = TermAlternativesService.ALPHANUM_PATTERN;
        Assert.assertTrue(pattern.matcher("ab12").find());
        Assert.assertTrue(pattern.matcher("12ab").find());
        Assert.assertFalse(pattern.matcher("a12").find());
        Assert.assertFalse(pattern.matcher("ab1").find());
    }

    @Test
    public void testExtractTermGroups(){
        TermAlternativesService termAlternativesService = new TermAlternativesService(augmentationConfigProviderMock,
            augmentDataAlgoFactoryMock);

        Assert.assertEquals(Lists.newArrayList("9", "abc", "10"), termAlternativesService.splitAlphabetsAndNumbers("9abc10"));
        Assert.assertEquals(Lists.newArrayList("9.9", "ab"), termAlternativesService.splitAlphabetsAndNumbers("9.9ab"));
        Assert.assertEquals(Lists.newArrayList("abcpqr"), termAlternativesService.splitAlphabetsAndNumbers("abcpqr"));
        Assert.assertEquals(Lists.newArrayList("9.999"), termAlternativesService.splitAlphabetsAndNumbers("9.999"));
    }

    @Test
    public void testAugmentShingle(){
        /**
         * Stub term alternatives, call method and evaluate response
         */
        Set<AugmentAlternative> augAlts = Sets.newHashSet(new AugmentAlternative("orig", "rep1", "abc", "query"),
            new AugmentAlternative("orig", "rep2", "abc", "query"));  //these will be given as term alternatives for any term

        when(negativesDSMock.containsNegative(anyString())).thenReturn(false); //No negative terms
        TermAlternativesService termAlternativesService = new TermAlternativesService(augmentationConfigProviderMock,
            augmentDataAlgoFactoryMock);
        TermAlternativesService spy = spy(termAlternativesService);   //to allow mocking of few methods and use real methods otherwise

        when(spy.getTermAlternativesHelper(anyString(), anyString())).thenReturn(augAlts);    //any term will be

        String[] terms = {"term1", "term2"};
        QueryContainer queryContainer = new QueryContainer("orig");
        StringBuilder sb = new StringBuilder();
        Set<AugmentAlternative> augmentAlternatives = new HashSet<>();
        spy.getTermRangeAlternatives(terms, queryContainer, sb, augmentAlternatives, 0, 1, "context");   //call real method

        System.out.println(sb);
        System.out.println(queryContainer);

        //Validate querycontainer created based on stubbed term alternatives
        Assert.assertTrue(sb.toString().equals(" (rep2 OR rep1)") || sb.toString().equals(" (rep1 OR rep2)")); //Ordering of rep1, rep2 in set is not deterministic
        Assert.assertTrue(queryContainer.isModified());
        Assert.assertTrue(queryContainer.getAugmentations().equals(ImmutableMap.of("term1", Sets.newHashSet("rep1", "rep2"))));
    }

    @Test
    public void testAugmentShingleSplitAlphaNumeric(){
        /**
         * Terms are alphanumeric and alternatives are available only for alphabet part of it
         * Evaluate response
         */
        Set<AugmentAlternative> augAlts = Sets.newHashSet(new AugmentAlternative("orig", "rep1", "abc", "query"),
            new AugmentAlternative("orig", "rep2", "abc", "query"));

        when(negativesDSMock.containsNegative(anyString())).thenReturn(false); //No negative terms
        TermAlternativesService termAlternativesService = new TermAlternativesService(augmentationConfigProviderMock,
            augmentDataAlgoFactoryMock);
        TermAlternativesService spy = spy(termAlternativesService);   //to allow mocking of few methods and use real methods otherwise

        when(spy.getTermAlternativesHelper(eq("term"), anyString())).thenReturn(augAlts);   //alternative only available for term "term"

        String[] terms = {"term123", "456anotherTerm"};
        QueryContainer queryContainer = new QueryContainer("orig");
        StringBuilder sb = new StringBuilder();
        Set<AugmentAlternative> augmentAlternatives = new HashSet<>();
        spy.getTermRangeAlternatives(terms, queryContainer, sb, augmentAlternatives, 0, 1, "context");   //call real method

        System.out.println(sb);
        System.out.println(queryContainer);

        //Validate querycontainer created based on stubbed term alternatives
        Assert.assertTrue(sb.toString().equals(" (( (rep2 OR rep1) 123) OR term123)")
            || sb.toString().equals(" (( (rep1 OR rep2) 123) OR term123)")); //Ordering of rep1, rep2 in set is not deterministic
        Assert.assertTrue(queryContainer.isModified());
        //term123 was split into term and 123 and alternative was available only for "term"
        Assert.assertTrue(queryContainer.getAugmentations().equals(ImmutableMap.of("term", Sets.newHashSet("rep1", "rep2"))));
    }


    @Test
    public void testSplitTermsAndGetAlternatives(){

        /**
         * There are 3 terms in the query: term1, term2, term3
         * term1 has 2 alternatives: rep1, rep2
         * term2 term3 together have 1 alternative: combined replacement
         * term4 has no alternative
         *
         * Verify after splitting terms you are able
         */

        Set<AugmentAlternative> term1Alts = Sets.newHashSet(new AugmentAlternative("term1", "rep1", "abc", "query"),
            new AugmentAlternative("term1", "rep2", "abc", "query"));

        Set<AugmentAlternative> term2Alts = Sets.newHashSet(new AugmentAlternative("term2", "combined replacement", "abc", "sometype"));

        when(negativesDSMock.containsNegative(anyString())).thenReturn(false); //No negative terms
        TermAlternativesService termAlternativesService = new TermAlternativesService(augmentationConfigProviderMock,
            augmentDataAlgoFactoryMock);
        TermAlternativesService spy = spy(termAlternativesService);   //to allow mocking of few methods and use real methods otherwise

        when(spy.getTermAlternativesHelper(eq("term1"), anyString())).thenReturn(term1Alts);
        when(spy.getTermAlternativesHelper(eq("term2 term3"), anyString())).thenReturn(term2Alts);

        QueryContainer queryContainer = spy.splitTermsAndGetAlternatives("term1 term2 term3 term4", ""); //call real method to find alternatives

        System.out.println(queryContainer);

        //validate alternatives for term1 & (term2 term3) was found as expected
        Assert.assertEquals(Sets.newHashSet("rep1", "rep2"), queryContainer.getAugmentations().get("term1"));
        Assert.assertEquals(Sets.newHashSet("combined replacement"), queryContainer.getAugmentations().get("term2 term3"));
        Assert.assertNull(queryContainer.getAugmentations().get("term4")); //no alternative for term4

        Assert.assertEquals("augment,query,sometype", queryContainer.getType());
        Assert.assertTrue(queryContainer.getLuceneQuery().equals("(rep2 OR rep1) combined replacement term4") ||
            queryContainer.getLuceneQuery().equals("(rep1 OR rep2) combined replacement term4")); //Ordering of rep1, rep2 in set is not deterministic
    }
}
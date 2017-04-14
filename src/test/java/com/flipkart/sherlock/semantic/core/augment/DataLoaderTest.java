package com.flipkart.sherlock.semantic.core.augment;

import com.flipkart.sherlock.semantic.dao.mysql.AugmentationDao;
import com.flipkart.sherlock.semantic.dao.mysql.RawQueriesDao;
import com.flipkart.sherlock.semantic.dao.mysql.entity.AugmentationEntities.*;
import com.flipkart.sherlock.semantic.core.augment.LocalCachedAugmentDataSource.*;
import com.google.common.collect.Sets;
import junit.framework.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;

import static org.mockito.Mockito.*;

import static org.junit.Assert.*;

/**
 * Created by anurag.laddha on 14/04/17.
 */

@RunWith(MockitoJUnitRunner.class)
public class DataLoaderTest {

    @Mock
    AugmentationDao augmentationDaoMock;
    @Mock
    RawQueriesDao rawQueriesDaoMock;
    @Mock
    ExecutorService executorServiceMock;

    @Test
    public void testSynonyms_ReplaceBehavior(){

        /**
         * All synonym options are 'replace' type
         * So these should formulate only query to query replacements, no term to term
         */

        List<Synonym> replaceSynonyms = new ArrayList<>();
        replaceSynonyms.add(new Synonym("SOURCE1", "replacement1, REPLACEMENT2", Synonym.Type.replace));
        replaceSynonyms.add(new Synonym("source2", "rep3", Synonym.Type.replace));

        when(augmentationDaoMock.getSynonyms()).thenReturn(replaceSynonyms);
        DataLoader dataLoader = new DataLoader(augmentationDaoMock, rawQueriesDaoMock, executorServiceMock);
        TermAlternativesWrapper termAlternativesWrapper = dataLoader.getSynonymAugmentations(augmentationDaoMock);

        System.out.println("Query alternatives: " + termAlternativesWrapper.getQueryToAlternativesMap());
        System.out.println("Term alternatives: " + termAlternativesWrapper.getTermToAlternativesMap());

        //Verify no term to term alternatives were formed
        Assert.assertEquals(0, termAlternativesWrapper.getTermToAlternativesMap().size());
        Assert.assertEquals(2, termAlternativesWrapper.getQueryToAlternativesMap().size());

        /**
         * Validate all cache keys for q->q replacement are lower cased
         */
        Assert.assertTrue(Sets.newHashSet("source1", "source2").equals(termAlternativesWrapper.getQueryToAlternativesMap().keySet()));
        Assert.assertFalse(termAlternativesWrapper.getQueryToAlternativesMap().keySet().contains("SOURCE1"));  //SOURCE1 should have been lowercased.

        /**
         * Compare values in cache for one of the keys
         */
        Set<AugmentAlternative> expectedAugmentations = Sets.newHashSet(
            new AugmentAlternative("SOURCE1", "replacement1", AugmentationConstants.CONTEXT_DEFAULT, AugmentAlternative.Type.Synonym),
            new AugmentAlternative("SOURCE1", "REPLACEMENT2", AugmentationConstants.CONTEXT_DEFAULT, AugmentAlternative.Type.Synonym));

        Set<AugmentAlternative> actualAugmentations = termAlternativesWrapper.getQueryAlternatives("source1");

        Assert.assertTrue(expectedAugmentations.equals(actualAugmentations));
    }


    @Test
    public void testSynonyms_NonReplaceBehavior(){

        /**
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

        List<Synonym> replaceSynonyms = new ArrayList<>();
        replaceSynonyms.add(new Synonym("SOURCE1", "replacement1, REPLACEMENT2", Synonym.Type.term));
        replaceSynonyms.add(new Synonym("source2", "rep3", Synonym.Type.query));

        when(augmentationDaoMock.getSynonyms()).thenReturn(replaceSynonyms);
        DataLoader dataLoader = new DataLoader(augmentationDaoMock, rawQueriesDaoMock, executorServiceMock);
        TermAlternativesWrapper termAlternativesWrapper = dataLoader.getSynonymAugmentations(augmentationDaoMock);

        System.out.println("Query alternatives: " + termAlternativesWrapper.getQueryToAlternativesMap());
        System.out.println("Term alternatives: " + termAlternativesWrapper.getTermToAlternativesMap());

        //Verify no query to query alternatives were formed
        Assert.assertEquals(0, termAlternativesWrapper.getQueryToAlternativesMap().size());

        //Verify each synonym term and its alternative are part of the cache
        Assert.assertEquals(5, termAlternativesWrapper.getTermToAlternativesMap().size());

        //Verify synonym term and alternative form each others alternatives
        Assert.assertEquals(3, termAlternativesWrapper.getTermToAlternativesMap().get("source1").size()); //source1 will have source1, replacement1 and REPLACEMENT2 as alteratives
        Assert.assertEquals(3, termAlternativesWrapper.getTermToAlternativesMap().get("replacement1").size()); //replacement1 will have source1, replacement1 and REPLACEMENT2 as alteratives
        Assert.assertEquals(2, termAlternativesWrapper.getTermToAlternativesMap().get("source2").size()); //source2 will have source2, rep3 as alternatives
        Assert.assertEquals(2, termAlternativesWrapper.getTermToAlternativesMap().get("rep3").size()); //rep3 will have source2, rep3 as alternatives


        Set<AugmentAlternative> expectedAugmentations = Sets.newHashSet(
            new AugmentAlternative("SOURCE1", "SOURCE1", AugmentationConstants.CONTEXT_DEFAULT, AugmentAlternative.Type.Synonym),
            new AugmentAlternative("SOURCE1", "replacement1", AugmentationConstants.CONTEXT_DEFAULT, AugmentAlternative.Type.Synonym),
            new AugmentAlternative("SOURCE1", "REPLACEMENT2", AugmentationConstants.CONTEXT_DEFAULT, AugmentAlternative.Type.Synonym)
        );

        Assert.assertTrue(expectedAugmentations.equals(termAlternativesWrapper.getTermAlternatives("source1")));
    }

}
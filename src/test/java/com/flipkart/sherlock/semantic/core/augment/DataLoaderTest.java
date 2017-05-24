package com.flipkart.sherlock.semantic.core.augment;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flipkart.sherlock.semantic.common.config.Constants;
import com.flipkart.sherlock.semantic.dao.mysql.AugmentationDao;
import com.flipkart.sherlock.semantic.dao.mysql.RawQueriesDao;
import com.flipkart.sherlock.semantic.dao.mysql.entity.AugmentationEntities.*;
import com.flipkart.sherlock.semantic.core.augment.LocalCachedTermAlternativesDataSource.*;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import junit.framework.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;

import static org.mockito.Mockito.*;

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

    ObjectMapper objectMapper = new ObjectMapper();


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
        DataLoader dataLoader = new DataLoader(augmentationDaoMock, rawQueriesDaoMock, executorServiceMock, objectMapper);
        TermAlternativesWrapper termAlternativesWrapper = dataLoader.getSynonymAugmentations();

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
            new AugmentAlternative("SOURCE1", "replacement1", Constants.CONTEXT_DEFAULT, AugmentAlternative.Type.Synonym.name()),
            new AugmentAlternative("SOURCE1", "REPLACEMENT2", Constants.CONTEXT_DEFAULT, AugmentAlternative.Type.Synonym.name()));

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
        DataLoader dataLoader = new DataLoader(augmentationDaoMock, rawQueriesDaoMock, executorServiceMock, objectMapper);
        TermAlternativesWrapper termAlternativesWrapper = dataLoader.getSynonymAugmentations();

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
            new AugmentAlternative("SOURCE1", "SOURCE1", Constants.CONTEXT_DEFAULT, AugmentAlternative.Type.Synonym.name()),
            new AugmentAlternative("SOURCE1", "replacement1", Constants.CONTEXT_DEFAULT, AugmentAlternative.Type.Synonym.name()),
            new AugmentAlternative("SOURCE1", "REPLACEMENT2", Constants.CONTEXT_DEFAULT, AugmentAlternative.Type.Synonym.name())
        );

        Assert.assertTrue(expectedAugmentations.equals(termAlternativesWrapper.getTermAlternatives("source1")));
    }


    @Test
    public void testLoadCompoundWords(){
        compoundWordsTestHelper(true); //test for new compound words
        compoundWordsTestHelper(false); //test for old compound words
    }

    private void compoundWordsTestHelper(boolean testNewCompoundWords){
        List<BiCompound> biCompoundList = new ArrayList<>();
        //case1 : since unigram is correct, cache will be keyed on bigram
        biCompoundList.add(new BiCompound("firstunigram", "first bigram", "unigram", 10, 10, 10, 10));
        //case2 : since bigram is correct, cache will be keyed on unigram
        biCompoundList.add(new BiCompound("secondunigram", "second bigram", "bigram", 10, 10, 10, 10));
        //case3 : since both are correct, cache will be keyed on both unigram and bigram
        biCompoundList.add(new BiCompound("thirdunigram", "third bigram", "both", 10, 10, 10, 10));
        EntityMeta dummyEntityMeta = new EntityMeta("b", "latestB");

        if(testNewCompoundWords) {
            when(augmentationDaoMock.getEntityMeta(anyString())).thenReturn(dummyEntityMeta);
            when(rawQueriesDaoMock.getAugmentationCompounds(dummyEntityMeta.getLatestEntityTable())).thenReturn(biCompoundList);
        }
        else{
            when(augmentationDaoMock.getOldCompounds()).thenReturn(biCompoundList);
        }

        DataLoader dataLoader = new DataLoader(augmentationDaoMock, rawQueriesDaoMock, executorServiceMock, objectMapper);
        TermAlternativesWrapper termAlternativesWrapper = dataLoader.loadCompoundWords(testNewCompoundWords);

        System.out.println(termAlternativesWrapper.getQueryToAlternativesMap());
        System.out.println(termAlternativesWrapper.getTermToAlternativesMap());

        //Verify no query to query alternatives were formed
        Assert.assertEquals(0, termAlternativesWrapper.getQueryToAlternativesMap().size());

        //Case 1, verify bigram has two suggestions
        Assert.assertEquals(2, termAlternativesWrapper.getTermAlternatives("first bigram").size());
        //Case 2, verify unigram has two suggestions
        Assert.assertEquals(2, termAlternativesWrapper.getTermAlternatives("secondunigram").size());

        //Case 3, verify both unigram and bigram have two suggestions
        Assert.assertEquals(2, termAlternativesWrapper.getTermAlternatives("thirdunigram").size());
        Assert.assertEquals(2, termAlternativesWrapper.getTermAlternatives("third bigram").size());

        //Verify alternatives created for one of the cases: case 1
        Set<AugmentAlternative> expectedAlternatives = Sets.newHashSet(
            new AugmentAlternative("first bigram", "(first bigram)", Constants.CONTEXT_DEFAULT, AugmentAlternative.Type.CompundWord.name()),
            new AugmentAlternative("first bigram", "firstunigram", Constants.CONTEXT_DEFAULT, AugmentAlternative.Type.CompundWord.name())
        );
        Assert.assertTrue(expectedAlternatives.equals(termAlternativesWrapper.getTermAlternatives("first bigram")));
    }


    @Test
    public void testStringSet() throws Exception{
        Set<String> stringSet = Sets.newHashSet("abc", "pqr");
        System.out.println(objectMapper.writeValueAsString(stringSet));

        Set<String> deserialised = objectMapper.readValue(objectMapper.writeValueAsString(stringSet), new TypeReference<Set<String>>() {
        });
        System.out.println(deserialised);

    }


    @Test
    public void testSpellVariations(){

        EntityMeta dummyEntityMeta = new EntityMeta("b", "latestB");

        List<SpellCorrection> newSpellCorrections = Lists.newArrayList(new SpellCorrection("new1", "[\"newCorrect1\"]"),
            new SpellCorrection("new2", "[\"pqr\",\"abc\"]"));
        List<SpellCorrection> oldSpellCorrections = Lists.newArrayList(new SpellCorrection("old", "[\"oldCorrect\"]"));

        when(augmentationDaoMock.getEntityMeta(anyString())).thenReturn(dummyEntityMeta);
        when(rawQueriesDaoMock.getAugmentationSpellCorrections(dummyEntityMeta.getLatestEntityTable())).thenReturn(newSpellCorrections);
        when(augmentationDaoMock.getSpellCorrectionsLowConf()).thenReturn(oldSpellCorrections);

        //Invoke processing spell variations
        DataLoader dataLoader = new DataLoader(augmentationDaoMock, rawQueriesDaoMock, executorServiceMock, objectMapper);
        TermAlternativesWrapper termAlternativesWrapper = dataLoader.loadSpellVariations();

        System.out.println("Query alternatives: " + termAlternativesWrapper.getQueryToAlternativesMap());
        System.out.println("Term alternatives: " + termAlternativesWrapper.getTermToAlternativesMap());

        //Verify no query to query alternatives were formed
        Assert.assertEquals(0, termAlternativesWrapper.getQueryToAlternativesMap().size());

        //Validate correct spelling and incorrect spelling are added as alternative for incorrect spelling
        Assert.assertEquals(2, termAlternativesWrapper.getTermToAlternativesMap().get("new1").size()); //new1 --> new1, newCorrect1
        Assert.assertEquals(3, termAlternativesWrapper.getTermToAlternativesMap().get("new2").size()); //new2 --> new2, pqr, abc
        Assert.assertEquals(2, termAlternativesWrapper.getTermToAlternativesMap().get("old").size());

        //Validate returned value for one of the cases: new2 --> new2, pqr, abc

        Set<AugmentAlternative> expectedAlternatives = Sets.newHashSet(
            new AugmentAlternative("new2", "new2", Constants.CONTEXT_DEFAULT, AugmentAlternative.Type.SpellVariation.name(), 0),
            new AugmentAlternative("new2", "abc", Constants.CONTEXT_DEFAULT, AugmentAlternative.Type.SpellVariation.name(), 0),
            new AugmentAlternative("new2", "pqr", Constants.CONTEXT_DEFAULT, AugmentAlternative.Type.SpellVariation.name(), 0)
        );
        Assert.assertTrue(expectedAlternatives.equals(termAlternativesWrapper.getTermToAlternativesMap().get("new2")));
    }


    @Test
    public void testLoadAugmentationExperiments(){
        List<AugmentationExperiment> augmentationExperiments = new ArrayList<>();
        augmentationExperiments.add(new AugmentationExperiment("incorrect query", "correct replace", "10.0", "replace", "source1"));   //eg1
        augmentationExperiments.add(new AugmentationExperiment("incorrect term", "correct term", "20.0", "term", "source2"));  //eg2

        when(augmentationDaoMock.getAugmentationExperiements()).thenReturn(augmentationExperiments);

        DataLoader dataLoader = new DataLoader(augmentationDaoMock, rawQueriesDaoMock, executorServiceMock, objectMapper);
        TermAlternativesWrapper termAlternativesWrapper = dataLoader.loadAugmentationExperiments();

        System.out.println("Query alternatives: " + termAlternativesWrapper.getQueryToAlternativesMap());
        System.out.println("Term alternatives: " + termAlternativesWrapper.getTermToAlternativesMap());

        //replace type goes as query alternative  (eg1)
        Assert.assertEquals(1, termAlternativesWrapper.getQueryToAlternativesMap().size());
        //term type goes as term alternative (eg2)
        Assert.assertEquals(1, termAlternativesWrapper.getTermToAlternativesMap().size());

        /**
         * Validate the query and term alternatives formed are as expected
         */
        Set<AugmentAlternative> expectedQueryAlternative = Sets.newHashSet(new AugmentAlternative("incorrect query",
            "correct replace", "source1", "replace", 10.0f));

        Set<AugmentAlternative> expectedTermAlternative = Sets.newHashSet(
            new AugmentAlternative("incorrect term", "(correct term)", "source2", "term", 20.0f),
            new AugmentAlternative("incorrect term", "(incorrect term)", "source2", "term", 20.0f));

        Assert.assertTrue(expectedQueryAlternative.equals(termAlternativesWrapper.getQueryToAlternativesMap().get("incorrect query")));
        Assert.assertTrue(expectedTermAlternative.equals(termAlternativesWrapper.getTermToAlternativesMap().get("incorrect term")));
    }
}
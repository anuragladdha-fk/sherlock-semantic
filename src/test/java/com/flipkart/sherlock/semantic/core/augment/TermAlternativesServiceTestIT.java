package com.flipkart.sherlock.semantic.core.augment;

import com.flipkart.sherlock.semantic.common.util.TestContext;
import junit.framework.Assert;
import org.junit.Test;

import java.util.Set;

/**
 * Created by anurag.laddha on 22/04/17.
 */

/**
 * Integration test for term alternative service.
 * File name must end in 'IT'
 */
public class TermAlternativesServiceTestIT {

    @Test
    public void testTermAlternatives(){
        /**
         * Validate query and term alternatives from all the contexts are chosen
         */
        TermAlternativesService termAlternativesService = TestContext.getInstance(TermAlternativesService.class);

        Set<AugmentAlternative> term1Alt = termAlternativesService.getTermAlternativesHelper("ziaomi", "");
        Set<AugmentAlternative> term2Alt = termAlternativesService.getTermAlternativesHelper("cse", "");
        Set<AugmentAlternative> query1Alt = termAlternativesService.getQueryAlternativesHelper("xiaomi redmi note 4g", "");
        Set<AugmentAlternative> query2Alt = termAlternativesService.getQueryAlternativesHelper("moto g 4 generation", "");

        System.out.println(String.format("Alternative count: %d, alternatives: %s", term1Alt.size(), term1Alt));
        System.out.println(String.format("Alternative count: %d, alternatives: %s", term2Alt.size(), term2Alt));
        System.out.println(String.format("Alternative count: %d, alternatives: %s", query1Alt.size(), query1Alt));
        System.out.println(String.format("Alternative count: %d, alternatives: %s", query2Alt.size(), query2Alt));

        //Validate all available options are selected as alternatives. Alternatives from none of the contexts are dropped
        Assert.assertTrue(term1Alt.size() == 4);
        Assert.assertTrue(term2Alt.size() == 4);
        Assert.assertTrue(query1Alt.size() == 2);
        Assert.assertTrue(query2Alt.size() == 2);
    }

    @Test
    public void testTermAlternativesDropFewContext(){
        /**
         * Validate query and term alternatives from selected contexts are dropped and rest of the alternatives are chosen
         */
        TermAlternativesService termAlternativesService = TestContext.getInstance(TermAlternativesService.class);

        Set<AugmentAlternative> term1Alt = termAlternativesService.getTermAlternativesHelper("ziaomi", "llr"); //dont consider alternatives from llr context
        Set<AugmentAlternative> term2Alt = termAlternativesService.getTermAlternativesHelper("cse", "llr"); //dont consider alternatives from llr context
        Set<AugmentAlternative> query1Alt = termAlternativesService.getQueryAlternativesHelper("xiaomi redmi note 4g", "qspell"); //dont consider alternatives from qspell context
        Set<AugmentAlternative> query2Alt = termAlternativesService.getQueryAlternativesHelper("moto g 4 generation", "qspell"); //dont consider alternatives from qspell context

        System.out.println(String.format("Alternative count: %d, alternatives: %s", term1Alt.size(), term1Alt));
        System.out.println(String.format("Alternative count: %d, alternatives: %s", term2Alt.size(), term2Alt));
        System.out.println(String.format("Alternative count: %d, alternatives: %s", query1Alt.size(), query1Alt));
        System.out.println(String.format("Alternative count: %d, alternatives: %s", query2Alt.size(), query2Alt));

        //Validate options from some of the contexts are dropped and rest of the alternatives are selected
        Assert.assertTrue(term1Alt.size() == 2);
        Assert.assertTrue(term2Alt.size() == 2);
        Assert.assertTrue(query1Alt.size() == 1);
        Assert.assertTrue(query2Alt.size() == 1);
    }
}
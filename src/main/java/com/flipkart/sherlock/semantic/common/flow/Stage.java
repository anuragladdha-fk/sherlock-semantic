package com.flipkart.sherlock.semantic.common.flow;

/**
 * Created by anurag.laddha on 07/05/17.
 */

public class Stage {

    /**
     * Type of stages
     * These stages will be part of one or more {@link WorkflowType}
     */
    public enum Type {
        Normalisation,
        TermAlternatives,
        SpellCorrect
    }

    /**
     * Flavor (type of implementations) for stages
     * One specific flavor of implementation of a stage type will be chosen based on context
     */
    public enum Flavor {
        NormalisationDefault,
        TermAlternativesDefault,
        SpellCorrectDefault
    }
}

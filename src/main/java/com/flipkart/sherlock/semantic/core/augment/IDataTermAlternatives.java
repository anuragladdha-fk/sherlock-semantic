package com.flipkart.sherlock.semantic.core.augment;

import java.util.Set;

/**
 * Created by anurag.laddha on 08/05/17.
 */

/**
 * Interface for data source (hence IData) for term alternatives and replacements
 */
//TODO rename methods to alternatives, replacement. Replacement should have only 1 return value instead of a set. Requires data sanity and validations in place.
public interface IDataTermAlternatives {

    enum Type {
        Default
    }

    /**
     * Provide alternatives for given term
     * @param term
     * @return
     */
    Set<AugmentAlternative> getTermAlternatives(String term);

    /**
     * Provide replacement for given term
     * @param query
     * @return
     */
    Set<AugmentAlternative> getQueryAlternatives(String query);
}

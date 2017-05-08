package com.flipkart.sherlock.semantic.core.augment;


/**
 * Created by anurag.laddha on 08/05/17.
 */

/**
 * Interface for data source (hence IData) for negative terms
 */
public interface IDataNegatives {
    enum Type {
        Default
    }

    /**
     * Specifies if this term is considered negative (eg dont augment)
     */
    boolean containsNegative(String term);
}

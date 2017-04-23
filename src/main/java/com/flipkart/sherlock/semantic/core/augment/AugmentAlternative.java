package com.flipkart.sherlock.semantic.core.augment;

/**
 * Created by anurag.laddha on 12/04/17.
 */

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import java.util.Objects;

/**
 * Augmentation alternative
 */

@AllArgsConstructor
@Getter
@ToString
public class AugmentAlternative {

    /**
     * Type of augmentation
     */
    public static enum Type{
            CompundWord, Synonym, SpellVariation, term, replace, query, replaceNoShow
    }

    private String original;
    private String augmentation;
    private String context;
    private String type;
    private float confidence;

    public AugmentAlternative(String original, String augmentation, String context, String type) {
        this(original, augmentation, context, type, 0f);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AugmentAlternative that = (AugmentAlternative) o;
        return Objects.equals(original, that.original) &&
            Objects.equals(augmentation, that.augmentation) &&
            Objects.equals(context, that.context) &&
            Objects.equals(type, that.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(original, augmentation, context, type);
    }
}
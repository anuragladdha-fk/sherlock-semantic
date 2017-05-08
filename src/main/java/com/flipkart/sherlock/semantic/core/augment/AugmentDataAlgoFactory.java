package com.flipkart.sherlock.semantic.core.augment;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.AllArgsConstructor;

import java.util.Map;

/**
 * Created by anurag.laddha on 08/05/17.
 */

/**
 * Factory to get concrete implementation of data sources and algorithms required for augmentation
 */

@Singleton
public class AugmentDataAlgoFactory {
    private final Map<IDataNegatives.Type, IDataNegatives> negativesDataSourceTypeToImplMap;
    private final Map<IDataTermAlternatives.Type, IDataTermAlternatives> termAlternativesDataSourceTypeToImplMap;

    @Inject
    public AugmentDataAlgoFactory(Map<IDataNegatives.Type, IDataNegatives> negativesDataSourceTypeToImplMap,
                                  Map<IDataTermAlternatives.Type, IDataTermAlternatives> termAlternativesDataSourceTypeToImplMap) {
        this.negativesDataSourceTypeToImplMap = negativesDataSourceTypeToImplMap;
        this.termAlternativesDataSourceTypeToImplMap = termAlternativesDataSourceTypeToImplMap;
    }

    IDataNegatives getNegativesDataSource(Map<String, String> context){
        return negativesDataSourceTypeToImplMap.get(IDataNegatives.Type.Default);
    }

    IDataTermAlternatives getTermAlternativesDataSource(Map<String, String> context){
        return termAlternativesDataSourceTypeToImplMap.get(IDataTermAlternatives.Type.Default);
    }
}

package com.flipkart.sherlock.semantic.core.augment;

import com.flipkart.sherlock.semantic.common.flow.IStage;
import com.flipkart.sherlock.semantic.common.flow.Stage;
import com.flipkart.sherlock.semantic.common.flow.StageRequest;
import com.flipkart.sherlock.semantic.common.flow.StageResponse;

/**
 * Created by anurag.laddha on 07/05/17.
 */
public class DefaultTermAlternativeStage implements IStage {

    private Stage.Type stageType;
    private TermAlternativesService termAlternativesService;

    public DefaultTermAlternativeStage(Stage.Type stageType, TermAlternativesService termAlternativesService) {
        this.stageType = stageType;
        this.termAlternativesService = termAlternativesService;
    }

    @Override
    public StageResponse process(StageRequest stageRequest) {
        return null;
    }

    @Override
    public Stage.Type getStageType() {
        return this.stageType;
    }
}

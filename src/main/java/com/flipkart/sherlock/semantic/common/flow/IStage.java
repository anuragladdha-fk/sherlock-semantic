package com.flipkart.sherlock.semantic.common.flow;

/**
 * Created by anurag.laddha on 07/05/17.
 */

/**
 * Semantic stage.
 */
public interface IStage {

    /**
     * Processing to be performed by this stage.
     * @param stageRequest: State of request till now
     * @return
     */
    StageResponse process(StageRequest stageRequest);

    /**
     * Type of stage
     * @return
     */
    Stage.Type getStageType();
}

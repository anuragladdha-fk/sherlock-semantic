package com.flipkart.sherlock.semantic.common.flow;

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import java.util.List;
import java.util.Map;

/**
 * Created by anurag.laddha on 07/05/17.
 */

@Singleton
public class StageFactory {

    private final Map<Stage.Flavor, IStage> stageFlavorToStageImplMap;

    @Inject
    public StageFactory(Map<Stage.Flavor, IStage> stageFlavorToStageImplMap) {
        this.stageFlavorToStageImplMap = stageFlavorToStageImplMap;
    }

    /**
     * Depending upon workflow type and context information, select list of stages to execute
     * Context (eg experiment, etc) can provide more information on order of stages to execute
     * @param workflowType
     * @param context
     * @return
     */
    List<Stage.Type> getStageTypesForWorkflow(WorkflowType workflowType, Map<String, String> context){
        List<Stage.Type> stageTypes = null;
        switch(workflowType){
            case Augment:
                stageTypes = Lists.newArrayList(Stage.Type.TermAlternatives);
                break;
            default:
                throw new RuntimeException("Unsupported operation");
        }
        return stageTypes;
    }

    /**
     * Get a specific stage implementation based on stage type and other context information
     * Context (eg experiment, etc) can provide more information on what specific implementation of a stage type to choose
     * @param stageType
     * @param context
     * @return
     */
    IStage getStage(Stage.Type stageType, Map<String, String> context){
        IStage stageImpl = null;
        switch(stageType) {
            case TermAlternatives:
                stageImpl = this.stageFlavorToStageImplMap.get(Stage.Flavor.TermAlternativesDefault);
                break;
            default:
                throw new RuntimeException("Unsupported operation");
        }
        return stageImpl;
    }
}
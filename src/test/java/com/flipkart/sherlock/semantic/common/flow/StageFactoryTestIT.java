package com.flipkart.sherlock.semantic.common.flow;

import com.flipkart.sherlock.semantic.common.util.TestContext;
import junit.framework.Assert;
import org.junit.Test;

/**
 * Created by anurag.laddha on 07/05/17.
 */
public class StageFactoryTestIT {

    @Test
    public void testStageFactorInit(){
        StageFactory stageFactory = TestContext.getInstance(StageFactory.class);
        Assert.assertTrue(stageFactory.getStageTypesForWorkflow(WorkflowType.Augment, null).size() > 0);
        Assert.assertTrue(stageFactory.getStage(Stage.Type.TermAlternatives, null) != null);
    }
}
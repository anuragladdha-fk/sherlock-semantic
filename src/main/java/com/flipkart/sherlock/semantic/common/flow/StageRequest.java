package com.flipkart.sherlock.semantic.common.flow;

/**
 * Created by anurag.laddha on 07/05/17.
 */

/**
 * Holds state of semantic request till now (output of the recent most {@link com.flipkart.sherlock.semantic.common.flow.Stage.Type} that was processed
 * Could be for any of the {@link WorkflowType}.
 *
 * Clients should be able to add data to this DS, but not delete existing data.
 */
public class StageRequest {
}

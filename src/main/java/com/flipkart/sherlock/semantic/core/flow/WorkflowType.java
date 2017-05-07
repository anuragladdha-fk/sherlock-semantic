package com.flipkart.sherlock.semantic.core.flow;

/**
 * Created by anurag.laddha on 07/05/17.
 */

/**
 * Type of workflow.
 * Each workflow will have one or more {@link com.flipkart.sherlock.semantic.core.flow.Stage.Type} to be executed.
 */
public enum WorkflowType {
    Augment,
    OnlineSpeller,
    Intent
}

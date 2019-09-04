package uk.gov.hmcts.cmc.claimstore.services.stateflow.grammar;

/**
 * This specifies what can come after a CREATE_SUBFLOW clause
 */
public interface CreateSubflowNext<S>
        extends TransitionTo<S>
{
}

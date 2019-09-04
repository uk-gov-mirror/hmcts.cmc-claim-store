package uk.gov.hmcts.cmc.claimstore.services.stateflow.grammar;

/**
 * This specifies what can come after a INITIAL clause
 */
public interface InitialNext<S>
        extends TransitionTo<S>, Subflow<S>
{
}


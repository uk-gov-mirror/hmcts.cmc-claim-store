package uk.gov.hmcts.cmc.claimstore.services.stateflow.grammar;

/**
 * This specifies what can come after a TRANSITION_TO clause
 */
public interface TransitionToNext<S>
        extends TransitionTo<S>, OnlyIf<S>, State<S>, Subflow<S>, Build<S>
{
}


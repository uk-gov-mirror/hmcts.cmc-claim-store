package uk.gov.hmcts.cmc.claimstore.services.stateflow.grammar;

/**
 * Represents the TRANSITION_TO clause
 */
public interface TransitionTo<S>
{
    TransitionToNext<S> transitionTo(S state);
}

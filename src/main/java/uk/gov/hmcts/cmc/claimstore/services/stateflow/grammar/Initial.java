package uk.gov.hmcts.cmc.claimstore.services.stateflow.grammar;

/**
 * Represents the INITIAL clause
 */
public interface Initial<S>
{
    InitialNext<S> initial(S state);
}

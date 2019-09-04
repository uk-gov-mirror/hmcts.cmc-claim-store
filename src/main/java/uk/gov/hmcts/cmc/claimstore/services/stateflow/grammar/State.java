package uk.gov.hmcts.cmc.claimstore.services.stateflow.grammar;

/**
 * Represents the STATE clause
 */
public interface State<S>
{
    StateNext<S> state(S state);
}

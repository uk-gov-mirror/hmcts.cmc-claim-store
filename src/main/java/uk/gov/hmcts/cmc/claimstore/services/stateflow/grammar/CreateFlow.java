package uk.gov.hmcts.cmc.claimstore.services.stateflow.grammar;

/**
 * Represents the CREATE_FLOW clause
 */
public interface CreateFlow<S>
{
    CreateFlowNext<S> createFlow();
}

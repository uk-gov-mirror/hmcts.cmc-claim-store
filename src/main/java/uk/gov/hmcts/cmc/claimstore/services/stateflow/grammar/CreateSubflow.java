package uk.gov.hmcts.cmc.claimstore.services.stateflow.grammar;

/**
 * Represents the CREATE_SUBFLOW clause
 */
public interface CreateSubflow<S>
{
    CreateSubflowNext<S> createSubflow();
}

package uk.gov.hmcts.cmc.claimstore.services.stateflow.grammar;

import uk.gov.hmcts.cmc.claimstore.services.stateflow.StateFlow;

/**
 * Represents the BUILD clause
 */
public interface Build<S>
{
    StateFlow build();
}

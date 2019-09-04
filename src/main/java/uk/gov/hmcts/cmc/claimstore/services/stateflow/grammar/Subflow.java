package uk.gov.hmcts.cmc.claimstore.services.stateflow.grammar;

import uk.gov.hmcts.cmc.claimstore.services.stateflow.StateFlowContext;

import java.util.function.Consumer;

/**
 * Represents the SUBFLOW clause
 */
public interface Subflow<S>
{
    SubflowNext<S> subflow(Consumer<StateFlowContext> consumer);
}

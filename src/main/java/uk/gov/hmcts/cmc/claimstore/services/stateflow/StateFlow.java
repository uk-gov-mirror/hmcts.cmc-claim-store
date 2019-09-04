package uk.gov.hmcts.cmc.claimstore.services.stateflow;

import org.springframework.statemachine.StateMachine;
import uk.gov.hmcts.cmc.claimstore.services.stateflow.model.State;
import uk.gov.hmcts.cmc.domain.models.Claim;

import java.util.Map;

import static uk.gov.hmcts.cmc.claimstore.services.stateflow.StateFlowContext.EXTENDED_STATE_VARIABLE_KEY;

public class StateFlow {

    public StateFlow(StateMachine<String, String> stateMachine) {
        this.stateMachine = stateMachine;
    }

    private StateMachine<String, String> stateMachine;

    public StateMachine<String, String> asStateMachine() {
        return stateMachine;
    }

    public StateFlow evaluate(Claim claim) {
        Map<Object, Object> variables = stateMachine.getExtendedState().getVariables();
        variables.put(EXTENDED_STATE_VARIABLE_KEY, claim);
        stateMachine.startReactively().block();
        return this;
    }

    public State getState() {
        return stateMachine.hasStateMachineError() ? State.error() : State.from(stateMachine.getState().getId());
    }
}

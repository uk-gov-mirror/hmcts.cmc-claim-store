package uk.gov.hmcts.cmc.claimstore.services.stateflow;

import org.springframework.statemachine.StateMachine;
import uk.gov.hmcts.cmc.claimstore.services.stateflow.model.State;
import uk.gov.hmcts.cmc.domain.models.Claim;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static uk.gov.hmcts.cmc.claimstore.services.stateflow.StateFlowContext.EXTENDED_STATE_CLAIM_KEY;
import static uk.gov.hmcts.cmc.claimstore.services.stateflow.StateFlowContext.EXTENDED_STATE_HISTORY_KEY;

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
        variables.put(EXTENDED_STATE_CLAIM_KEY, claim);
        stateMachine.startReactively().block();
        return this;
    }

    public State getState() {
        return stateMachine.hasStateMachineError() ? State.error() : State.from(stateMachine.getState().getId());
    }

    public List<State> getStateHistory() {
        List<String> historyList = stateMachine.getExtendedState().get(EXTENDED_STATE_HISTORY_KEY, ArrayList.class);
        return historyList.stream().map(State::from).collect(Collectors.toList());
    }
}

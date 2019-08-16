package uk.gov.hmcts.cmc.claimstore.services.stateflow;

import uk.gov.hmcts.cmc.claimstore.services.stateflow.model.Transition;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class StateFlowContext {

    private final List<String> states = new ArrayList<>();

    private final List<Transition> transitions = new ArrayList<>();

    Optional<String> getInitialState() {
        return states.size() < 1 ? Optional.empty() : Optional.of(states.get(0));
    }

    Optional<String> getCurrentState() {
        return states.size() < 1 ? Optional.empty() : Optional.of(states.get(states.size() - 1));
    }

    Optional<Transition> getCurrentTransition() {
        return transitions.size() < 1 ? Optional.empty() : Optional.of(transitions.get(transitions.size() - 1));
    }

    public List<String> addState(String state) {
        this.states.add(state);
        return this.states;
    }

    public List<Transition> addTransition(Transition transition) {
        this.transitions.add(transition);
        return this.transitions;
    }
}

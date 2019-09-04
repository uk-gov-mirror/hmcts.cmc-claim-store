package uk.gov.hmcts.cmc.claimstore.services.stateflow;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.listener.StateMachineListenerAdapter;
import org.springframework.statemachine.state.State;
import org.springframework.statemachine.transition.Transition;
import uk.gov.hmcts.cmc.claimstore.services.stateflow.utils.StateMachineUtils;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class StateFlowListener extends StateMachineListenerAdapter<String, String> {

    private static final Logger logger = LoggerFactory.getLogger(StateFlowListener.class);
    private StateContext<String, String> stateContext;

    @Override
    public void stateContext(StateContext<String, String> stateContext) {
        this.stateContext = stateContext;
        super.stateContext(stateContext);
    }

    @Override
    public void stateEntered(State<String, String> state) {
        Collection<Transition<String, String>> permittedTransitions =
            StateMachineUtils.findPermittedTransitionsForState(stateContext, state);
        if (permittedTransitions.size() > 1) {
            String sourceState = state.getId();
            String permittedStates = String.join( ",", toPermittedStates(permittedTransitions));
            String message = String.format(
                "Ambiguous transitions permitting state [%s] to move to more than one next states [%s].",
                sourceState, permittedStates);
            logger.error(message);
            stateContext.getStateMachine().setStateMachineError(new IllegalStateException(message));
        }
    }

    private List<String> toPermittedStates(Collection<Transition<String, String>> permittedTransitions) {
        return permittedTransitions.stream()
            .map(transition -> transition.getTarget().getId())
            .collect(Collectors.toList());
    }
}

package uk.gov.hmcts.cmc.claimstore.services.stateflow;

import org.junit.Test;
import uk.gov.hmcts.cmc.claimstore.services.stateflow.model.Transition;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class StateFlowContextTest {

    @Test
    public void shouldReturnEmptyInitialStateWhenNoStates() {
        StateFlowContext stateFlowContext = new StateFlowContext();
        assertThat(stateFlowContext.getInitialState()).isEmpty();
    }

    @Test
    public void shouldReturnEmptyCurrentStateStateWhenNoStates() {
        StateFlowContext stateFlowContext = new StateFlowContext();
        assertThat(stateFlowContext.getCurrentState()).isEmpty();
    }

    @Test
    public void shouldReturnEmptyCurrentTransitionWhenNoTransitions() {
        StateFlowContext stateFlowContext = new StateFlowContext();
        assertThat(stateFlowContext.getCurrentState()).isEmpty();
    }

    @Test
    public void shouldReturnCorrectInitialAndCurrentState() {
        StateFlowContext stateFlowContext = new StateFlowContext();
        stateFlowContext.addState("state-1");
        stateFlowContext.addState("state-2");
        assertThat(stateFlowContext.getInitialState()).isNotEmpty();
        assertThat(stateFlowContext.getInitialState().get()).isEqualTo("state-1");
        assertThat(stateFlowContext.getCurrentState()).isNotEmpty();
        assertThat(stateFlowContext.getCurrentState().get()).isEqualTo("state-2");
    }

    @Test
    public void shouldReturnCorrectCurrentTransition() {
        StateFlowContext stateFlowContext = new StateFlowContext();
        Transition transition1 = new Transition("state-1", "state-2", claim -> true);
        Transition transition2 = new Transition("state-2", "state-3", claim -> false);
        stateFlowContext.addTransition(transition1);
        stateFlowContext.addTransition(transition2);

        Optional<Transition> currentTransition = stateFlowContext.getCurrentTransition();
        assertThat(currentTransition).isNotEmpty();
        assertThat(currentTransition.get())
            .extracting("sourceState", "targetState", "condition")
            .doesNotContainNull()
            .containsExactly(transition2.getSourceState(), transition2.getTargetState(), transition2.getCondition());
    }

    @Test
    public void shouldReturnAllStatesThatHaveBeenAdded() {
        StateFlowContext stateFlowContext = new StateFlowContext();
        stateFlowContext.addState("state-1");
        stateFlowContext.addState("state-2");
        assertThat(stateFlowContext.getStates()).isNotEmpty().hasSize(2);
        assertThat(stateFlowContext.getStates()).contains("state-1", "state-2");
    }

    @Test
    public void shouldReturnAllTransitionsThatHaveBeenAdded() {
        StateFlowContext stateFlowContext = new StateFlowContext();
        Transition transition1 = new Transition("state-1", "state-2", claim -> true);
        Transition transition2 = new Transition("state-2", "state-3", claim -> false);
        stateFlowContext.addTransition(transition1);
        stateFlowContext.addTransition(transition2);

        assertThat(stateFlowContext.getTransitions()).isNotEmpty().hasSize(2);
        assertThat(stateFlowContext.getTransitions()).contains(transition1, transition2);
    }
}

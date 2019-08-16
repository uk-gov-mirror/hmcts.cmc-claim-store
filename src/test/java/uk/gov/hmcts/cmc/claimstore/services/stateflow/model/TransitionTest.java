package uk.gov.hmcts.cmc.claimstore.services.stateflow.model;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class TransitionTest {

    @Test
    public void shouldConstructAnInstanceWithNoCondition() {
        Transition transition = new Transition("state-1", "state-2");
        assertThat(transition)
            .extracting("sourceState", "targetState")
            .doesNotContainNull()
            .containsExactly(transition.getSourceState(), transition.getTargetState());
        assertThat(transition)
            .extracting("condition")
            .containsNull();
    }

    @Test
    public void shouldConstructAnInstanceWithCondition() {
        Transition transition = new Transition("state-1", "state-2", claim -> true);
        assertThat(transition)
            .extracting("sourceState", "targetState", "condition")
            .doesNotContainNull()
            .containsExactly(transition.getSourceState(), transition.getTargetState(), transition.getCondition());
    }
}

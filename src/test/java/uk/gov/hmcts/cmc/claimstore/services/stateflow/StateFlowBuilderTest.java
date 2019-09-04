package uk.gov.hmcts.cmc.claimstore.services.stateflow;

import org.junit.Test;
import uk.gov.hmcts.cmc.domain.models.Claim;

import java.util.function.Consumer;
import java.util.function.Predicate;

import static org.assertj.core.api.Assertions.assertThat;

enum FlowState {
    STATE_1,
    STATE_2,
    STATE_3
}

enum SubflowState {
    STATE_1,
    STATE_2
}

public class StateFlowBuilderTest {

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowIllegalArgumentExceptionIfFlowNameIsNull() {
        StateFlowBuilder.<FlowState>flow(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowIllegalArgumentExceptionIfFlowNameIsEmpty() {
        StateFlowBuilder.<FlowState>flow("");
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowIllegalArgumentExceptionIfSubflowNameIsNull() {
        StateFlowBuilder.<SubflowState>subflow(null, new StateFlowContext());
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowIllegalArgumentExceptionIfSubflowNameIsEmpty() {
        StateFlowBuilder.<SubflowState>subflow("", new StateFlowContext());
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowIllegalArgumentExceptionIfStateFlowContextIsNull() {
        StateFlowBuilder.<SubflowState>subflow("SUBFLOW", null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowIllegalArgumentExceptionIfInitialIsNull() {
        StateFlowBuilder.<FlowState>flow("FLOW")
            .initial(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowIllegalArgumentExceptionIfTransitionToIsNull() {
        StateFlowBuilder.<FlowState>flow("FLOW")
            .initial(FlowState.STATE_1)
            .transitionTo(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowIllegalArgumentExceptionIfStateIsNull() {
        StateFlowBuilder.<FlowState>flow("FLOW")
            .initial(FlowState.STATE_1)
                .transitionTo(FlowState.STATE_1)
            .state(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowIllegalArgumentExceptionIfOnlyIfIsNull() {
        StateFlowBuilder.<FlowState>flow("FLOW")
            .initial(FlowState.STATE_1)
                .transitionTo(FlowState.STATE_1).onlyIf(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowIllegalArgumentExceptionIfSubflowIsNull() {
        StateFlowBuilder.<FlowState>flow("FLOW")
            .initial(FlowState.STATE_1)
            .subflow(null);
    }

    @Test
    public void shouldBuildStateFlowWithImplicitTransition() {
        StateFlow stateFlow = StateFlowBuilder.<FlowState>flow("FLOW")
            .initial(FlowState.STATE_1)
                .transitionTo(FlowState.STATE_2)
            .state(FlowState.STATE_2)
            .build();

        StateFlowAssert.assertThat(stateFlow).enteredStates("FLOW.STATE_1", "FLOW.STATE_2");
        assertThat(stateFlow.asStateMachine().hasStateMachineError()).isFalse();
    }

    @Test
    public void shouldBuildStateFlowWithTrueConditionOnTransition() {
        StateFlow stateFlow = StateFlowBuilder.<FlowState>flow("FLOW")
            .initial(FlowState.STATE_1)
                .transitionTo(FlowState.STATE_2).onlyIf(claim -> true)
            .state(FlowState.STATE_2)
            .build();

        StateFlowAssert.assertThat(stateFlow).enteredStates("FLOW.STATE_1", "FLOW.STATE_2");
        assertThat(stateFlow.asStateMachine().hasStateMachineError()).isFalse();
    }

    @Test
    public void shouldBuildStateFlowWithFalseConditionOnTransition() {
        StateFlow stateFlow = StateFlowBuilder.<FlowState>flow("FLOW")
            .initial(FlowState.STATE_1)
                .transitionTo(FlowState.STATE_2).onlyIf(claim -> false)
            .state(FlowState.STATE_2)
            .build();

        StateFlowAssert.assertThat(stateFlow).enteredStates("FLOW.STATE_1");
        assertThat(stateFlow.asStateMachine().hasStateMachineError()).isFalse();
    }

    @Test
    public void shouldBuildStateFlowWithMutuallyExclusiveConditionalTransitions() {
        StateFlow stateFlow = StateFlowBuilder.<FlowState>flow("FLOW")
            .initial(FlowState.STATE_1)
                .transitionTo(FlowState.STATE_2).onlyIf(claim -> false)
                .transitionTo(FlowState.STATE_3).onlyIf(claim -> true)
            .state(FlowState.STATE_2)
            .state(FlowState.STATE_3)
            .build();

        StateFlowAssert.assertThat(stateFlow).enteredStates("FLOW.STATE_1", "FLOW.STATE_3");
        assertThat(stateFlow.asStateMachine().hasStateMachineError()).isFalse();
    }

    @Test
    public void shouldBuildStateFlowWithMutuallyExclusiveImplicitAndConditionalTransitions() {
        StateFlow stateFlow = StateFlowBuilder.<FlowState>flow("FLOW")
            .initial(FlowState.STATE_1)
            .transitionTo(FlowState.STATE_2).onlyIf(claim -> false)
            .transitionTo(FlowState.STATE_3)
            .state(FlowState.STATE_2)
            .state(FlowState.STATE_3)
            .build();

        StateFlowAssert.assertThat(stateFlow).enteredStates("FLOW.STATE_1", "FLOW.STATE_3");
        assertThat(stateFlow.asStateMachine().hasStateMachineError()).isFalse();
    }

    @Test
    public void shouldBuildStateFlowWithMultipleStateTransitions() {
        StateFlow stateFlow = StateFlowBuilder.<FlowState>flow("FLOW")
            .initial(FlowState.STATE_1)
                .transitionTo(FlowState.STATE_2).onlyIf(claim -> true)
            .state(FlowState.STATE_2)
                .transitionTo(FlowState.STATE_3)
            .state(FlowState.STATE_3)
            .build();

        StateFlowAssert.assertThat(stateFlow).enteredStates("FLOW.STATE_1", "FLOW.STATE_2", "FLOW.STATE_3");
        assertThat(stateFlow.asStateMachine().hasStateMachineError()).isFalse();
    }

    @Test
    public void shouldBuildStateFlowWithTransitionToUndefinedState() {
        StateFlow stateFlow = StateFlowBuilder.<FlowState>flow("FLOW")
            .initial(FlowState.STATE_1)
                .transitionTo(FlowState.STATE_2).onlyIf(claim -> true)
            .state(FlowState.STATE_2)
                .transitionTo(FlowState.STATE_3)
            .build();

        StateFlowAssert.assertThat(stateFlow).enteredStates("FLOW.STATE_1", "FLOW.STATE_2");
        assertThat(stateFlow.asStateMachine().hasStateMachineError()).isFalse();
    }

    @Test
    public void shouldBuildStateFlowWithSubflowUnderInitialState() {
        Consumer<StateFlowContext> subflow = stateFlowContext ->
            StateFlowBuilder.<SubflowState>subflow("SUBFLOW", stateFlowContext)
                    .transitionTo(SubflowState.STATE_1)
                .state(SubflowState.STATE_1)
                    .transitionTo(SubflowState.STATE_2)
                .state(SubflowState.STATE_2);

        StateFlow stateFlow = StateFlowBuilder.<FlowState>flow("FLOW")
            .initial(FlowState.STATE_1)
                .subflow(subflow)
            .state(FlowState.STATE_2)
            .build();

        StateFlowAssert.assertThat(stateFlow).enteredStates("FLOW.STATE_1", "SUBFLOW.STATE_1", "SUBFLOW.STATE_2");
        assertThat(stateFlow.asStateMachine().hasStateMachineError()).isFalse();
    }

    @Test
    public void shouldBuildStateFlowWithSubflowUnderNonInitialState() {
        Consumer<StateFlowContext> subflow = stateFlowContext ->
            StateFlowBuilder.<SubflowState>subflow("SUBFLOW", stateFlowContext)
                    .transitionTo(SubflowState.STATE_1)
                .state(SubflowState.STATE_1)
                    .transitionTo(SubflowState.STATE_2)
                .state(SubflowState.STATE_2);

        StateFlow stateFlow = StateFlowBuilder.<FlowState>flow("FLOW")
            .initial(FlowState.STATE_1)
                .transitionTo(FlowState.STATE_2)
            .state(FlowState.STATE_2)
                .subflow(subflow)
            .build();

        StateFlowAssert.assertThat(stateFlow).enteredStates("FLOW.STATE_1", "FLOW.STATE_2", "SUBFLOW.STATE_1",  "SUBFLOW.STATE_2");
        assertThat(stateFlow.asStateMachine().hasStateMachineError()).isFalse();
    }

    @Test
    public void shouldBuildStateFlowButSetStateMachineErrorIfConditionsOnTransitionsAreNotMutuallyExclusive() {
        StateFlow stateFlow = StateFlowBuilder.<FlowState>flow("FLOW")
            .initial(FlowState.STATE_1)
                .transitionTo(FlowState.STATE_2).onlyIf(claim -> true)
                .transitionTo(FlowState.STATE_3).onlyIf(claim -> true)
            .state(FlowState.STATE_2)
            .state(FlowState.STATE_3)
            .build();

        StateFlowAssert.assertThat(stateFlow).enteredStates("FLOW.STATE_1", "FLOW.STATE_3");
        assertThat(stateFlow.asStateMachine().hasStateMachineError()).isTrue();
    }

    @Test
    public void shouldBuildStateFlowButSetStateMachineErrorIfMoreThanOneImplicitTransitions() {
        StateFlow stateFlow = StateFlowBuilder.<FlowState>flow("FLOW")
            .initial(FlowState.STATE_1)
                .transitionTo(FlowState.STATE_2)
                .transitionTo(FlowState.STATE_3)
            .state(FlowState.STATE_2)
            .state(FlowState.STATE_3)
            .build();

        StateFlowAssert.assertThat(stateFlow).enteredStates("FLOW.STATE_1", "FLOW.STATE_3");
        assertThat(stateFlow.asStateMachine().hasStateMachineError()).isTrue();
    }

    @Test
    public void shouldBuildStateFlowButSetStateMachineErrorIfImplicitTransitionAndConditionalTransitionAreNotMutuallyExclusive() {
        StateFlow stateFlow = StateFlowBuilder.<FlowState>flow("FLOW")
            .initial(FlowState.STATE_1)
                .transitionTo(FlowState.STATE_2).onlyIf(claim -> true)
                .transitionTo(FlowState.STATE_3)
            .state(FlowState.STATE_2)
            .state(FlowState.STATE_3)
            .build();

        StateFlowAssert.assertThat(stateFlow).enteredStates("FLOW.STATE_1", "FLOW.STATE_3");
        assertThat(stateFlow.asStateMachine().hasStateMachineError()).isTrue();
    }

    @Test
    public void shouldBuildStateFlowWithSubflowButSetStateMachineErrorIfAmbiguousTransitions() {
        Consumer<StateFlowContext> subflow = stateFlowContext ->
            StateFlowBuilder.<SubflowState>subflow("SUBFLOW", stateFlowContext)
                    .transitionTo(SubflowState.STATE_1)
                .state(SubflowState.STATE_1);

        StateFlow stateFlow = StateFlowBuilder.<FlowState>flow("FLOW")
            .initial(FlowState.STATE_1)
                .transitionTo(FlowState.STATE_2)
                .subflow(subflow)
            .state(FlowState.STATE_2)
            .build();

        StateFlowAssert.assertThat(stateFlow).enteredStates("FLOW.STATE_1", "SUBFLOW.STATE_1");
        assertThat(stateFlow.asStateMachine().hasStateMachineError()).isTrue();
    }

    @Test
    public void shouldEvaluateTheClaimState() {
        Claim claim = Claim.builder().build();

        Predicate<Claim> firstPredicate = c -> {
            assertThat(c).isSameAs(claim);
            return true;
        };

        Predicate<Claim> secondPredicate = c -> {
            assertThat(c).isSameAs(claim);
            return false;
        };

        StateFlow stateFlow = StateFlowBuilder.<FlowState>flow("FLOW")
            .initial(FlowState.STATE_1)
            .transitionTo(FlowState.STATE_2).onlyIf(firstPredicate)
            .state(FlowState.STATE_2)
            .transitionTo(FlowState.STATE_3).onlyIf(secondPredicate)
            .state(FlowState.STATE_3)
            .build();

        stateFlow.evaluate(claim);

        String stateId = stateFlow.asStateMachine().getState().getId();
        assertThat(stateId).isEqualTo("FLOW.STATE_2");
    }
}

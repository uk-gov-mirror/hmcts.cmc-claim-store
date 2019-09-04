package uk.gov.hmcts.cmc.claimstore.services.stateflow;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.statemachine.ExtendedState;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.state.State;
import reactor.core.publisher.Mono;
import uk.gov.hmcts.cmc.domain.models.Claim;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.cmc.claimstore.services.stateflow.StateFlowContext.EXTENDED_STATE_VARIABLE_KEY;
import static uk.gov.hmcts.cmc.claimstore.services.stateflow.model.State.ERROR;

@RunWith(MockitoJUnitRunner.class)
public class StateFlowTest {

    private static String TEST_STATE = "TEST_STATE";

    @Mock
    private StateMachine<String, String> mockedStateMachine;

    @SuppressWarnings("unchecked")
    private Map<Object, Object> createMockedVariables() {
        return mock(Map.class);
    }

    private ExtendedState createMockedExtendedState(Map<Object, Object> mockedVariables) {
        ExtendedState mockedExtendedState = mock(ExtendedState.class);
        when(mockedExtendedState.getVariables()).thenReturn(mockedVariables);
        return mockedExtendedState;
    }

    @SuppressWarnings("unchecked")
    private State<String, String> createMockedState(String stateName) {
        State state = mock(State.class);
        when(state.getId()).thenReturn(stateName);
        return state;
    }

    @SuppressWarnings("unchecked")
    private Mono<Void> createMockedMono() {
        return mock(Mono.class);
    }

    @Test
    public void shouldReturnAsStateMachine() {
        StateFlow stateFlow = new StateFlow(mockedStateMachine);

        assertThat(stateFlow.asStateMachine()).isSameAs(mockedStateMachine);
    }

    @Test
    public void shouldEvaluateClaim() {
        Map<Object, Object> mockedVariables = createMockedVariables();
        ExtendedState mockedExtendedState = createMockedExtendedState(mockedVariables);
        Mono<Void> mockedMono = createMockedMono();

        when(mockedStateMachine.getExtendedState()).thenReturn(mockedExtendedState);
        when(mockedStateMachine.startReactively()).thenReturn(mockedMono);

        Claim claim = Claim.builder().build();

        StateFlow stateFlow = new StateFlow(mockedStateMachine);

        assertThat(stateFlow.evaluate(claim)).isSameAs(stateFlow);
        verify(mockedVariables).put(eq(EXTENDED_STATE_VARIABLE_KEY), eq(claim));
        verify(mockedMono).block();
    }

    @Test
    public void shouldGetStateWhenStateMachineHasNoErrors() {
        State<String, String> mockedState = createMockedState(TEST_STATE);
        when(mockedStateMachine.hasStateMachineError()).thenReturn(false);
        when(mockedStateMachine.getState()).thenReturn(mockedState);

        StateFlow stateFlow = new StateFlow(mockedStateMachine);

        assertThat(stateFlow.getState())
            .extracting("name")
            .doesNotContainNull()
            .containsExactly(TEST_STATE);
    }

    @Test
    public void shouldGetStateWhenStateMachineHasErrors() {
        when(mockedStateMachine.hasStateMachineError()).thenReturn(true);
        StateFlow stateFlow = new StateFlow(mockedStateMachine);

        assertThat(stateFlow.getState())
            .extracting("name")
            .doesNotContainNull()
            .containsExactly(ERROR);
    }

}

package uk.gov.hmcts.cmc.claimstore.services.stateflow.model;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class StateTest {

    @Test
    public void shouldCreateStateFrom() {
        assertThat(State.from("STATE"))
            .extracting("name")
            .isNotNull()
            .containsExactly("STATE");
    }

    @Test
    public void shouldCreateErrorState() {
        assertThat(State.error())
            .extracting("name")
            .isNotNull()
            .containsExactly(State.ERROR);
    }

}

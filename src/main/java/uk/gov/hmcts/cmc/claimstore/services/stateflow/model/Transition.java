package uk.gov.hmcts.cmc.claimstore.services.stateflow.model;

import uk.gov.hmcts.cmc.domain.models.Claim;

import java.util.function.Predicate;

public class Transition {

    private String sourceState;

    private String targetState;

    private Predicate<Claim> condition;

    public Transition(String sourceState, String targetState) {
        this.sourceState = sourceState;
        this.targetState = targetState;
    }

    public Transition(String sourceState, String targetState, Predicate<Claim> condition) {
        this.sourceState = sourceState;
        this.targetState = targetState;
        this.condition = condition;
    }

    public String getSourceState() {
        return sourceState;
    }

    public void setSourceState(String sourceState) {
        this.sourceState = sourceState;
    }

    public String getTargetState() {
        return targetState;
    }

    public void setTargetState(String targetState) {
        this.targetState = targetState;
    }

    public Predicate<Claim> getCondition() {
        return condition;
    }

    public void setCondition(Predicate<Claim> condition) {
        this.condition = condition;
    }
}

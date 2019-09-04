package uk.gov.hmcts.cmc.claimstore.services.stateflow.model;

public class State {

    public static String ERROR = "ERROR";

    private String name;

    public static State from(String name) {
        return new State(name);
    }

    public static State error() {
        return new State(ERROR);
    }

    private State(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}

package uk.gov.hmcts.cmc.claimstore.services.stateflow.grammar;

/**
 * This specifies what can come after a SUBFLOW clause
 */
public interface SubflowNext<S>
        extends State<S>, Subflow<S>, Build<S>
{
}

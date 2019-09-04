package uk.gov.hmcts.cmc.claimstore.services.stateflow.grammar;

import uk.gov.hmcts.cmc.domain.models.Claim;

import java.util.function.Predicate;

/**
 * Represents the ONLY_IF clause
 */
public interface OnlyIf<S>
{
    OnlyIfNext<S> onlyIf(Predicate<Claim> condition);
}

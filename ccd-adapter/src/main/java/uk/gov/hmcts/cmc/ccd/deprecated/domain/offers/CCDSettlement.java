package uk.gov.hmcts.cmc.ccd.deprecated.domain.offers;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Builder;
import lombok.Value;
import uk.gov.hmcts.cmc.ccd.domain.CCDCollectionElement;
import uk.gov.hmcts.cmc.ccd.domain.offers.CCDPartyStatement;

import java.util.List;

@Value
@Builder
public class CCDSettlement {
    private List<CCDCollectionElement<CCDPartyStatement>> partyStatements;

    @JsonCreator
    public CCDSettlement(List<CCDCollectionElement<CCDPartyStatement>> partyStatements) {
        this.partyStatements = partyStatements;
    }
}

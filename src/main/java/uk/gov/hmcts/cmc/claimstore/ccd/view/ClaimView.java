package uk.gov.hmcts.cmc.claimstore.ccd.view;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.definition.BaseCaseView;
import uk.gov.hmcts.cmc.domain.models.Claim;

@Component
public class ClaimView extends BaseCaseView<Claim> {
    @Override
    public String getTab() {
        return "Case Details";
    }

    @Override
    protected void onRender(Claim theCase) {
        render(theCase.getReferenceNumber());
        render(theCase.getClaimData().getReason());
    }
}

package uk.gov.hmcts.cmc.claimstore.events.operations;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.domain.CaseEvent;
import uk.gov.hmcts.cmc.claimstore.repositories.CaseRepository;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.ClaimSubmissionOperationIndicators;

@Component
@ConditionalOnProperty(prefix = "feature_toggles", name = "async_eventOperations_enabled")
public class ClaimCreationEventsStatusService {

    private final CaseRepository caseRepository;

    @Autowired
    public ClaimCreationEventsStatusService(CaseRepository caseRepository) {
        this.caseRepository = caseRepository;
    }

    public Claim updateClaimOperationCompletion(
        String authorisation,
        Long claimId,
        ClaimSubmissionOperationIndicators operationIndicators,
        CaseEvent caseEvent){

        return caseRepository.updateClaimSubmissionOperationStatus(authorisation, claimId, operationIndicators,
            caseEvent);
    }
}

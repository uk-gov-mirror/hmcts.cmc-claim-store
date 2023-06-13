package uk.gov.hmcts.cmc.claimstore.services.unstuck;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.claimstore.events.claim.CitizenClaimCreatedEvent;
import uk.gov.hmcts.cmc.claimstore.events.claim.PostClaimOrchestrationHandler;
import uk.gov.hmcts.cmc.claimstore.exceptions.NotFoundException;
import uk.gov.hmcts.cmc.claimstore.models.idam.User;
import uk.gov.hmcts.cmc.claimstore.services.UserService;
import uk.gov.hmcts.cmc.domain.models.Claim;

import java.util.Set;
import java.util.function.Supplier;

import static java.lang.String.format;
import static uk.gov.hmcts.cmc.claimstore.controllers.support.SupportController.CLAIM_DOES_NOT_EXIST;

@Slf4j
@Service
@AllArgsConstructor
public class UnStuckClaimService {

    private final SearchStuckClaims searchStuckClaims;
    private final UserService userService;
    private final PostClaimOrchestrationHandler postClaimOrchestrationHandler;


    public void unStuckClaims() {
        try {
            final User user = userService.authenticateAnonymousCaseWorker();
            final String authorisation = user.getAuthorisation();
            Set<Claim> stuckClaims = searchStuckClaims.findStuckClaims(user);
            stuckClaims.stream().forEach(claim ->
                triggerAsyncOperation(authorisation, claim)
            );
        } catch (Exception exception) {
            log.error("Error in  unStuckClaims:: " + exception);
        }
    }

    private void triggerAsyncOperation(String authorisation, Claim claim) {
        if (claim.getClaimData().isClaimantRepresented()) {
            log.info("Claim with reference number " + claim.getReferenceNumber()
                + " is represented and not needed to unstuck");
        } else {
            log.info("Triggering unstuck operation for claim with reference number " + claim.getReferenceNumber());
            String submitterName = claim.getClaimData().getClaimant().getName();
            this.postClaimOrchestrationHandler
                .citizenIssueHandler(new CitizenClaimCreatedEvent(claim, submitterName, authorisation));
        }
    }

    private Supplier<NotFoundException> claimNotFoundException(String reference) {
        return () -> new NotFoundException(format(CLAIM_DOES_NOT_EXIST, reference));
    }
}

package uk.gov.hmcts.cmc.claimstore.events.claim;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.ccd.domain.CaseEvent;
import uk.gov.hmcts.cmc.claimstore.documents.output.PDF;
import uk.gov.hmcts.cmc.claimstore.events.operations.BulkPrintOperationService;
import uk.gov.hmcts.cmc.claimstore.events.operations.ClaimCreationEventsStatusService;
import uk.gov.hmcts.cmc.claimstore.events.operations.ClaimantOperationService;
import uk.gov.hmcts.cmc.claimstore.events.operations.DefendantOperationService;
import uk.gov.hmcts.cmc.claimstore.events.operations.NotifyStaffOperationService;
import uk.gov.hmcts.cmc.claimstore.events.operations.RepresentativeOperationService;
import uk.gov.hmcts.cmc.claimstore.events.operations.RpaOperationService;
import uk.gov.hmcts.cmc.claimstore.events.operations.UploadOperationService;
import uk.gov.hmcts.cmc.claimstore.events.solicitor.RepresentedClaimCreatedEvent;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.ClaimSubmissionOperationIndicators;
import uk.gov.hmcts.cmc.domain.models.response.YesNoOption;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

@Async("threadPoolTaskExecutor")
@Service
@ConditionalOnProperty(prefix = "feature_toggles", name = "async_eventOperations_enabled")
public class ClaimCreatedOperationHandler {
    private static final Logger logger = LoggerFactory.getLogger(ClaimCreatedOperationHandler.class);

    private final RepresentativeOperationService representativeOperationService;
    private final BulkPrintOperationService bulkPrintOperationService;
    private final ClaimantOperationService claimantOperationService;
    private final DefendantOperationService defendantOperationService;
    private final RpaOperationService rpaOperationService;
    private final NotifyStaffOperationService notifyStaffOperationService;
    private final UploadOperationService uploadOperationService;
    private final DocumentGenerationService documentGenerationService;
    private final ClaimCreationEventsStatusService eventsStatusService;

    private final ClaimCreationOperation<Claim, String, GeneratedDocuments, Claim> uploadDefendantLetter;
    private final ClaimCreationOperation<Claim, String, GeneratedDocuments, Claim> bulkPrint;
    private final ClaimCreationOperation<Claim, String, GeneratedDocuments, Claim> notifyStaff;

    private final Predicate<ClaimSubmissionOperationIndicators> isPinOperationSuccess = indicators ->
        Stream.of(indicators.getDefendantNotification(), indicators.getRPA(),
            indicators.getBulkPrint(), indicators.getStaffNotification())
            .anyMatch(ind -> ind.equals(YesNoOption.NO));

    @Autowired
    @SuppressWarnings("squid:S00107")
    public ClaimCreatedOperationHandler(
        RepresentativeOperationService representativeOperationService,
        BulkPrintOperationService bulkPrintOperationService,
        ClaimantOperationService claimantOperationService,
        DefendantOperationService defendantOperationService,
        RpaOperationService rpaOperationService,
        NotifyStaffOperationService notifyStaffOperationService,
        UploadOperationService uploadOperationService,
        DocumentGenerationService documentGenerationService,
        ClaimCreationEventsStatusService eventsStatusService
    ) {
        this.representativeOperationService = representativeOperationService;
        this.bulkPrintOperationService = bulkPrintOperationService;
        this.claimantOperationService = claimantOperationService;
        this.defendantOperationService = defendantOperationService;
        this.rpaOperationService = rpaOperationService;
        this.notifyStaffOperationService = notifyStaffOperationService;
        this.uploadOperationService = uploadOperationService;
        this.documentGenerationService = documentGenerationService;
        this.eventsStatusService = eventsStatusService;

        uploadDefendantLetter = (claim, auth, docs) -> uploadOperationService.uploadDocument(claim, auth,
            docs.getDefendantLetter());

        bulkPrint = (claim, auth, docs) -> bulkPrintOperationService.print(claim, docs.getDefendantLetterDoc(),
            docs.getSealedClaimDoc(), auth);

        notifyStaff = (claim, auth, docs) -> notifyStaffOperationService.notify(claim, auth, docs.getSealedClaim(),
            docs.getDefendantLetter());

    }

    @EventListener
    public void citizenIssueHandler(CitizenClaimCreatedEvent event) {
        try {
            Claim claim = event.getClaim();
            String authorisation = event.getAuthorisation();
            String submitterName = event.getSubmitterName();
            GeneratedDocuments generatedDocuments = documentGenerationService.generateForCitizen(claim, authorisation);
            ClaimSubmissionOperationIndicators indicator = claim.getClaimSubmissionOperationIndicators();
            Claim updatedClaim = claim;

            if (isPinOperationSuccess.test(indicator)) {
                updatedClaim = CompletableFuture.supplyAsync(() ->
                    uploadDefendantLetter.perform(claim, authorisation, generatedDocuments)
                ).thenApplyAsync(claimAfterDefendantLetterSuccess ->
                    bulkPrint.perform(claimAfterDefendantLetterSuccess, authorisation, generatedDocuments)
                ).thenApplyAsync(claimAfterBulkPrintSuccess ->
                    notifyStaff.perform(claimAfterBulkPrintSuccess, authorisation, generatedDocuments)
                ).thenApplyAsync(updClaim ->
                    defendantOperationService.notify(updClaim, generatedDocuments.getPin(), submitterName,
                        authorisation)
                ).get();
            }

            updatedClaim = eventsStatusService.updateClaimOperationCompletion(authorisation, updatedClaim.getId(),
                indicator, CaseEvent.PIN_GENERATION_OPERATIONS);

            //TODO Check if above operation indicators are successful, if no return else  continue

            updatedClaim = uploadOperationService.uploadDocument(
                updatedClaim,
                authorisation,
                generatedDocuments.getSealedClaim()
            );

            updatedClaim = eventsStatusService.updateClaimOperationCompletion(authorisation, updatedClaim.getId(),
                indicator, CaseEvent.PIN_GENERATION_OPERATIONS);

            updatedClaim = uploadOperationService.uploadDocument(
                updatedClaim,
                authorisation,
                generatedDocuments.getClaimIssueReceipt()
            );

            updatedClaim = eventsStatusService.updateClaimOperationCompletion(authorisation, updatedClaim.getId(),
                indicator, CaseEvent.PIN_GENERATION_OPERATIONS);

            updatedClaim = rpaOperationService.notify(updatedClaim, authorisation, generatedDocuments.getSealedClaim());
            updatedClaim = eventsStatusService.updateClaimOperationCompletion(authorisation, updatedClaim.getId(),
                indicator, CaseEvent.PIN_GENERATION_OPERATIONS);

            updatedClaim = claimantOperationService.notifyCitizen(updatedClaim, submitterName, authorisation);
            updatedClaim = eventsStatusService.updateClaimOperationCompletion(authorisation, updatedClaim.getId(),
                indicator, CaseEvent.PIN_GENERATION_OPERATIONS);


        } catch (Exception e) {
            logger.error("failed operation processing for event ()", event, e);
        }
    }

    @EventListener
    public void representativeIssueHandler(RepresentedClaimCreatedEvent event) {
        try {
            Claim claim = event.getClaim();
            String authorisation = event.getAuthorisation();

            GeneratedDocuments generatedDocuments = documentGenerationService.generateForRepresentative(claim);
            PDF sealedClaim = generatedDocuments.getSealedClaim();

            Claim updatedClaim = uploadOperationService.uploadDocument(claim, authorisation, sealedClaim);
            updatedClaim = rpaOperationService.notify(updatedClaim, authorisation, sealedClaim);
            updatedClaim = notifyStaffOperationService.notify(updatedClaim, authorisation, sealedClaim);

            String submitterName = event.getRepresentativeName().orElse(null);
            representativeOperationService.notify(updatedClaim, submitterName, authorisation);

            claimantOperationService
                .confirmRepresentative(updatedClaim, submitterName, event.getRepresentativeEmail(), authorisation);

            //TODO update claim state
            //claimService.updateState

        } catch (Exception e) {
            logger.error("failed operation processing for event ()", event, e);
        }
    }

    @FunctionalInterface
    interface ClaimCreationOperation<C, A, G, U> {
        U perform(C claim, A authorisation, G generateddocs);
    }
}



package uk.gov.hmcts.cmc.claimstore.ccd;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ccd.ICCDApplication;
import uk.gov.hmcts.ccd.definition.CaseEventField;
import uk.gov.hmcts.ccd.domain.model.aggregated.CaseViewTrigger;
import uk.gov.hmcts.ccd.domain.model.aggregated.ProfileCaseState;
import uk.gov.hmcts.ccd.domain.model.std.CaseDataContent;
import uk.gov.hmcts.ccd.endpoint.exceptions.ApiException;
import uk.gov.hmcts.cmc.claimstore.events.EventProducer;
import uk.gov.hmcts.cmc.claimstore.exceptions.NotFoundException;
import uk.gov.hmcts.cmc.claimstore.processors.JsonMapper;
import uk.gov.hmcts.cmc.claimstore.repositories.ClaimRepository;
import uk.gov.hmcts.cmc.claimstore.rules.MoreTimeRequestRule;
import uk.gov.hmcts.cmc.claimstore.services.ResponseDeadlineCalculator;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class CCDImpl implements ICCDApplication<Claim> {

    private class SearchableFields {
        private static final String REFERENCE_NUMBER = "referenceNumber";
        private static final String SUBMITTER_ID = "submitterId";
        private static final String DEFENDANT_ID = "defendantId";
    }

    private class EventIds {
        private static final String MORE_TIME_REQUESTED_ON_PAPER = "more-time-requested-on-paper";
    }

    private class MoreTimeRequestedEventData {

        @CaseEventField(label = "Reason", order = 1)
        private String reason;

        public String getReason() {
            return reason;
        }

        public void setReason(String reason) {
            this.reason = reason;
        }
    }

    @Autowired
    private ClaimRepository repository;
    @Autowired
    private EventProducer eventProducer;
    @Autowired
    private ResponseDeadlineCalculator responseDeadlineCalculator;
    @Autowired
    private JsonMapper jsonMapper;
    @Autowired
    private MoreTimeRequestRule moreTimeRequestRule;

    // Allows CCD to fetch cases from the service.
    @Override
    public List<Claim> getCases(Map<String, String> searchCriteria) {
        if (searchCriteria != null) {
            if (searchCriteria.containsKey(SearchableFields.REFERENCE_NUMBER)) {
                return mapOptionalToList(repository.getByClaimReferenceNumberAnonymous(searchCriteria.get(SearchableFields.REFERENCE_NUMBER)));
            }
            if (searchCriteria.containsKey(SearchableFields.SUBMITTER_ID)) {
                return repository.getBySubmitterId(searchCriteria.get(SearchableFields.SUBMITTER_ID));
            }
            if (searchCriteria.containsKey(SearchableFields.DEFENDANT_ID)) {
                return repository.getByDefendantId(searchCriteria.get(SearchableFields.DEFENDANT_ID));
            }
        }
        return repository.findAll();
    }

    private List<Claim> mapOptionalToList(Optional<Claim> result) {
        return result.map(ImmutableList::of).orElse(ImmutableList.of());
    }

    // Called by CCD when a caseworker creates a case.
    @Override
    public String saveCase(Claim input) {
        Claim claim = SampleClaim.builder()
            .withClaimId(null)
            .build();

        Long id = repository.saveSubmittedByClaimant(jsonMapper.toJson(claim), input.getSubmitterId(), input.getLetterHolderId(),
            LocalDate.now(), claim.getResponseDeadline(), UUID.randomUUID().toString(), input.getSubmitterEmail());

        return Long.toString(id);
    }

    @Override
    public Claim getCase(String id) {
        return repository.getById(Long.valueOf(id)).orElse(null);
    }

    // Inform CCD of the events our case can have.
    @Override
    public ImmutableSet<String> getEvents() {
        return ImmutableSet.of("Created");
    }

    @Override
    public List<CaseViewTrigger> getTriggers(String caseId) {
        Claim claim = repository.getById(Long.valueOf(caseId))
            .orElseThrow(() -> new NotFoundException("Case not found"));

        List<String> errors = moreTimeRequestRule.validateMoreTimeCanBeRequested(claim);

        if (errors.isEmpty()) {
            CaseViewTrigger trigger = new CaseViewTrigger();
            trigger.setId(EventIds.MORE_TIME_REQUESTED_ON_PAPER);
            trigger.setName("More time requested on paper");
            trigger.setDescription("More time requested on paper");

            return Arrays.asList(trigger);
        }

        return Arrays.asList();
    }

    @Override
    public Map<String, Class> eventsMapping() {
        return ImmutableMap.of(EventIds.MORE_TIME_REQUESTED_ON_PAPER, MoreTimeRequestedEventData.class);
    }

    @Override
    public void handleTrigger(String caseID, CaseDataContent caseDetails) {
        String eventId = caseDetails.getEvent().getEventId();

        switch (eventId) {
            case EventIds.MORE_TIME_REQUESTED_ON_PAPER:
                Claim claim = repository.getById(Long.valueOf(caseID))
                    .orElseThrow(() -> new NotFoundException("Case not found"));

                List<String> errors = moreTimeRequestRule.validateMoreTimeCanBeRequested(claim);

                if (!errors.isEmpty()) {
                    throw new ApiException("Action cannot be performed")
                        .withErrors(errors);
                }

                LocalDate extendedResponseDeadline = responseDeadlineCalculator.calculatePostponedResponseDeadline(claim.getIssuedOn());

                repository.requestMoreTime(claim.getExternalId(), extendedResponseDeadline);
                break;
            default:
                throw new RuntimeException("Unsupported event: " + eventId);
        }
    }

    @Override
    public ProfileCaseState getCaseState(String caseId) {
        return null;
    }
}

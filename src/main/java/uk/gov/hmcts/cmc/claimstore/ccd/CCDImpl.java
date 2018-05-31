package uk.gov.hmcts.cmc.claimstore.ccd;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ccd.ICCDApplication;
import uk.gov.hmcts.ccd.domain.model.aggregated.CaseViewTrigger;
import uk.gov.hmcts.ccd.domain.model.aggregated.ProfileCaseState;
import uk.gov.hmcts.cmc.claimstore.repositories.ClaimRepository;
import uk.gov.hmcts.cmc.domain.models.Claim;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class CCDImpl implements ICCDApplication<Claim> {

    private static final String REFERENCE_NUMBER_SEARCH_CRITERIA = "referenceNumber";
    private static final String SUBMITTER_ID_SEARCH_CRITERIA = "submitterId";
    private static final String DEFENDANT_ID_SEARCH_CRITERIA = "defendantId";

    @Autowired
    private ClaimRepository repository;

    // Allows CCD to fetch cases from the service.
    @Override
    public List<Claim> getCases(Map<String, String> searchCriteria) {
        if (searchCriteria != null) {
            if (searchCriteria.containsKey(REFERENCE_NUMBER_SEARCH_CRITERIA)) {
                return mapOptionalToList(repository.getByClaimReferenceNumberAnonymous(searchCriteria.get(REFERENCE_NUMBER_SEARCH_CRITERIA)));
            }
            if (searchCriteria.containsKey(SUBMITTER_ID_SEARCH_CRITERIA)) {
                return repository.getBySubmitterId(searchCriteria.get(SUBMITTER_ID_SEARCH_CRITERIA));
            }
            if (searchCriteria.containsKey(DEFENDANT_ID_SEARCH_CRITERIA)) {
                return repository.getByDefendantId(searchCriteria.get(DEFENDANT_ID_SEARCH_CRITERIA));
            }
        }
        return repository.findAll();
    }

    private List<Claim> mapOptionalToList(Optional<Claim> result) {
        return result.map(ImmutableList::of).orElse(ImmutableList.of());
    }

    // Called by CCD when a caseworker creates a case.
    @Override
    public void saveCase(Claim c) {
        //
    }

    // Inform CCD of the events our case can have.
    @Override
    public ImmutableSet<String> getEvents() {
        return ImmutableSet.of("created");
    }

    @Override
    public List<CaseViewTrigger> getTriggers(String caseId) {
        return null;
    }

    @Override
    public ProfileCaseState getCaseState(String caseId) {
        return null;
    }
}

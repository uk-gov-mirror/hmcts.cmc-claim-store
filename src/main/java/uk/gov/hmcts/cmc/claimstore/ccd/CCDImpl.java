package uk.gov.hmcts.cmc.claimstore.ccd;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ccd.ICCDApplication;
import uk.gov.hmcts.ccd.domain.model.aggregated.CaseViewTrigger;
import uk.gov.hmcts.ccd.domain.model.aggregated.ProfileCaseState;
import uk.gov.hmcts.cmc.claimstore.repositories.ClaimRepository;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;

import java.util.List;
import java.util.Map;

/**
 * Sample implementation of the CCD contract.
 */
@Service
public class CCDImpl implements ICCDApplication<Claim> {

    @Autowired
    private ClaimRepository repository;

    // Allows CCD to fetch cases from the service.
    @Override
    public List<Claim> getCases(Map<String, String> searchCriteria) {
        return repository.findAll();
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

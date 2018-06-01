package uk.gov.hmcts.cmc.claimstore.ccd;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ccd.ICCDApplication;
import uk.gov.hmcts.ccd.domain.model.aggregated.CaseViewTrigger;
import uk.gov.hmcts.ccd.domain.model.aggregated.ProfileCaseState;
import uk.gov.hmcts.cmc.claimstore.exceptions.NotFoundException;
import uk.gov.hmcts.cmc.claimstore.repositories.ClaimRepository;
import uk.gov.hmcts.cmc.domain.models.Claim;

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
        if (searchCriteria != null && !searchCriteria.isEmpty()) {
            for (String key : searchCriteria.keySet()) {
                switch (key) {
                    case "id":
                        return ImmutableList.of(repository.getById(Long.valueOf(searchCriteria.get("id")))
                            .orElseThrow(() -> new NotFoundException("")));
                    case "externalId":
                        return ImmutableList.of(repository.getById(Long.valueOf(searchCriteria.get("externalId")))
                            .orElseThrow(() -> new NotFoundException("")));
                    case "submitterId":
                        return ImmutableList.of(repository.getById(Long.valueOf(searchCriteria.get("submitterId")))
                            .orElseThrow(() -> new NotFoundException("")));
                    case "referenceNumber":
                        return ImmutableList.of(repository.getById(Long.valueOf(searchCriteria.get("submitterId")))
                            .orElseThrow(() -> new NotFoundException("")));


                }
            }
        }
        return repository.findAll();
    }

    // Called by CCD when a caseworker creates a case.
    @Override
    public void saveCase(Claim claim) {
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

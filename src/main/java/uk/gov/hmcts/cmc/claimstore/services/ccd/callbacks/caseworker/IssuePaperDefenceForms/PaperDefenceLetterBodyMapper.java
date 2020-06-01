package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.caseworker.IssuePaperDefenceForms;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.domain.CCDAddress;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.ccd.domain.CCDParty;
import uk.gov.hmcts.cmc.claimstore.services.ccd.legaladvisor.DocAssemblyTemplateBody;

import java.time.Clock;
import java.time.LocalDate;

import static uk.gov.hmcts.cmc.domain.utils.LocalDateTimeFactory.UTC_ZONE;

@Component
public class PaperDefenceLetterBodyMapper {
    private final Clock clock;

    public PaperDefenceLetterBodyMapper(Clock clock) {
        this.clock = clock;
    }

    public DocAssemblyTemplateBody coverLetterTemplateMapper(CCDCase ccdCase, String caseworkerName) {

        LocalDate currentDate = LocalDate.now(clock.withZone(UTC_ZONE));

        return DocAssemblyTemplateBody.builder()
                //change variables
                .currentDate(currentDate)
                .referenceNumber(ccdCase.getPreviousServiceCaseReference())
                .hearingCourtName(ccdCase.getHearingCourtName())
                .hearingCourtAddress(ccdCase.getHearingCourtAddress())
                .caseworkerName(caseworkerName)
                .caseName(ccdCase.getCaseName())
                .build();
    }

    private DocAssemblyTemplateBody oconFormCommonTemplateMapper(CCDCase ccdCase) {
        CCDParty applicant = ccdCase.getApplicants().get(0).getValue().getPartyDetail();
        CCDParty respondent = ccdCase.getRespondents().get(0).getValue().getClaimantProvidedDetail();
        CCDAddress claimantAddress = applicant.getCorrespondenceAddress() == null
                ? applicant.getPrimaryAddress() : applicant.getCorrespondenceAddress();
        CCDAddress defendantAddress = respondent.getCorrespondenceAddress() == null
                ? respondent.getPrimaryAddress() : respondent.getCorrespondenceAddress();
        //set deadlines

        return DocAssemblyTemplateBody.builder()
                .referenceNumber(ccdCase.getPreviousServiceCaseReference())
                .responseDeadline()
                .extendedResponseDeadline()
                .claimAmount(ccdCase.getTotalAmount())
                //do I need specific defendant address attribute?
                .partyAddress(defendantAddress)
                //dq or normal court?
                .claimantName(String.join(" ", applicant.getTitle(), applicant.getFirstName(), applicant.getLastName()))
                .claimantPhone(applicant.getTelephoneNumber().toString())
                .claimantEmail(applicant.getEmailAddress())
                .claimantAddress(claimantAddress)
                .build();
    }

    public DocAssemblyTemplateBody oconFormIndividualWithDQsTemplateMapper(CCDCase ccdCase) {
        DocAssemblyTemplateBody commonTemplate = oconFormCommonTemplateMapper(ccdCase);
        return  commonTemplate.toBuilder().preferredCourt(ccdCase.getPreferredDQCourt()).build();
    }
    public DocAssemblyTemplateBody oconFormIndividualWithoutDQsTemplateMapper(CCDCase ccdCase) {
        DocAssemblyTemplateBody commonTemplate = oconFormCommonTemplateMapper(ccdCase);
        return  commonTemplate;
    }

    public DocAssemblyTemplateBody oconFormWithBusinessNameWithDQsTemplateMapper(CCDCase ccdCase) {
        DocAssemblyTemplateBody commonTemplate = oconFormCommonTemplateMapper(ccdCase);
        return  commonTemplate.toBuilder()
                .soleTradingTraderName(ccdCase.getRespondents().get(0).getValue().getPartyDetail().getBusinessName())
                .preferredCourt(ccdCase.getPreferredDQCourt())
                .build();
    }

    public DocAssemblyTemplateBody oconFormWithBusinessNameWithoutDQsTemplateMapper(CCDCase ccdCase) {
        DocAssemblyTemplateBody commonTemplate = oconFormCommonTemplateMapper(ccdCase);
        return commonTemplate.toBuilder()
                .soleTradingTraderName(ccdCase.getRespondents().get(0).getValue().getPartyDetail().getBusinessName())
                .build();
    }
}

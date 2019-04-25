package uk.gov.hmcts.cmc.domain.models;

import lombok.Builder;
import lombok.Value;
import uk.gov.hmcts.cmc.domain.models.response.YesNoOption;

import java.util.function.Function;
import java.util.function.Supplier;

@Value
public class ClaimSubmissionOperationIndicators {
    private YesNoOption claimantNotification;
    private YesNoOption defendantNotification;
    private YesNoOption bulkPrint;
    private YesNoOption RPA;
    private YesNoOption staffNotification;
    private YesNoOption sealedClaimUpload;
    private YesNoOption claimIssueReceiptUpload;
    private YesNoOption defendantPinLetterUpload;

    private Function<YesNoOption, YesNoOption> defaultIfNotPresent
        = (value) -> value == null? YesNoOption.NO:value;

    @Builder
    private ClaimSubmissionOperationIndicators(
        YesNoOption claimantNotification,
        YesNoOption defendantNotification,
        YesNoOption bulkPrint,
        YesNoOption RPA,
        YesNoOption staffNotification,
        YesNoOption sealedClaimUpload,
        YesNoOption claimIssueReceiptUpload,
        YesNoOption defendantPinLetterUpload) {
        this.claimantNotification = defaultIfNotPresent.apply(claimantNotification);
        this.defendantNotification = defaultIfNotPresent.apply(defendantNotification);
        this.bulkPrint = defaultIfNotPresent.apply(bulkPrint);
        this.RPA = defaultIfNotPresent.apply(RPA);
        this.staffNotification = defaultIfNotPresent.apply(staffNotification);
        this.sealedClaimUpload = defaultIfNotPresent.apply(sealedClaimUpload);
        this.claimIssueReceiptUpload = defaultIfNotPresent.apply(claimIssueReceiptUpload);
        this.defendantPinLetterUpload = defaultIfNotPresent.apply(defendantPinLetterUpload);
    }
}

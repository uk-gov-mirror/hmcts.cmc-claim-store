package uk.gov.hmcts.cmc.claimstore.controllers;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.cmc.claimstore.services.ClaimService;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.PaymentStatus;
import uk.gov.hmcts.cmc.domain.models.PaymentUpdate;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.hmcts.reform.authorisation.validators.AuthTokenValidator;

import java.math.BigDecimal;
import java.util.List;

import static java.util.Collections.singletonList;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.cmc.domain.models.ClaimFeatures.ADMISSIONS;
import static uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim.CLAIM_ID;
import static uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim.DEFENDANT_ID;
import static uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim.EXTERNAL_ID;
import static uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim.LETTER_HOLDER_ID;
import static uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim.SUBMITTER_EMAIL;
import static uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim.USER_ID;
import static uk.gov.hmcts.cmc.domain.utils.DatesProvider.ISSUE_DATE;
import static uk.gov.hmcts.cmc.domain.utils.DatesProvider.NOW_IN_LOCAL_ZONE;

@RunWith(MockitoJUnitRunner.class)
public class PaymentControllerTest {

    private static final String AUTHORISATION = "Bearer: aaa";
    private static final List<String> FEATURES = singletonList(ADMISSIONS.getValue());

    private PaymentController paymentController;

    @Mock
    private AuthTokenValidator authTokenValidator;

    @Mock
    private ClaimService claimService;

    private PaymentUpdate paymentUpdate = null;

    private static final Claim claim = SampleClaim.builder()
        .withClaimId(CLAIM_ID)
        .withSubmitterId(USER_ID)
        .withLetterHolderId(LETTER_HOLDER_ID)
        .withDefendantId(DEFENDANT_ID)
        .withExternalId(EXTERNAL_ID)
        .withReferenceNumber(SampleClaim.REFERENCE_NUMBER)
        .withCreatedAt(NOW_IN_LOCAL_ZONE)
        .withIssuedOn(ISSUE_DATE)
        .withSubmitterEmail(SUBMITTER_EMAIL)
        .build();

    @Before
    public void setup() {
        paymentUpdate = PaymentUpdate.builder()
            .amount(new BigDecimal(200))
            .status(PaymentStatus.SUCCESS.name())
            .reference("Ref")
            .ccdCaseNumber("CCD-111")
            .feeId("111")
            .build();
        paymentController = new PaymentController(claimService, authTokenValidator);
    }

    @Test
    public void updateCardPayment() {
        when(authTokenValidator.getServiceName(AUTHORISATION)).thenReturn("fees_and_payments");
        when(claimService.updateCardPayment(AUTHORISATION, paymentUpdate)).thenReturn(claim);

        paymentController.updateCardPayment(AUTHORISATION, paymentUpdate);

        //then
        Assert.assertNotNull(claim);
    }
}

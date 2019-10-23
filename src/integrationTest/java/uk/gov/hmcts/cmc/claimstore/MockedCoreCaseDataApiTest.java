package uk.gov.hmcts.cmc.claimstore;

import org.junit.Test;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MvcResult;
import uk.gov.hmcts.cmc.domain.models.ClaimData;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleCaseDetails;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaimData;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleStartEventResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.document.domain.Classification;

import java.util.Collections;

import static com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder.okForJson;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static java.net.HttpURLConnection.HTTP_INTERNAL_ERROR;
import static java.net.HttpURLConnection.HTTP_OK;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.CLAIM_ISSUE_RECEIPT_UPLOAD;
import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.ISSUE_CASE;
import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.SEALED_CLAIM_UPLOAD;
import static uk.gov.hmcts.cmc.claimstore.utils.ResourceLoader.successfulDocumentManagementUploadResponse;

@TestPropertySource(
    properties = {
        "core_case_data.api.url=http://localhost:${wiremock.server.port}"
    }
)
@AutoConfigureWireMock(port = 0)
public class MockedCoreCaseDataApiTest extends BaseSaveTest {

    protected final CaseDetails representativeSampleCaseDetails =
        SampleCaseDetails.builder().buildRepresentativeCaseDetails();
    protected final CaseDetails citizenSampleCaseDetails =
        SampleCaseDetails.builder().buildCitizenCaseDetails();
    protected final StartEventResponse representativeStartEventResponse =
        SampleStartEventResponse.builder().buildRepresentativeStartEventResponse();
    protected final StartEventResponse citizenStartEventResponse =
        SampleStartEventResponse.builder().buildCitizenStartEventResponse();

    @Test
    public void shouldSuccessfullySubmitClaimForCitizen() throws Exception {
        final ClaimData submittedByClaimant = SampleClaimData.submittedByClaimant();
        final String caseId = citizenSampleCaseDetails.getId().toString();

        makeSuccessfulIssueClaimRequestForCitizen(submittedByClaimant, caseId);
    }

    @Test
    public void shouldSuccessfullySubmitClaimForRepresentative() throws Exception {
        final ClaimData legalRepresentativeClaimData = SampleClaimData.submittedByLegalRepresentative();
        final String caseId = representativeSampleCaseDetails.getId().toString();

        makeSuccessfulIssueClaimRequestForRepresentative(legalRepresentativeClaimData, caseId);
    }

    protected MvcResult makeSuccessfulIssueClaimRequestForRepresentative(ClaimData claimData, String caseId)
        throws Exception {
        final String externalId = claimData.getExternalId().toString();

        commonStubStepsClaimRequestForRepresentative(caseId, externalId);

        return makeIssueClaimRequest(claimData, SOLICITOR_AUTHORISATION_TOKEN)
            .andExpect(status().isOk())
            .andReturn();
    }

    protected void commonStubStepsClaimRequestForRepresentative(String caseId, String externalId) {
        stubForSearchNonExistingClaimForRepresentative(externalId);
        stubForStartForRepresentative();
        stubForSubmitForRepresentative(externalId);

        stubForStartEventForRepresentative(caseId, SEALED_CLAIM_UPLOAD.getValue());
        stubForSubmitEventForRepresentative(caseId,
            representativeStartEventResponse.getCaseDetails().getId().toString());
        stubForStartEventForRepresentative(caseId, ISSUE_CASE.getValue());

        given(authTokenGenerator.generate()).willReturn(SOLICITOR_AUTHORISATION_TOKEN);
        given(documentUploadClient
            .upload(anyString(), anyString(), anyString(), anyList(), any(Classification.class), anyList()))
            .willReturn(successfulDocumentManagementUploadResponse());

    }

    protected MvcResult makeSuccessfulIssueClaimRequestForCitizen(ClaimData claimData, String caseId) throws Exception {
        final String externalId = claimData.getExternalId().toString();

        commonStubStepsClaimRequestForCitizen(caseId, externalId);

        return makeIssueClaimRequest(claimData, AUTHORISATION_TOKEN)
            .andExpect(status().isOk())
            .andReturn();
    }

    protected void commonStubStepsClaimRequestForCitizen(String caseId, String externalId) {
        stubForSearchNonExistingClaimForCitizen(externalId);
        stubForStartForCitizen();
        stubForSubmitForCitizen(externalId);

        stubForStartEventForCitizen(caseId, CLAIM_ISSUE_RECEIPT_UPLOAD.getValue());
        stubForSubmitEventForCitizen(caseId,
            citizenStartEventResponse.getCaseDetails().getId().toString());
        stubForStartEventForCitizen(caseId, SEALED_CLAIM_UPLOAD.getValue());
        stubForStartEventForCitizen(caseId, ISSUE_CASE.getValue());

        given(authTokenGenerator.generate()).willReturn(AUTHORISATION_TOKEN);
        given(documentUploadClient
            .upload(anyString(), anyString(), anyString(), anyList(), any(Classification.class), anyList()))
            .willReturn(successfulDocumentManagementUploadResponse());

    }

    protected void stubForSearchNonExistingClaimForCitizen(String externalId) {
        final String URI = "/citizens/" + USER_ID + "/jurisdictions/" + JURISDICTION_ID
                         + "/case-types/" + CASE_TYPE_ID + "/cases" + "?" + "case.externalId="
                         + externalId + "&" + "sortDirection=desc" + "&" + "page=1";

        stubFor(get(urlEqualTo(URI))
            .withHeader(HttpHeaders.CONTENT_TYPE, equalTo(MediaType.APPLICATION_JSON_VALUE))
            .withHeader(HttpHeaders.AUTHORIZATION, equalTo(AUTHORISATION_TOKEN))
            .willReturn(okForJson(Collections.emptyList()))
        );
    }

    protected void stubForSearchExistingClaimForCitizen(String externalId) {
        final String URI = "/citizens/" + USER_ID + "/jurisdictions/" + JURISDICTION_ID
            + "/case-types/" + CASE_TYPE_ID + "/cases" + "?" + "case.externalId="
            + externalId + "&" + "sortDirection=desc" + "&" + "page=1";

        stubFor(get(urlEqualTo(URI))
            .withHeader(HttpHeaders.CONTENT_TYPE, equalTo(MediaType.APPLICATION_JSON_VALUE))
            .withHeader(HttpHeaders.AUTHORIZATION, equalTo(AUTHORISATION_TOKEN))
            .willReturn(okForJson(Collections.singletonList(citizenSampleCaseDetails)))
        );
    }

    protected void stubForSearchNonExistingClaimForRepresentative(String externalId) {
        final String URI = "/caseworkers/" + USER_ID + "/jurisdictions/" + JURISDICTION_ID
                         + "/case-types/" + CASE_TYPE_ID + "/cases" + "?" + "case.externalId="
                         + externalId + "&" + "sortDirection=desc" + "&" + "page=1";

        stubFor(get(urlEqualTo(URI))
            .withHeader(HttpHeaders.CONTENT_TYPE, equalTo(MediaType.APPLICATION_JSON_VALUE))
            .withHeader(HttpHeaders.AUTHORIZATION, equalTo(SOLICITOR_AUTHORISATION_TOKEN))
            .willReturn(okForJson(Collections.emptyList()))
        );
    }

    protected void stubForSearchExistingClaimForRepresentative(String externalId) {
        final String URI = "/caseworkers/" + USER_ID + "/jurisdictions/" + JURISDICTION_ID
            + "/case-types/" + CASE_TYPE_ID + "/cases" + "?" + "case.externalId="
            + externalId + "&" + "sortDirection=desc" + "&" + "page=1";

        stubFor(get(urlEqualTo(URI))
            .withHeader(HttpHeaders.CONTENT_TYPE, equalTo(MediaType.APPLICATION_JSON_VALUE))
            .withHeader(HttpHeaders.AUTHORIZATION, equalTo(SOLICITOR_AUTHORISATION_TOKEN))
            .willReturn(okForJson(Collections.singletonList(representativeSampleCaseDetails)))
        );
    }

    protected void stubForStartForRepresentative() {

        stubFor(get(urlEqualTo(getStartForRepresentativeURI()))
            .withHeader(HttpHeaders.CONTENT_TYPE, equalTo(MediaType.APPLICATION_JSON_VALUE))
            .withHeader(HttpHeaders.AUTHORIZATION, equalTo(SOLICITOR_AUTHORISATION_TOKEN))
            .willReturn(aResponse()
                .withStatus(HTTP_OK)
                .withBody(jsonMapper.toJson(representativeStartEventResponse)))
        );
    }

    protected void stubForStartForRepresentativeWithServerError() {

        stubFor(get(urlEqualTo(getStartForRepresentativeURI()))
            .withHeader(HttpHeaders.CONTENT_TYPE, equalTo(MediaType.APPLICATION_JSON_VALUE))
            .withHeader(HttpHeaders.AUTHORIZATION, equalTo(SOLICITOR_AUTHORISATION_TOKEN))
            .willReturn(aResponse()
                .withStatus(HTTP_INTERNAL_ERROR))
        );
    }

    protected void stubForStartForCitizen() {

        stubFor(get(urlEqualTo(getStartForCitizenURI()))
            .withHeader(HttpHeaders.CONTENT_TYPE, equalTo(MediaType.APPLICATION_JSON_VALUE))
            .withHeader(HttpHeaders.AUTHORIZATION, equalTo(AUTHORISATION_TOKEN))
            .willReturn(aResponse()
                .withStatus(HTTP_OK)
                .withBody(jsonMapper.toJson(citizenStartEventResponse)))
        );
    }

    protected void stubForStartForCitizenWithServerError() {

        stubFor(get(urlEqualTo(getStartForCitizenURI()))
            .withHeader(HttpHeaders.CONTENT_TYPE, equalTo(MediaType.APPLICATION_JSON_VALUE))
            .withHeader(HttpHeaders.AUTHORIZATION, equalTo(AUTHORISATION_TOKEN))
            .willReturn(aResponse()
                .withStatus(HTTP_INTERNAL_ERROR))
        );
    }

    protected void stubForSubmitForRepresentative(String externalId) {

        stubFor(post(urlEqualTo(getSubmitForRepresentativeURI()))
            .withHeader(HttpHeaders.CONTENT_TYPE, equalTo(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .withHeader(HttpHeaders.AUTHORIZATION, equalTo(SOLICITOR_AUTHORISATION_TOKEN))
            .withRequestBody(containing(externalId))
            .willReturn(aResponse()
                .withStatus(HTTP_OK)
                .withBody(jsonMapper.toJson(representativeSampleCaseDetails)))
        );
    }

    protected void stubForSubmitForRepresentativeWithServerError(String externalId) {

        stubFor(post(urlEqualTo(getSubmitForRepresentativeURI()))
            .withHeader(HttpHeaders.CONTENT_TYPE, equalTo(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .withHeader(HttpHeaders.AUTHORIZATION, equalTo(SOLICITOR_AUTHORISATION_TOKEN))
            .withRequestBody(containing(externalId))
            .willReturn(aResponse()
                .withStatus(HTTP_INTERNAL_ERROR)));
    }

    protected void stubForSubmitForCitizen(String externalId) {
        final String URI = "/citizens/" + USER_ID + "/jurisdictions/" + JURISDICTION_ID
                         + "/case-types/" + CASE_TYPE_ID + "/cases" + "?"
                         + "ignore-warning=" + IGNORE_WARNING;

        stubFor(post(urlEqualTo(URI))
            .withHeader(HttpHeaders.CONTENT_TYPE, equalTo(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .withHeader(HttpHeaders.AUTHORIZATION, equalTo(AUTHORISATION_TOKEN))
            .withRequestBody(containing(externalId))
                .willReturn(aResponse()
                    .withStatus(HTTP_OK)
                    .withBody(jsonMapper.toJson(citizenSampleCaseDetails)))
        );
    }

    protected void stubForStartEventForRepresentative(String caseId, String eventTriggerType) {
        final String URI = "/caseworkers/" + USER_ID + "/jurisdictions/" + JURISDICTION_ID
                         + "/case-types/" + CASE_TYPE_ID + "/cases/" + caseId
                         + "/event-triggers/" + eventTriggerType + "/token";

        stubFor(get(urlEqualTo(URI))
            .withHeader(HttpHeaders.CONTENT_TYPE, equalTo(MediaType.APPLICATION_JSON_VALUE))
            .withHeader(HttpHeaders.AUTHORIZATION, equalTo(SOLICITOR_AUTHORISATION_TOKEN))
            .willReturn(aResponse()
                .withStatus(HTTP_OK)
                .withBody(jsonMapper.toJson(representativeStartEventResponse)))
        );
    }

    protected void stubForStartEventForCitizen(String caseId, String eventTriggerType) {
        final String URI = "/citizens/" + USER_ID + "/jurisdictions/" + JURISDICTION_ID
                         + "/case-types/" + CASE_TYPE_ID + "/cases/" + caseId
                         + "/event-triggers/" + eventTriggerType + "/token";

        stubFor(get(urlEqualTo(URI))
            .withHeader(HttpHeaders.CONTENT_TYPE, equalTo(MediaType.APPLICATION_JSON_VALUE))
            .withHeader(HttpHeaders.AUTHORIZATION, equalTo(AUTHORISATION_TOKEN))
            .willReturn(aResponse()
                .withStatus(HTTP_OK)
                .withBody(jsonMapper.toJson(citizenStartEventResponse)))
        );
    }

    protected void stubForSubmitEventForRepresentative(String caseId, String eventId) {
        final String URI = "/caseworkers/" + USER_ID + "/jurisdictions/" + JURISDICTION_ID
                         + "/case-types/" + CASE_TYPE_ID + "/cases/" + caseId
                         + "/events" + "?" + "ignore-warning=" + IGNORE_WARNING;

        stubFor(post(urlEqualTo(URI))
            .withHeader(HttpHeaders.CONTENT_TYPE, equalTo(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .withHeader(HttpHeaders.AUTHORIZATION, equalTo(SOLICITOR_AUTHORISATION_TOKEN))
            .withRequestBody(containing(eventId))
            .willReturn(aResponse()
                .withStatus(HTTP_OK)
                .withBody(jsonMapper.toJson(representativeSampleCaseDetails)))
        );
    }

    protected void stubForSubmitEventForCitizen(String caseId, String eventId) {
        final String URI = "/citizens/" + USER_ID + "/jurisdictions/" + JURISDICTION_ID
                         + "/case-types/" + CASE_TYPE_ID + "/cases/" + caseId
                         + "/events" + "?" + "ignore-warning=" + IGNORE_WARNING;

        stubFor(post(urlEqualTo(URI))
            .withHeader(HttpHeaders.CONTENT_TYPE, equalTo(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .withHeader(HttpHeaders.AUTHORIZATION, equalTo(AUTHORISATION_TOKEN))
            .withRequestBody(containing(eventId))
            .willReturn(aResponse()
                .withStatus(HTTP_OK)
                .withBody(jsonMapper.toJson(citizenSampleCaseDetails)))
        );
    }
}
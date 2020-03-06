package uk.gov.hmcts.cmc.claimstore.controllers;

import com.google.common.collect.ImmutableList;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import uk.gov.hmcts.cmc.ccd.sample.data.SampleCaseDetails;
import uk.gov.hmcts.cmc.ccd.sample.data.SampleStartEventResponse;
import uk.gov.hmcts.cmc.claimstore.BaseMockSpringTest;
import uk.gov.hmcts.cmc.claimstore.helper.JsonMappingHelper;
import uk.gov.hmcts.cmc.domain.models.ClaimData;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaimData;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;

import java.util.Collections;

import static com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder.okForJson;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static java.net.HttpURLConnection.HTTP_OK;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.CREATE_CASE;
import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.ISSUE_CASE;
import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.SEALED_CLAIM_UPLOAD;

@TestPropertySource(
    properties = {
        "core_case_data.api.url=http://localhost:${wiremock.server.port}"
    }
)
@AutoConfigureWireMock(port = 0)
public class SubmitClaimTest extends BaseMockSpringTest {

    protected static final String SOLICITOR_AUTHORISATION_TOKEN = "Solicitor Bearer token";

    @Autowired
    protected JsonMappingHelper jsonMappingHelper;
    @Autowired
    protected MockMvc webClient;

    private final CaseDetails representativeSampleCaseDetails =
        SampleCaseDetails.builder().buildRepresentativeCaseDetails();
    private final CaseDetails citizenSampleCaseDetails =
        SampleCaseDetails.builder().buildCitizenCaseDetails();
    private final StartEventResponse representativeStartEventResponse =
        SampleStartEventResponse.builder().buildRepresentativeStartEventResponse();
    private final StartEventResponse citizenStartEventResponse =
        SampleStartEventResponse.builder().buildCitizenStartEventResponse();

    @Test
    public void shouldSuccessfullySubmitClaimForCitizen() throws Exception {
        makeSuccessfulIssueClaimRequestForCitizen();
    }

    @Test
    public void shouldSuccessfullySubmitClaimForRepresentative() throws Exception {
        makeSuccessfulIssueClaimRequestForRepresentative();
    }

    protected MvcResult makeSuccessfulIssueClaimRequestForRepresentative() throws Exception {
        final ClaimData legalRepresentativeClaimData = SampleClaimData.submittedByLegalRepresentative();
        final String externalId = legalRepresentativeClaimData.getExternalId().toString();
        final String caseId = representativeSampleCaseDetails.getId().toString();

        stubForSearchForRepresentative(externalId);
        stubForStartForRepresentative();
        stubForSubmitForRepresentative(externalId);

        stubForStartEventForRepresentative(caseId, SEALED_CLAIM_UPLOAD.getValue());
        stubForSubmitEventForRepresentative(caseId,
            representativeStartEventResponse.getCaseDetails().getId().toString());
        stubForStartEventForRepresentative(caseId, ISSUE_CASE.getValue());

        given(authTokenGenerator.generate()).willReturn(SOLICITOR_AUTHORISATION_TOKEN);

        return makeIssueClaimRequest(legalRepresentativeClaimData, SOLICITOR_AUTHORISATION_TOKEN)
            .andExpect(status().isOk())
            .andReturn();
    }

    protected MvcResult makeSuccessfulIssueClaimRequestForCitizen() throws Exception {
        final ClaimData submittedByClaimant = SampleClaimData.submittedByClaimant();
        final String externalId = submittedByClaimant.getExternalId().toString();
        final String caseId = citizenSampleCaseDetails.getId().toString();

        stubForSearchForCitizen(externalId);
        stubForStartForCitizen();
        stubForSubmitForCitizen(externalId);

        stubForStartEventForCitizen(caseId, SEALED_CLAIM_UPLOAD.getValue());
        stubForSubmitEventForCitizen(caseId,
            citizenStartEventResponse.getCaseDetails().getId().toString());
        stubForStartEventForCitizen(caseId, ISSUE_CASE.getValue());

        given(authTokenGenerator.generate()).willReturn(AUTHORISATION_TOKEN);

        return makeIssueClaimRequest(submittedByClaimant, AUTHORISATION_TOKEN)
            .andExpect(status().isOk())
            .andReturn();
    }

    protected void stubForSearchForCitizen(String externalId) {
        final String URI = "/citizens/" + USER_ID + "/jurisdictions/" + JURISDICTION_ID
            + "/case-types/" + CASE_TYPE_ID + "/cases" + "?" + "case.externalId="
            + externalId + "&" + "sortDirection=desc" + "&" + "page=1";

        stubFor(get(urlEqualTo(URI))
            .withHeader(HttpHeaders.CONTENT_TYPE, equalTo(MediaType.APPLICATION_JSON_VALUE))
            .withHeader(HttpHeaders.AUTHORIZATION, equalTo(AUTHORISATION_TOKEN))
            .willReturn(okForJson(Collections.emptyList()))
        );
    }

    protected void stubForSearchForRepresentative(String externalId) {
        final String URI = "/caseworkers/" + USER_ID + "/jurisdictions/" + JURISDICTION_ID
            + "/case-types/" + CASE_TYPE_ID + "/cases" + "?" + "case.externalId="
            + externalId + "&" + "sortDirection=desc" + "&" + "page=1";

        stubFor(get(urlEqualTo(URI))
            .withHeader(HttpHeaders.CONTENT_TYPE, equalTo(MediaType.APPLICATION_JSON_VALUE))
            .withHeader(HttpHeaders.AUTHORIZATION, equalTo(SOLICITOR_AUTHORISATION_TOKEN))
            .willReturn(okForJson(Collections.emptyList()))
        );
    }

    protected void stubForStartForRepresentative() {
        final String URI = "/caseworkers/" + USER_ID + "/jurisdictions/" + JURISDICTION_ID
            + "/case-types/" + CASE_TYPE_ID + "/event-triggers/"
            + CREATE_CASE.getValue() + "/token";

        stubFor(get(urlEqualTo(URI))
            .withHeader(HttpHeaders.CONTENT_TYPE, equalTo(MediaType.APPLICATION_JSON_VALUE))
            .withHeader(HttpHeaders.AUTHORIZATION, equalTo(SOLICITOR_AUTHORISATION_TOKEN))
            .willReturn(aResponse()
                .withStatus(HTTP_OK)
                .withBody(jsonMappingHelper.toJson(representativeStartEventResponse)))
        );
    }

    protected void stubForStartForCitizen() {
        final String URI = "/citizens/" + USER_ID + "/jurisdictions/" + JURISDICTION_ID
            + "/case-types/" + CASE_TYPE_ID + "/event-triggers/"
            + CREATE_CASE.getValue() + "/token";

        stubFor(get(urlEqualTo(URI))
            .withHeader(HttpHeaders.CONTENT_TYPE, equalTo(MediaType.APPLICATION_JSON_VALUE))
            .withHeader(HttpHeaders.AUTHORIZATION, equalTo(AUTHORISATION_TOKEN))
            .willReturn(aResponse()
                .withStatus(HTTP_OK)
                .withBody(jsonMappingHelper.toJson(citizenStartEventResponse)))
        );
    }

    protected void stubForSubmitForRepresentative(String externalId) {
        final String URI = "/caseworkers/" + USER_ID + "/jurisdictions/" + JURISDICTION_ID
            + "/case-types/" + CASE_TYPE_ID + "/cases"
            + "?" + "ignore-warning=" + IGNORE_WARNING;

        stubFor(post(urlEqualTo(URI))
            .withHeader(HttpHeaders.CONTENT_TYPE, equalTo(MediaType.APPLICATION_JSON_VALUE))
            .withHeader(HttpHeaders.AUTHORIZATION, equalTo(SOLICITOR_AUTHORISATION_TOKEN))
            .withRequestBody(containing(externalId))
            .willReturn(aResponse()
                .withStatus(HTTP_OK)
                .withBody(jsonMappingHelper.toJson(representativeSampleCaseDetails)))
        );
    }

    protected void stubForSubmitForCitizen(String externalId) {
        final String URI = "/citizens/" + USER_ID + "/jurisdictions/" + JURISDICTION_ID
            + "/case-types/" + CASE_TYPE_ID + "/cases" + "?"
            + "ignore-warning=" + IGNORE_WARNING;

        stubFor(post(urlEqualTo(URI))
            .withHeader(HttpHeaders.CONTENT_TYPE, equalTo(MediaType.APPLICATION_JSON_VALUE))
            .withHeader(HttpHeaders.AUTHORIZATION, equalTo(AUTHORISATION_TOKEN))
            .withRequestBody(containing(externalId))
            .willReturn(aResponse()
                .withStatus(HTTP_OK)
                .withBody(jsonMappingHelper.toJson(citizenSampleCaseDetails)))
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
                .withBody(jsonMappingHelper.toJson(representativeStartEventResponse)))
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
                .withBody(jsonMappingHelper.toJson(citizenStartEventResponse)))
        );
    }

    protected void stubForSubmitEventForRepresentative(String caseId, String eventId) {
        final String URI = "/caseworkers/" + USER_ID + "/jurisdictions/" + JURISDICTION_ID
            + "/case-types/" + CASE_TYPE_ID + "/cases/" + caseId
            + "/events" + "?" + "ignore-warning=" + IGNORE_WARNING;

        stubFor(post(urlEqualTo(URI))
            .withHeader(HttpHeaders.CONTENT_TYPE, equalTo(MediaType.APPLICATION_JSON_VALUE))
            .withHeader(HttpHeaders.AUTHORIZATION, equalTo(SOLICITOR_AUTHORISATION_TOKEN))
            .withRequestBody(containing(eventId))
            .willReturn(aResponse()
                .withStatus(HTTP_OK)
                .withBody(jsonMappingHelper.toJson(representativeSampleCaseDetails)))
        );
    }

    protected void stubForSubmitEventForCitizen(String caseId, String eventId) {
        final String URI = "/citizens/" + USER_ID + "/jurisdictions/" + JURISDICTION_ID
            + "/case-types/" + CASE_TYPE_ID + "/cases/" + caseId
            + "/events" + "?" + "ignore-warning=" + IGNORE_WARNING;

        stubFor(post(urlEqualTo(URI))
            .withHeader(HttpHeaders.CONTENT_TYPE, equalTo(MediaType.APPLICATION_JSON_VALUE))
            .withHeader(HttpHeaders.AUTHORIZATION, equalTo(AUTHORISATION_TOKEN))
            .withRequestBody(containing(eventId))
            .willReturn(aResponse()
                .withStatus(HTTP_OK)
                .withBody(jsonMappingHelper.toJson(citizenSampleCaseDetails)))
        );
    }

    protected ResultActions makeIssueClaimRequest(ClaimData claimData, String authorization) throws Exception {
        return webClient
            .perform(MockMvcRequestBuilders.post("/claims/" + USER_ID)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .header(HttpHeaders.AUTHORIZATION, authorization)
                .header("Features", ImmutableList.of("admissions"))
                .content(jsonMappingHelper.toJson(claimData))
            );
    }
}

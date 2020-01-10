package uk.gov.hmcts.cmc.claimstore.controllers;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.google.common.collect.ImmutableList;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import uk.gov.hmcts.cmc.claimstore.BaseMockSpringTest;
import uk.gov.hmcts.cmc.claimstore.deprecated.PostClaimOperation;
import uk.gov.hmcts.cmc.claimstore.services.staff.BulkPrintStaffNotificationService;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.ClaimData;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaimData;
import uk.gov.hmcts.cmc.email.EmailService;

import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@TestPropertySource(
    properties = {
        "core_case_data.api.url=false",
        "send-letter.url=http://localhost:${wiremock.server.port}"
    }
)
@AutoConfigureWireMock(port = 0)
@ActiveProfiles("test")
public class BulkPrintRequestTest extends BaseMockSpringTest {

    @Autowired
    private WireMockServer wireMockServer;
    @Autowired
    protected PostClaimOperation postClaimOperation;

    @MockBean
    private BulkPrintStaffNotificationService bulkPrintNotificationService;
    @MockBean
    protected EmailService emailService;

    @Test
    public void shouldNotSendNotificationWhenEverythingIsOk() throws Exception {
        when(authTokenGenerator.generate()).thenReturn(AUTHORISATION_TOKEN);

        wireMockServer.stubFor(post(urlEqualTo("/letters"))
            .willReturn(aResponse()
                .withStatus(HttpStatus.OK.value())
                .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .withBody("{ \"letter_id\":\"" + UUID.randomUUID().toString() + "\" }")
            )
        );

        MvcResult result = makeIssueClaimRequest(SampleClaimData.submittedByClaimant(), AUTHORISATION_TOKEN)
            .andExpect(status().isOk())
            .andReturn();

        Claim savedClaim = jsonMappingHelper.deserializeObjectFrom(result, Claim.class);

        postClaimOperation.getClaim(savedClaim.getExternalId(), AUTHORISATION_TOKEN);

        verify(bulkPrintNotificationService, never())
            .notifyFailedBulkPrint(
                anyList(),
                eq(jsonMappingHelper.deserializeObjectFrom(result, Claim.class)));
    }

    @Test
    public void shouldSendNotificationWhenBulkPrintFailsWithHttpClientError() throws Exception {
        when(authTokenGenerator.generate()).thenReturn(AUTHORISATION_TOKEN);

        wireMockServer.stubFor(post(urlEqualTo("/letters"))
            .willReturn(aResponse()
                .withStatus(HttpStatus.BAD_REQUEST.value())
                .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            )
        );

        makeIssueClaimRequest(SampleClaimData.submittedByClaimant(), AUTHORISATION_TOKEN)
            .andExpect(status().isOk())
            .andReturn();

        verify(bulkPrintNotificationService)
            .notifyFailedBulkPrint(
                anyList(),
                any(Claim.class));
    }

    @Test
    public void shouldSendNotificationWhenBulkPrintFailsWithHttpServerError() throws Exception {

        when(authTokenGenerator.generate()).thenReturn(AUTHORISATION_TOKEN);

        wireMockServer.stubFor(post(urlEqualTo("/letters"))
            .willReturn(aResponse()
                .withStatus(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .withBody("Internal server error occurred")));

        makeIssueClaimRequest(SampleClaimData.submittedByClaimant(), AUTHORISATION_TOKEN)
            .andExpect(status().isOk())
            .andReturn();

        verify(bulkPrintNotificationService)
            .notifyFailedBulkPrint(
                anyList(),
                any(Claim.class));
    }

    private ResultActions makeIssueClaimRequest(ClaimData claimData, String authorization) throws Exception {
        return webClient
            .perform(MockMvcRequestBuilders.post("/claims/" + USER_ID)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .header(HttpHeaders.AUTHORIZATION, authorization)
                .header("Features", ImmutableList.of("admissions"))
                .content(jsonMappingHelper.toJson(claimData))
            );
    }

}

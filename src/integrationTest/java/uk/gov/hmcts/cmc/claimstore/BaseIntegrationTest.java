package uk.gov.hmcts.cmc.claimstore;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;
import uk.gov.hmcts.cmc.claimstore.deprecated.PostClaimOperation;
import uk.gov.hmcts.cmc.domain.models.ClaimData;
import uk.gov.hmcts.cmc.email.EmailService;
import uk.gov.hmcts.reform.document.DocumentUploadClientApi;
import uk.gov.hmcts.reform.sendletter.api.SendLetterApi;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

public abstract class BaseIntegrationTest extends BaseMockSpringTest {

    @MockBean
    protected EmailService emailService;
    @MockBean
    protected SendLetterApi sendLetterApi;
    @MockBean
    protected DocumentUploadClientApi documentUploadClient;

    @Autowired
    protected PostClaimOperation postClaimOperation;


    protected ResultActions makeIssueClaimRequest(ClaimData claimData, String authorization) throws Exception {
        return webClient
            .perform(post("/claims/" + USER_ID)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_UTF8_VALUE)
                .header(HttpHeaders.AUTHORIZATION, authorization)
                .header("Features", ImmutableList.of("admissions"))
                .content(jsonMappingHelper.toJson(claimData))
            );
    }

    protected ImmutableMap<String, String> searchCriteria(String externalId) {
        return ImmutableMap.of(
            "page", "1",
            "sortDirection", "desc",
            "case.externalId", externalId
        );
    }

    protected ResultActions makeGetRequest(String urlTemplate) throws Exception {
        return webClient.perform(
            get(urlTemplate)
                .header(HttpHeaders.AUTHORIZATION, AUTHORISATION_TOKEN)
        );
    }
}

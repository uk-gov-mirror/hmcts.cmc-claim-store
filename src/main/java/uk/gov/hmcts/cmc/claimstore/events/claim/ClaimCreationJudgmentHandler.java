package uk.gov.hmcts.cmc.claimstore.events.claim;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.claimstore.camunda.CamundaApi;
import uk.gov.hmcts.cmc.claimstore.camunda.models.CaseDetail;
import uk.gov.hmcts.cmc.claimstore.stereotypes.LogExecutionTime;

@Async("threadPoolTaskExecutor")
@Service
@ConditionalOnProperty(prefix = "feature_toggles", name = "async_event_operations_enabled", havingValue = "true")
public class ClaimCreationJudgmentHandler {
    private static final Logger logger = LoggerFactory.getLogger(ClaimCreationJudgmentHandler.class);

    private final CamundaApi camundaApi;

    @Autowired
    @SuppressWarnings("squid:S00107")
    public ClaimCreationJudgmentHandler(
        CamundaApi camundaApi
    ) {
        this.camundaApi = camundaApi;

    }

    @LogExecutionTime
    @EventListener
    public void citizenIssueHandler(CitizenClaimCreatedEvent event) {
        try {
            CaseDetail caseDetail = CaseDetail.builder().caseId(event.getClaim().getCcdCaseId()).build();
            camundaApi.createJudgmentProcess(caseDetail);
        } catch (Exception e) {
            logger.error("Failed to create judgment process processing for event ()", event, e);
        }
    }
}

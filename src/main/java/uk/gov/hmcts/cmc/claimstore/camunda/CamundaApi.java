package uk.gov.hmcts.cmc.claimstore.camunda;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import uk.gov.hmcts.cmc.claimstore.camunda.models.CaseDetail;

@FeignClient(name = "camunda-api", url = "${camunda.api.url}")
public interface CamundaApi {

    String CREATE_CAMUNDA_PROCESS_URL = "process/judgment";

    @RequestMapping(method = RequestMethod.POST, value = CREATE_CAMUNDA_PROCESS_URL)
    void createJudgmentProcess(@RequestBody CaseDetail caseDetail);

}

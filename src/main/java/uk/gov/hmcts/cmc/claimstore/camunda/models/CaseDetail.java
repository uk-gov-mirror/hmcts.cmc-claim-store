package uk.gov.hmcts.cmc.claimstore.camunda.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.Data;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Builder
public class CaseDetail {
    private Long caseId;
}

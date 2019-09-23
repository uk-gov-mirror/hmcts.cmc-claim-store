package uk.gov.hmcts.cmc.domain.models;

import lombok.AllArgsConstructor;
import lombok.Getter;

import javax.validation.constraints.NotEmpty;

@AllArgsConstructor
@Getter
public class JudgmentRequest {

    @NotEmpty
    private Long caseId;

    @NotEmpty
    private String jusgmentProcessId;

}

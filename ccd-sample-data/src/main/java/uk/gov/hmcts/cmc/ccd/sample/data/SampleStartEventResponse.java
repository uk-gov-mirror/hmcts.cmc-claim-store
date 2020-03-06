package uk.gov.hmcts.cmc.ccd.sample.data;

import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;

public class SampleStartEventResponse {

    private String eventId = "1516189555935242";
    private String token = "Bearer token here";

    private StartEventResponse.StartEventResponseBuilder startEventResponseBuilder = StartEventResponse.builder()
        .eventId(eventId)
        .token(token);

    private SampleStartEventResponse() {
        //Utility class
    }

    public static SampleStartEventResponse builder() {
        return new SampleStartEventResponse();
    }

    public StartEventResponse buildRepresentativeStartEventResponse() {
        return startEventResponseBuilder.caseDetails(SampleCaseDetails
            .builder()
            .buildRepresentativeCaseDetails())
            .build();
    }

    public StartEventResponse buildCitizenStartEventResponse() {
        return startEventResponseBuilder.caseDetails(SampleCaseDetails
            .builder()
            .buildCitizenCaseDetails())
            .build();
    }
}

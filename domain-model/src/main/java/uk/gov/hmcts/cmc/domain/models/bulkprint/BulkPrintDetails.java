package uk.gov.hmcts.cmc.domain.models.bulkprint;

import lombok.Builder;
import lombok.Value;

import java.time.LocalDate;

@Builder
@Value
public class BulkPrintDetails {
    private String bulkPrintLetterId;
    private BulkPrintLetterType bulkPrintLetterType;
    private LocalDate printingDate;
}

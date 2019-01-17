package uk.gov.hmcts.cmc.domain.models;

import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.hibernate.validator.constraints.NotBlank;
import uk.gov.hmcts.ccd.definition.FieldLabel;

import javax.validation.constraints.Size;

import static uk.gov.hmcts.cmc.domain.utils.ToStringStyle.ourStyle;

@EqualsAndHashCode

@FieldLabel("Event")
public class TimelineEvent {

    @NotBlank
    @Size(max = 20)
    @FieldLabel("Date")
    private final String date;

    @NotBlank
    @Size(max = 99000)
    @FieldLabel("Description")
    private final String description;

    public TimelineEvent(String eventDate, String description) {
        this.date = eventDate;
        this.description = description;
    }

    public String getDate() {
        return date;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ourStyle());
    }

}

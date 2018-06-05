package uk.gov.hmcts.cmc.domain.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.hibernate.validator.constraints.NotBlank;
import uk.gov.hmcts.ccd.definition.FieldLabel;

import java.util.Objects;
import javax.validation.constraints.Size;

import static uk.gov.hmcts.cmc.domain.utils.ToStringStyle.ourStyle;


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

    @JsonCreator
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
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        TimelineEvent that = (TimelineEvent) other;

        return Objects.equals(date, that.date)
            && Objects.equals(description, that.description);
    }

    @Override
    public int hashCode() {
        return Objects.hash(date, description);
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ourStyle());
    }

}

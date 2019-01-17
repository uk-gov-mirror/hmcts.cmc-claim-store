package uk.gov.hmcts.cmc.domain.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.hibernate.validator.constraints.NotBlank;
import uk.gov.hmcts.ccd.definition.FieldLabel;

import java.math.BigDecimal;
import javax.validation.constraints.NotNull;

import static uk.gov.hmcts.cmc.domain.utils.ToStringStyle.ourStyle;

@Builder
@EqualsAndHashCode
@JsonIgnoreProperties(value = {"description", "state"})
@FieldLabel("Payment")
public class Payment {
    @FieldLabel(value = "id")
    private final String id;
    /**
     * The amount which was paid, in pennies for payments v1 or pounds with payments v2.
     */
    @NotNull
    @FieldLabel(value = "Amount")
    private final BigDecimal amount;
    @NotBlank
    @FieldLabel(value = "Reference")
    private final String reference;
    @JsonProperty("date_created")
    @FieldLabel("Date Created")
    private final String dateCreated;

    @FieldLabel(value = "Status")
    private final String status;

    public Payment(
        String id,
        BigDecimal amount,
        String reference,
        String dateCreated,
        String status
    ) {
        this.id = id;
        this.amount = amount;
        this.reference = reference;
        this.dateCreated = dateCreated;
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getReference() {
        return reference;
    }

    public String getStatus() {
        return status;
    }

    public String getDateCreated() {
        return dateCreated;
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ourStyle());
    }
}

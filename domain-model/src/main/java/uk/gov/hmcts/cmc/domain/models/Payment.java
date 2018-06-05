package uk.gov.hmcts.cmc.domain.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.hibernate.validator.constraints.NotBlank;
import uk.gov.hmcts.ccd.definition.FieldLabel;

import java.math.BigDecimal;
import java.util.Objects;
import javax.validation.constraints.NotNull;

import static uk.gov.hmcts.cmc.domain.utils.ToStringStyle.ourStyle;

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
    @SuppressWarnings("squid:S1067") // Its generated code for equals sonar
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        Payment payment = (Payment) obj;
        return Objects.equals(id, payment.id)
            && Objects.equals(amount, payment.amount)
            && Objects.equals(reference, payment.reference)
            && Objects.equals(dateCreated, payment.dateCreated)
            && Objects.equals(status, payment.status);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, amount, reference, dateCreated, status);
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ourStyle());
    }
}

package uk.gov.hmcts.cmc.claimstore.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import uk.gov.hmcts.cmc.claimstore.models.ccj.PaymentOption;
import uk.gov.hmcts.cmc.claimstore.models.ccj.RepaymentPlan;
import uk.gov.hmcts.cmc.claimstore.models.otherparty.TheirDetails;

import java.time.LocalDate;
import java.util.Objects;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import static uk.gov.hmcts.cmc.claimstore.utils.ToStringStyle.ourStyle;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_ABSENT)
public class CountyCourtJudgment {

    @Valid
    @NotNull
    private final TheirDetails defendant;

    private Double paidAmount;

    @Valid
    @NotNull
    private final PaymentOption paymentOption;

    @Valid
    private final RepaymentPlan repaymentPlan;

    @Valid
    private final LocalDate payBySetDate;

    public CountyCourtJudgment(
        TheirDetails defendant,
        PaymentOption paymentOption,
        Double paidAmount,
        RepaymentPlan repaymentPlan,
        LocalDate payBySetDate) {
        this.defendant = defendant;
        this.paymentOption = paymentOption;
        this.paidAmount = paidAmount;
        this.repaymentPlan = repaymentPlan;
        this.payBySetDate = payBySetDate;
    }

    public TheirDetails getDefendant() {
        return defendant;
    }

    public PaymentOption getPaymentOption() {
        return paymentOption;
    }

    public Double getPaidAmount() {
        return paidAmount;
    }

    public RepaymentPlan getRepaymentPlan() {
        return repaymentPlan;
    }

    public LocalDate getPayBySetDate() {
        return payBySetDate;
    }

    @Override
    @SuppressWarnings("squid:S1067") // Its generated code for equals sonar
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        CountyCourtJudgment that = (CountyCourtJudgment) other;
        return Objects.equals(defendant, that.defendant)
            && Objects.equals(paymentOption, that.paymentOption)
            && Objects.equals(paidAmount, that.paidAmount)
            && Objects.equals(repaymentPlan, that.repaymentPlan)
            && Objects.equals(payBySetDate, that.payBySetDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(defendant, paymentOption);
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ourStyle());
    }
}

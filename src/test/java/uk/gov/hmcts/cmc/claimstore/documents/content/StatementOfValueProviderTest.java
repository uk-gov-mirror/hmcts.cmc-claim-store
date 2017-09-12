package uk.gov.hmcts.cmc.claimstore.documents.content;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.cmc.claimstore.controllers.utils.sampledata.SampleClaim;
import uk.gov.hmcts.cmc.claimstore.controllers.utils.sampledata.SampleClaimData;
import uk.gov.hmcts.cmc.claimstore.documents.content.models.StatementOfValueContent;
import uk.gov.hmcts.cmc.claimstore.models.Claim;
import uk.gov.hmcts.cmc.claimstore.models.amount.NotKnown;
import uk.gov.hmcts.cmc.claimstore.models.particulars.PersonalInjury;
import uk.gov.hmcts.cmc.claimstore.models.sampledata.SampleAmountRange;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.cmc.claimstore.documents.content.StatementOfValueProvider.ALSO_HOUSING_DISREPAIR;
import static uk.gov.hmcts.cmc.claimstore.documents.content.StatementOfValueProvider.CAN_NOT_STATE;
import static uk.gov.hmcts.cmc.claimstore.documents.content.StatementOfValueProvider.HOUSING_DISREPAIR;
import static uk.gov.hmcts.cmc.claimstore.documents.content.StatementOfValueProvider.PERSONAL_INJURY;
import static uk.gov.hmcts.cmc.claimstore.documents.content.StatementOfValueProvider.PERSONAL_INJURY_DAMAGES;
import static uk.gov.hmcts.cmc.claimstore.models.particulars.DamagesExpectation.MORE_THAN_THOUSAND_POUNDS;

@RunWith(MockitoJUnitRunner.class)
public class StatementOfValueProviderTest {

    @Test
    public void shouldCreateContentWithAmountRange() throws Exception {
        //given
        final Claim claim = SampleClaim.claim(SampleClaimData.builder()
            .withAmount(SampleAmountRange.builder().withHigherValue(BigDecimal.valueOf(100.50))
                .withLowerValue(BigDecimal.valueOf(200.95)).build())
            .build(), "000LR001");

        final StatementOfValueProvider statementOfValueProvider = new StatementOfValueProvider();

        //when
        final StatementOfValueContent statementOfValueContent = statementOfValueProvider.create(claim);

        //then
        assertThat(statementOfValueContent.getClaimValue()).contains("100.50");
        assertThat(statementOfValueContent.getClaimValue()).contains("200.95");
    }

    @Test
    public void shouldCreateContentWithAmountNotKnown() throws Exception {
        //given
        final Claim claim = SampleClaim.claim(SampleClaimData.builder()
            .withAmount(new NotKnown()).build(), "000LR001");

        final StatementOfValueProvider statementOfValueProvider = new StatementOfValueProvider();

        //when
        final StatementOfValueContent statementOfValueContent = statementOfValueProvider.create(claim);

        //then
        assertThat(statementOfValueContent.getClaimValue()).contains(CAN_NOT_STATE);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowErrorForInvalidAmountType() throws Exception {
        //given
        final Claim claim = SampleClaim.claim(SampleClaimData.builder()
            .build(), "000LR001");

        final StatementOfValueProvider statementOfValueProvider = new StatementOfValueProvider();

        //when
        final StatementOfValueContent statementOfValueContent = statementOfValueProvider.create(claim);

    }

    @Test
    public void shouldCreateContentWithPersonalInjury() throws Exception {
        //given
        final Claim claim = SampleClaim.claim(SampleClaimData.builder()
            .withAmount(SampleAmountRange.builder().withHigherValue(BigDecimal.valueOf(100.50))
                .withLowerValue(BigDecimal.valueOf(200.95)).build())
            .withPersonalInjury(new PersonalInjury(MORE_THAN_THOUSAND_POUNDS))
            .withHousingDisrepair(null)
            .build(), "000LR001");
        final String expected = String.format(PERSONAL_INJURY_DAMAGES, MORE_THAN_THOUSAND_POUNDS.getDisplayValue());

        final StatementOfValueProvider statementOfValueProvider = new StatementOfValueProvider();

        //when
        final StatementOfValueContent statementOfValueContent = statementOfValueProvider.create(claim);

        //then
        assertThat(statementOfValueContent.getPersonalInjury()).contains(PERSONAL_INJURY);
        assertThat(statementOfValueContent.getPersonalInjury()).contains(expected);
    }

    @Test
    public void shouldCreateContentWithHousingDisrepair() throws Exception {
        //given
        final Claim claim = SampleClaim.claim(SampleClaimData.builder()
            .withPersonalInjury(null)
            .withAmount(SampleAmountRange.builder().withHigherValue(BigDecimal.valueOf(100.50))
                .withLowerValue(BigDecimal.valueOf(200.95)).build())
            .build(), "000LR001");

        final StatementOfValueProvider statementOfValueProvider = new StatementOfValueProvider();

        //when
        final StatementOfValueContent statementOfValueContent = statementOfValueProvider.create(claim);

        //then
        assertThat(statementOfValueContent.getHousingDisrepair()).contains(HOUSING_DISREPAIR);
    }

    @Test
    public void shouldCreateContentWithHousingDisrepairAndPersonalInjury() throws Exception {
        //given
        final Claim claim = SampleClaim.claim(SampleClaimData.builder()
            .withAmount(SampleAmountRange.builder().withHigherValue(BigDecimal.valueOf(100.50))
                .withLowerValue(BigDecimal.valueOf(200.95)).build())
            .build(), "000LR001");

        final StatementOfValueProvider statementOfValueProvider = new StatementOfValueProvider();

        //when
        final StatementOfValueContent statementOfValueContent = statementOfValueProvider.create(claim);

        //then
        assertThat(statementOfValueContent.getHousingDisrepair()).contains(ALSO_HOUSING_DISREPAIR);
    }

}

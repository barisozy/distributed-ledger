package com.distributed.ledger.domain.model;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.assertj.core.api.Assertions.*;

class MoneyTest {

    @Test
    void shouldAddMoneyWithSameCurrency() {
        Money m1 = Money.of(new BigDecimal("100.00"), "USD");
        Money m2 = Money.of(new BigDecimal("50.00"), "USD");

        Money result = m1.add(m2);

        assertThat(result.getAmount()).isEqualByComparingTo("150.00");
        assertThat(result.getCurrencyCode()).isEqualTo("USD");
    }

    @Test
    void shouldThrowExceptionWhenCurrenciesMismatch() {
        Money usd = Money.of(BigDecimal.TEN, "USD");
        Money eur = Money.of(BigDecimal.TEN, "EUR");

        assertThatThrownBy(() -> usd.add(eur))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("different currencies");
    }

    @Test
    void shouldCalculateSubstractionCorrectly() {
        Money m1 = Money.of(new BigDecimal("100.00"), "TRY");
        Money m2 = Money.of(new BigDecimal("30.00"), "TRY");

        assertThat(m1.subtract(m2).getAmount()).isEqualByComparingTo("70.00");
    }
}
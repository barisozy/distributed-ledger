package com.distributed.ledger.domain.model;

import com.distributed.ledger.domain.exception.DomainException;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.assertj.core.api.Assertions.*;

class AccountTest {

    @Test
    void shouldWithdrawMoneyIdeally() {
        Account account = Account.create("TR01", "Alice", Money.of(new BigDecimal("100"), "USD"));

        account.withdraw(Money.of(new BigDecimal("40"), "USD"));

        assertThat(account.getBalance().getAmount()).isEqualByComparingTo("60");
    }

    @Test
    void shouldFailWithdrawIfInsufficientFunds() {
        Account account = Account.create("TR01", "Alice", Money.of(new BigDecimal("50"), "USD"));

        assertThatThrownBy(() -> account.withdraw(Money.of(new BigDecimal("100"), "USD")))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("Insufficient funds");
    }

    @Test
    void shouldFailIfAccountFrozen() {
        Account account = Account.with(
                AccountId.generate(), "Alice", "TR01",
                Money.of(BigDecimal.TEN, "USD"), AccountStatus.FROZEN, 1L
        );

        assertThatThrownBy(() -> account.deposit(Money.of(BigDecimal.ONE, "USD")))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("not active");
    }
}
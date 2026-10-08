package dev.arsenii.ledger.account;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AccountTest {

	@Test
	void withdrawReducesBalance() {
		Account account = new Account("Anna Virtanen", "EUR");
		account.deposit(new BigDecimal("100.00"));

		account.withdraw(new BigDecimal("30.50"));

		assertThat(account.getBalance()).isEqualByComparingTo("69.50");
	}

	@Test
	void canWithdrawWholeBalance() {
		Account account = new Account("Anna Virtanen", "EUR");
		account.deposit(new BigDecimal("25.00"));

		account.withdraw(new BigDecimal("25.00"));

		assertThat(account.getBalance()).isEqualByComparingTo("0");
	}

	@Test
	void withdrawingMoreThanBalanceThrows() {
		Account account = new Account("Anna Virtanen", "EUR");
		account.deposit(new BigDecimal("10.00"));

		assertThatThrownBy(() -> account.withdraw(new BigDecimal("10.01")))
				.isInstanceOf(InsufficientFundsException.class);
		assertThat(account.getBalance()).isEqualByComparingTo("10.00");
	}

}

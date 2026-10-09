package dev.arsenii.ledger.statement;

import dev.arsenii.ledger.account.Account;
import dev.arsenii.ledger.account.AccountNotFoundException;
import dev.arsenii.ledger.account.AccountRepository;
import dev.arsenii.ledger.ledger.EntryType;
import dev.arsenii.ledger.ledger.LedgerEntry;
import dev.arsenii.ledger.ledger.LedgerEntryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StatementServiceTest {

	private static final UUID ACCOUNT_ID = UUID.fromString("7f3c2a1e-5b4d-4c6e-9a8b-1d2e3f4a5b6c");

	@Mock
	private AccountRepository accountRepository;

	@Mock
	private LedgerEntryRepository ledgerEntryRepository;

	@InjectMocks
	private StatementService statementService;

	@Test
	void balancesCoverWholePeriodNotJustThePage() {
		Instant start = Instant.parse("2026-10-01T00:00:00Z");
		Instant end = Instant.parse("2026-11-01T00:00:00Z");
		PageRequest firstPage = PageRequest.of(0, 1);
		when(accountRepository.findById(ACCOUNT_ID)).thenReturn(Optional.of(new Account("Anna Virtanen", "EUR")));
		when(ledgerEntryRepository.sumAmountsBefore(ACCOUNT_ID, start)).thenReturn(new BigDecimal("100.00"));
		when(ledgerEntryRepository.sumAmountsBefore(ACCOUNT_ID, end)).thenReturn(new BigDecimal("119.75"));
		when(ledgerEntryRepository.findForPeriod(ACCOUNT_ID, start, end, firstPage)).thenReturn(new PageImpl<>(
				List.of(new LedgerEntry(ACCOUNT_ID, EntryType.DEPOSIT, new BigDecimal("50.00"))), firstPage, 2));

		Statement statement = statementService.getStatement(ACCOUNT_ID, LocalDate.of(2026, 10, 1),
				LocalDate.of(2026, 10, 31), 0, 1);

		assertThat(statement.currency()).isEqualTo("EUR");
		assertThat(statement.openingBalance()).isEqualByComparingTo("100.00");
		assertThat(statement.closingBalance()).isEqualByComparingTo("119.75");
		assertThat(statement.entries().getContent()).hasSize(1);
		assertThat(statement.entries().getTotalElements()).isEqualTo(2);
	}

	@Test
	void throwsForUnknownAccount() {
		when(accountRepository.findById(ACCOUNT_ID)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> statementService.getStatement(ACCOUNT_ID, LocalDate.of(2026, 10, 1),
				LocalDate.of(2026, 10, 31), 0, 50))
				.isInstanceOf(AccountNotFoundException.class);
	}

}

package dev.arsenii.ledger.statement;

import dev.arsenii.ledger.account.Account;
import dev.arsenii.ledger.account.AccountNotFoundException;
import dev.arsenii.ledger.account.AccountRepository;
import dev.arsenii.ledger.ledger.LedgerEntry;
import dev.arsenii.ledger.ledger.LedgerEntryRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.UUID;

@Service
public class StatementService {

	private final AccountRepository accountRepository;
	private final LedgerEntryRepository ledgerEntryRepository;

	public StatementService(AccountRepository accountRepository, LedgerEntryRepository ledgerEntryRepository) {
		this.accountRepository = accountRepository;
		this.ledgerEntryRepository = ledgerEntryRepository;
	}

	@Transactional(readOnly = true, isolation = Isolation.REPEATABLE_READ)
	public Statement getStatement(UUID accountId, LocalDate from, LocalDate to, int page, int size) {
		Account account = accountRepository.findById(accountId)
				.orElseThrow(() -> new AccountNotFoundException(accountId));

		Instant start = from.atStartOfDay(ZoneOffset.UTC).toInstant();
		Instant end = to.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant();

		BigDecimal openingBalance = ledgerEntryRepository.sumAmountsBefore(accountId, start).setScale(2);
		BigDecimal closingBalance = ledgerEntryRepository.sumAmountsBefore(accountId, end).setScale(2);
		Page<LedgerEntry> entries = ledgerEntryRepository.findForPeriod(accountId, start, end, PageRequest.of(page, size));

		return new Statement(accountId, account.getCurrency(), from, to, openingBalance, closingBalance, entries);
	}

}

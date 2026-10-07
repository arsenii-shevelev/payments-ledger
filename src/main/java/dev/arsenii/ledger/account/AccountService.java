package dev.arsenii.ledger.account;

import dev.arsenii.ledger.ledger.EntryType;
import dev.arsenii.ledger.ledger.LedgerEntry;
import dev.arsenii.ledger.ledger.LedgerEntryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class AccountService {

	private final AccountRepository accountRepository;
	private final LedgerEntryRepository ledgerEntryRepository;

	public AccountService(AccountRepository accountRepository, LedgerEntryRepository ledgerEntryRepository) {
		this.accountRepository = accountRepository;
		this.ledgerEntryRepository = ledgerEntryRepository;
	}

	@Transactional
	public Account openAccount(String ownerName, String currency) {
		return accountRepository.save(new Account(ownerName, currency));
	}

	@Transactional(readOnly = true)
	public Account getAccount(UUID id) {
		return accountRepository.findById(id)
				.orElseThrow(() -> new AccountNotFoundException(id));
	}

	@Transactional
	public Account deposit(UUID accountId, BigDecimal amount) {
		Account account = accountRepository.findByIdForUpdate(accountId)
				.orElseThrow(() -> new AccountNotFoundException(accountId));
		account.deposit(amount);
		ledgerEntryRepository.save(new LedgerEntry(account.getId(), EntryType.DEPOSIT, amount));
		return account;
	}

}

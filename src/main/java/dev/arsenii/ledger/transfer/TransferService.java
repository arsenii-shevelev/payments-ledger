package dev.arsenii.ledger.transfer;

import dev.arsenii.ledger.account.Account;
import dev.arsenii.ledger.account.AccountNotFoundException;
import dev.arsenii.ledger.account.AccountRepository;
import dev.arsenii.ledger.ledger.EntryType;
import dev.arsenii.ledger.ledger.LedgerEntry;
import dev.arsenii.ledger.ledger.LedgerEntryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

@Service
public class TransferService {

	private final AccountRepository accountRepository;
	private final TransferRepository transferRepository;
	private final LedgerEntryRepository ledgerEntryRepository;

	public TransferService(AccountRepository accountRepository, TransferRepository transferRepository,
			LedgerEntryRepository ledgerEntryRepository) {
		this.accountRepository = accountRepository;
		this.transferRepository = transferRepository;
		this.ledgerEntryRepository = ledgerEntryRepository;
	}

	@Transactional
	public Transfer transfer(UUID fromAccountId, UUID toAccountId, BigDecimal amount, String idempotencyKey) {
		if (fromAccountId.equals(toAccountId)) {
			throw new InvalidTransferException("Cannot transfer money to the same account");
		}

		Account from;
		Account to;
		if (fromAccountId.compareTo(toAccountId) < 0) {
			from = lockAccount(fromAccountId);
			to = lockAccount(toAccountId);
		} else {
			to = lockAccount(toAccountId);
			from = lockAccount(fromAccountId);
		}

		if (idempotencyKey != null) {
			Optional<Transfer> existing =
					transferRepository.findByFromAccountIdAndIdempotencyKey(fromAccountId, idempotencyKey);
			if (existing.isPresent()) {
				if (!existing.get().isSameRequest(fromAccountId, toAccountId, amount)) {
					throw new InvalidTransferException("Idempotency key was already used for a different transfer");
				}
				return existing.get();
			}
		}

		if (!from.getCurrency().equals(to.getCurrency())) {
			throw new InvalidTransferException("Accounts have different currencies");
		}

		from.withdraw(amount);
		to.deposit(amount);

		Transfer transfer = transferRepository.save(new Transfer(fromAccountId, toAccountId, amount, idempotencyKey));
		ledgerEntryRepository.save(
				new LedgerEntry(fromAccountId, EntryType.TRANSFER_OUT, amount.negate(), transfer.getId()));
		ledgerEntryRepository.save(
				new LedgerEntry(toAccountId, EntryType.TRANSFER_IN, amount, transfer.getId()));
		return transfer;
	}

	@Transactional(readOnly = true)
	public Transfer getTransfer(UUID id) {
		return transferRepository.findById(id)
				.orElseThrow(() -> new TransferNotFoundException(id));
	}

	private Account lockAccount(UUID id) {
		return accountRepository.findByIdForUpdate(id)
				.orElseThrow(() -> new AccountNotFoundException(id));
	}

}

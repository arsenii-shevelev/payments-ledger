package dev.arsenii.ledger.account;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class AccountService {

	private final AccountRepository accountRepository;

	public AccountService(AccountRepository accountRepository) {
		this.accountRepository = accountRepository;
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

}

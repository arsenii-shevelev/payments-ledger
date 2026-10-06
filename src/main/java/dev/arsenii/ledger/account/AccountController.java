package dev.arsenii.ledger.account;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/accounts")
public class AccountController {

	private final AccountService accountService;

	public AccountController(AccountService accountService) {
		this.accountService = accountService;
	}

	@PostMapping
	public ResponseEntity<AccountResponse> openAccount(@Valid @RequestBody OpenAccountRequest request) {
		Account account = accountService.openAccount(request.ownerName(), request.currency());
		URI location = URI.create("/accounts/" + account.getId());
		return ResponseEntity.created(location).body(AccountResponse.from(account));
	}

	@GetMapping("/{id}")
	public AccountResponse getAccount(@PathVariable UUID id) {
		return AccountResponse.from(accountService.getAccount(id));
	}

}

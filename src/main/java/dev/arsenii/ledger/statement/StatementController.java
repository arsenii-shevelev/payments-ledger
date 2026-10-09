package dev.arsenii.ledger.statement;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.UUID;

@RestController
public class StatementController {

	private final StatementService statementService;

	public StatementController(StatementService statementService) {
		this.statementService = statementService;
	}

	@GetMapping("/accounts/{id}/statement")
	public StatementResponse getStatement(@PathVariable UUID id, @RequestParam LocalDate from,
			@RequestParam LocalDate to) {
		if (from.isAfter(to)) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "'from' must not be after 'to'");
		}
		return StatementResponse.from(statementService.getStatement(id, from, to));
	}

}

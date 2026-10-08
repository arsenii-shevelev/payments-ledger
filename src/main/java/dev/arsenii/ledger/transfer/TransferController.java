package dev.arsenii.ledger.transfer;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/transfers")
public class TransferController {

	private final TransferService transferService;

	public TransferController(TransferService transferService) {
		this.transferService = transferService;
	}

	@PostMapping
	public ResponseEntity<TransferResponse> transfer(
			@RequestHeader(name = "Idempotency-Key", required = false) @Size(min = 1, max = 100) String idempotencyKey,
			@Valid @RequestBody TransferRequest request) {
		Transfer transfer = transferService.transfer(request.fromAccountId(), request.toAccountId(), request.amount(),
				idempotencyKey);
		URI location = URI.create("/transfers/" + transfer.getId());
		return ResponseEntity.created(location).body(TransferResponse.from(transfer));
	}

	@GetMapping("/{id}")
	public TransferResponse getTransfer(@PathVariable UUID id) {
		return TransferResponse.from(transferService.getTransfer(id));
	}

}

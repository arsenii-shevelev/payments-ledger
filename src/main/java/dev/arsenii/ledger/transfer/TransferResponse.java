package dev.arsenii.ledger.transfer;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record TransferResponse(UUID id, UUID fromAccountId, UUID toAccountId, BigDecimal amount, Instant createdAt) {

	static TransferResponse from(Transfer transfer) {
		return new TransferResponse(transfer.getId(), transfer.getFromAccountId(), transfer.getToAccountId(),
				transfer.getAmount(), transfer.getCreatedAt());
	}

}

package dev.arsenii.ledger.statement;

import dev.arsenii.ledger.ledger.EntryType;
import dev.arsenii.ledger.ledger.LedgerEntry;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record StatementResponse(UUID accountId, String currency, LocalDate from, LocalDate to,
		BigDecimal openingBalance, BigDecimal closingBalance, List<Entry> entries) {

	public record Entry(EntryType type, BigDecimal amount, UUID transferId, Instant createdAt) {

		static Entry from(LedgerEntry entry) {
			return new Entry(entry.getType(), entry.getAmount(), entry.getTransferId(), entry.getCreatedAt());
		}

	}

	static StatementResponse from(Statement statement) {
		return new StatementResponse(statement.accountId(), statement.currency(), statement.from(), statement.to(),
				statement.openingBalance(), statement.closingBalance(),
				statement.entries().stream().map(Entry::from).toList());
	}

}

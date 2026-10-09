package dev.arsenii.ledger.statement;

import dev.arsenii.ledger.ledger.LedgerEntry;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record Statement(UUID accountId, String currency, LocalDate from, LocalDate to, BigDecimal openingBalance,
		BigDecimal closingBalance, List<LedgerEntry> entries) {
}

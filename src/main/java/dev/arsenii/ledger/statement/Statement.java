package dev.arsenii.ledger.statement;

import dev.arsenii.ledger.ledger.LedgerEntry;
import org.springframework.data.domain.Page;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record Statement(UUID accountId, String currency, LocalDate from, LocalDate to, BigDecimal openingBalance,
		BigDecimal closingBalance, Page<LedgerEntry> entries) {
}

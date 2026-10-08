package dev.arsenii.ledger.ledger;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "ledger_entries")
public class LedgerEntry {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@Column(nullable = false)
	private UUID accountId;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private EntryType type;

	@Column(nullable = false, precision = 19, scale = 2)
	private BigDecimal amount;

	@Column(nullable = false)
	private Instant createdAt;

	private UUID transferId;

	protected LedgerEntry() {
	}

	public LedgerEntry(UUID accountId, EntryType type, BigDecimal amount) {
		this(accountId, type, amount, null);
	}

	public LedgerEntry(UUID accountId, EntryType type, BigDecimal amount, UUID transferId) {
		this.accountId = accountId;
		this.type = type;
		this.amount = amount;
		this.transferId = transferId;
		this.createdAt = Instant.now();
	}

	public UUID getId() {
		return id;
	}

	public UUID getAccountId() {
		return accountId;
	}

	public EntryType getType() {
		return type;
	}

	public BigDecimal getAmount() {
		return amount;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public UUID getTransferId() {
		return transferId;
	}

}

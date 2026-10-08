package dev.arsenii.ledger.transfer;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "transfers")
public class Transfer {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@Column(nullable = false)
	private UUID fromAccountId;

	@Column(nullable = false)
	private UUID toAccountId;

	@Column(nullable = false, precision = 19, scale = 2)
	private BigDecimal amount;

	@Column(nullable = false)
	private Instant createdAt;

	@Column(length = 100)
	private String idempotencyKey;

	protected Transfer() {
	}

	public Transfer(UUID fromAccountId, UUID toAccountId, BigDecimal amount) {
		this(fromAccountId, toAccountId, amount, null);
	}

	public Transfer(UUID fromAccountId, UUID toAccountId, BigDecimal amount, String idempotencyKey) {
		this.fromAccountId = fromAccountId;
		this.toAccountId = toAccountId;
		this.amount = amount;
		this.idempotencyKey = idempotencyKey;
		this.createdAt = Instant.now();
	}

	boolean isSameRequest(UUID fromAccountId, UUID toAccountId, BigDecimal amount) {
		return this.fromAccountId.equals(fromAccountId)
				&& this.toAccountId.equals(toAccountId)
				&& this.amount.compareTo(amount) == 0;
	}

	public UUID getId() {
		return id;
	}

	public UUID getFromAccountId() {
		return fromAccountId;
	}

	public UUID getToAccountId() {
		return toAccountId;
	}

	public BigDecimal getAmount() {
		return amount;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public String getIdempotencyKey() {
		return idempotencyKey;
	}

}

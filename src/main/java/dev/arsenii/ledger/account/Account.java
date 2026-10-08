package dev.arsenii.ledger.account;

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
@Table(name = "accounts")
public class Account {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@Column(nullable = false, length = 100)
	private String ownerName;

	@Column(nullable = false, length = 3)
	private String currency;

	@Column(nullable = false, precision = 19, scale = 2)
	private BigDecimal balance;

	@Column(nullable = false)
	private Instant createdAt;

	protected Account() {
	}

	public Account(String ownerName, String currency) {
		this.ownerName = ownerName;
		this.currency = currency;
		this.balance = BigDecimal.ZERO.setScale(2);
		this.createdAt = Instant.now();
	}

	public void deposit(BigDecimal amount) {
		balance = balance.add(amount);
	}

	public void withdraw(BigDecimal amount) {
		if (balance.compareTo(amount) < 0) {
			throw new InsufficientFundsException(id);
		}
		balance = balance.subtract(amount);
	}

	public UUID getId() {
		return id;
	}

	public String getOwnerName() {
		return ownerName;
	}

	public String getCurrency() {
		return currency;
	}

	public BigDecimal getBalance() {
		return balance;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

}

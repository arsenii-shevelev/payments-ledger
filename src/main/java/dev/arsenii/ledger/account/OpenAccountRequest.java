package dev.arsenii.ledger.account;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record OpenAccountRequest(
		@NotBlank @Size(max = 100) String ownerName,
		@NotNull @Pattern(regexp = "[A-Z]{3}") String currency) {
}

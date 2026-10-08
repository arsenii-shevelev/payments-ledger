package dev.arsenii.ledger.transfer;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface TransferRepository extends JpaRepository<Transfer, UUID> {

	Optional<Transfer> findByFromAccountIdAndIdempotencyKey(UUID fromAccountId, String idempotencyKey);

}

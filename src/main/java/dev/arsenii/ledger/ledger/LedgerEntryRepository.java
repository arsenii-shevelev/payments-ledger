package dev.arsenii.ledger.ledger;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface LedgerEntryRepository extends JpaRepository<LedgerEntry, UUID> {

	List<LedgerEntry> findByAccountIdOrderByCreatedAt(UUID accountId);

	List<LedgerEntry> findByTransferId(UUID transferId);

	@Query("""
			select coalesce(sum(e.amount), 0) from LedgerEntry e
			where e.accountId = :accountId and e.createdAt < :before""")
	BigDecimal sumAmountsBefore(@Param("accountId") UUID accountId, @Param("before") Instant before);

	@Query("""
			select e from LedgerEntry e
			where e.accountId = :accountId and e.createdAt >= :from and e.createdAt < :to
			order by e.createdAt""")
	List<LedgerEntry> findForPeriod(@Param("accountId") UUID accountId, @Param("from") Instant from,
			@Param("to") Instant to);

}

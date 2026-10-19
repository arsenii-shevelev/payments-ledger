package dev.arsenii.ledger.transfer;

import dev.arsenii.ledger.TestcontainersConfiguration;
import dev.arsenii.ledger.account.AccountService;
import dev.arsenii.ledger.account.InsufficientFundsException;
import dev.arsenii.ledger.ledger.LedgerEntryRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
class TransferConcurrencyIT {

	@Autowired
	private AccountService accountService;

	@Autowired
	private TransferService transferService;

	@Autowired
	private LedgerEntryRepository ledgerEntryRepository;

	@Test
	void oppositeTransfersDoNotDeadlock() throws Exception {
		UUID first = openAccountWith("500.00");
		UUID second = openAccountWith("500.00");

		List<Callable<Transfer>> tasks = new ArrayList<>();
		for (int i = 0; i < 50; i++) {
			tasks.add(() -> transferService.transfer(first, second, new BigDecimal("10.00"), null));
			tasks.add(() -> transferService.transfer(second, first, new BigDecimal("10.00"), null));
		}

		for (Future<Transfer> result : runInParallel(tasks)) {
			assertThat(result.get()).isNotNull();
		}
		assertBalance(first, "500.00");
		assertBalance(second, "500.00");
	}

	@Test
	void parallelTransfersNeverOverdrawAccount() throws Exception {
		UUID from = openAccountWith("100.00");
		UUID to = accountService.openAccount("Laura Nieminen", "EUR").getId();

		List<Callable<Transfer>> tasks = new ArrayList<>();
		for (int i = 0; i < 20; i++) {
			tasks.add(() -> transferService.transfer(from, to, new BigDecimal("10.00"), null));
		}

		int succeeded = 0;
		int rejected = 0;
		for (Future<Transfer> result : runInParallel(tasks)) {
			try {
				result.get();
				succeeded++;
			} catch (ExecutionException e) {
				assertThat(e.getCause()).isInstanceOf(InsufficientFundsException.class);
				rejected++;
			}
		}

		assertThat(succeeded).isEqualTo(10);
		assertThat(rejected).isEqualTo(10);
		assertBalance(from, "0.00");
		assertBalance(to, "100.00");
	}

	private UUID openAccountWith(String amount) {
		UUID id = accountService.openAccount("Mikko Laine", "EUR").getId();
		accountService.deposit(id, new BigDecimal(amount));
		return id;
	}

	private List<Future<Transfer>> runInParallel(List<Callable<Transfer>> tasks) throws InterruptedException {
		try (ExecutorService executor = Executors.newFixedThreadPool(8)) {
			return executor.invokeAll(tasks, 30, TimeUnit.SECONDS);
		}
	}

	private void assertBalance(UUID accountId, String expected) {
		assertThat(accountService.getAccount(accountId).getBalance()).isEqualByComparingTo(expected);
		assertThat(ledgerEntryRepository.sumAmountsBefore(accountId, Instant.now())).isEqualByComparingTo(expected);
	}

}

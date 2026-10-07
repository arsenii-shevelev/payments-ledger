package dev.arsenii.ledger.account;

import dev.arsenii.ledger.TestcontainersConfiguration;
import dev.arsenii.ledger.ledger.EntryType;
import dev.arsenii.ledger.ledger.LedgerEntry;
import dev.arsenii.ledger.ledger.LedgerEntryRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class AccountApiIT {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private LedgerEntryRepository ledgerEntryRepository;

	@Test
	void opensAccountAndReadsItBack() throws Exception {
		String location = openAccount("Mikko Laine");

		mockMvc.perform(get(location))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.ownerName").value("Mikko Laine"))
				.andExpect(jsonPath("$.currency").value("EUR"))
				.andExpect(jsonPath("$.balance").value(0.0));
	}

	@Test
	void returnsNotFoundForUnknownAccount() throws Exception {
		UUID id = UUID.fromString("00000000-0000-0000-0000-000000000001");

		mockMvc.perform(get("/accounts/{id}", id))
				.andExpect(status().isNotFound());
	}

	@Test
	void depositIncreasesBalanceAndAddsLedgerEntry() throws Exception {
		String location = openAccount("Laura Nieminen");
		UUID accountId = UUID.fromString(location.substring(location.lastIndexOf('/') + 1));

		mockMvc.perform(post(location + "/deposits")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"amount\": 100.50}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.balance").value(100.5));

		mockMvc.perform(get(location))
				.andExpect(jsonPath("$.balance").value(100.5));

		List<LedgerEntry> entries = ledgerEntryRepository.findByAccountIdOrderByCreatedAt(accountId);
		assertThat(entries).hasSize(1);
		assertThat(entries.getFirst().getType()).isEqualTo(EntryType.DEPOSIT);
		assertThat(entries.getFirst().getAmount()).isEqualByComparingTo(new BigDecimal("100.50"));
	}

	@Test
	void depositToUnknownAccountReturnsNotFound() throws Exception {
		UUID id = UUID.fromString("00000000-0000-0000-0000-000000000002");

		mockMvc.perform(post("/accounts/{id}/deposits", id)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"amount\": 10}"))
				.andExpect(status().isNotFound());
	}

	private String openAccount(String ownerName) throws Exception {
		return mockMvc.perform(post("/accounts")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"ownerName\": \"" + ownerName + "\", \"currency\": \"EUR\"}"))
				.andExpect(status().isCreated())
				.andReturn()
				.getResponse()
				.getHeader("Location");
	}

}

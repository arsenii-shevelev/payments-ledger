package dev.arsenii.ledger.transfer;

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
class TransferApiIT {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private LedgerEntryRepository ledgerEntryRepository;

	@Test
	void transferMovesMoneyBetweenAccounts() throws Exception {
		UUID from = openAccount("Mikko Laine", "EUR");
		UUID to = openAccount("Laura Nieminen", "EUR");
		deposit(from, "100.00");

		String location = mockMvc.perform(post("/transfers")
						.contentType(MediaType.APPLICATION_JSON)
						.content(transferJson(from, to, "40.25")))
				.andExpect(status().isCreated())
				.andReturn()
				.getResponse()
				.getHeader("Location");

		mockMvc.perform(get("/accounts/{id}", from))
				.andExpect(jsonPath("$.balance").value(59.75));
		mockMvc.perform(get("/accounts/{id}", to))
				.andExpect(jsonPath("$.balance").value(40.25));
		mockMvc.perform(get(location))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.amount").value(40.25));

		UUID transferId = UUID.fromString(location.substring(location.lastIndexOf('/') + 1));
		List<LedgerEntry> entries = ledgerEntryRepository.findByTransferId(transferId);
		assertThat(entries).extracting(LedgerEntry::getType)
				.containsExactlyInAnyOrder(EntryType.TRANSFER_OUT, EntryType.TRANSFER_IN);
		assertThat(entries.stream().map(LedgerEntry::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add))
				.isEqualByComparingTo("0");
	}

	@Test
	void rejectsTransferWithInsufficientFunds() throws Exception {
		UUID from = openAccount("Mikko Laine", "EUR");
		UUID to = openAccount("Laura Nieminen", "EUR");
		deposit(from, "20.00");

		mockMvc.perform(post("/transfers")
						.contentType(MediaType.APPLICATION_JSON)
						.content(transferJson(from, to, "50.00")))
				.andExpect(status().isUnprocessableContent());

		mockMvc.perform(get("/accounts/{id}", from))
				.andExpect(jsonPath("$.balance").value(20.0));
		mockMvc.perform(get("/accounts/{id}", to))
				.andExpect(jsonPath("$.balance").value(0.0));
	}

	@Test
	void rejectsTransferBetweenDifferentCurrencies() throws Exception {
		UUID from = openAccount("Mikko Laine", "EUR");
		UUID to = openAccount("Erik Lindqvist", "SEK");
		deposit(from, "20.00");

		mockMvc.perform(post("/transfers")
						.contentType(MediaType.APPLICATION_JSON)
						.content(transferJson(from, to, "10.00")))
				.andExpect(status().isUnprocessableContent());
	}

	@Test
	void transferFromUnknownAccountReturnsNotFound() throws Exception {
		UUID unknown = UUID.fromString("00000000-0000-0000-0000-000000000004");
		UUID to = openAccount("Laura Nieminen", "EUR");

		mockMvc.perform(post("/transfers")
						.contentType(MediaType.APPLICATION_JSON)
						.content(transferJson(unknown, to, "10.00")))
				.andExpect(status().isNotFound());
	}

	private UUID openAccount(String ownerName, String currency) throws Exception {
		String location = mockMvc.perform(post("/accounts")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"ownerName\": \"" + ownerName + "\", \"currency\": \"" + currency + "\"}"))
				.andExpect(status().isCreated())
				.andReturn()
				.getResponse()
				.getHeader("Location");
		return UUID.fromString(location.substring(location.lastIndexOf('/') + 1));
	}

	private void deposit(UUID accountId, String amount) throws Exception {
		mockMvc.perform(post("/accounts/{id}/deposits", accountId)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"amount\": " + amount + "}"))
				.andExpect(status().isOk());
	}

	private String transferJson(UUID from, UUID to, String amount) {
		return "{\"fromAccountId\": \"" + from + "\", \"toAccountId\": \"" + to + "\", \"amount\": " + amount + "}";
	}

}

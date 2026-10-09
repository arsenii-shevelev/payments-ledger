package dev.arsenii.ledger.statement;

import dev.arsenii.ledger.TestcontainersConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class StatementApiIT {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void statementShowsTodaysEntries() throws Exception {
		UUID from = openAccount("Mikko Laine");
		UUID to = openAccount("Laura Nieminen");
		deposit(from, "100.00");
		transfer(from, to, "35.50");
		String today = LocalDate.now(ZoneOffset.UTC).toString();

		mockMvc.perform(get("/accounts/{id}/statement", from)
						.param("from", today)
						.param("to", today))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.openingBalance").value(0.0))
				.andExpect(jsonPath("$.closingBalance").value(64.5))
				.andExpect(jsonPath("$.entries.length()").value(2))
				.andExpect(jsonPath("$.entries[0].type").value("DEPOSIT"))
				.andExpect(jsonPath("$.entries[1].type").value("TRANSFER_OUT"))
				.andExpect(jsonPath("$.entries[1].amount").value(-35.5));
	}

	@Test
	void earlierEntriesGoIntoOpeningBalance() throws Exception {
		UUID account = openAccount("Mikko Laine");
		deposit(account, "80.00");
		String tomorrow = LocalDate.now(ZoneOffset.UTC).plusDays(1).toString();

		mockMvc.perform(get("/accounts/{id}/statement", account)
						.param("from", tomorrow)
						.param("to", tomorrow))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.openingBalance").value(80.0))
				.andExpect(jsonPath("$.closingBalance").value(80.0))
				.andExpect(jsonPath("$.entries.length()").value(0));
	}

	@Test
	void entriesComeInPagesWithTheSameBalances() throws Exception {
		UUID account = openAccount("Mikko Laine");
		deposit(account, "10.00");
		deposit(account, "20.00");
		deposit(account, "30.00");
		String yesterday = LocalDate.now(ZoneOffset.UTC).minusDays(1).toString();
		String tomorrow = LocalDate.now(ZoneOffset.UTC).plusDays(1).toString();

		mockMvc.perform(get("/accounts/{id}/statement", account)
						.param("from", yesterday)
						.param("to", tomorrow)
						.param("size", "2"))
				.andExpect(jsonPath("$.totalEntries").value(3))
				.andExpect(jsonPath("$.entries.length()").value(2))
				.andExpect(jsonPath("$.entries[0].amount").value(10.0))
				.andExpect(jsonPath("$.closingBalance").value(60.0));

		mockMvc.perform(get("/accounts/{id}/statement", account)
						.param("from", yesterday)
						.param("to", tomorrow)
						.param("size", "2")
						.param("page", "1"))
				.andExpect(jsonPath("$.entries.length()").value(1))
				.andExpect(jsonPath("$.entries[0].amount").value(30.0))
				.andExpect(jsonPath("$.closingBalance").value(60.0));
	}

	@Test
	void statementForUnknownAccountReturnsNotFound() throws Exception {
		mockMvc.perform(get("/accounts/{id}/statement", UUID.fromString("00000000-0000-0000-0000-000000000005"))
						.param("from", "2026-10-01")
						.param("to", "2026-10-31"))
				.andExpect(status().isNotFound());
	}

	private UUID openAccount(String ownerName) throws Exception {
		String location = mockMvc.perform(post("/accounts")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"ownerName\": \"" + ownerName + "\", \"currency\": \"EUR\"}"))
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

	private void transfer(UUID from, UUID to, String amount) throws Exception {
		mockMvc.perform(post("/transfers")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"fromAccountId\": \"" + from + "\", \"toAccountId\": \"" + to + "\", \"amount\": " + amount + "}"))
				.andExpect(status().isCreated());
	}

}

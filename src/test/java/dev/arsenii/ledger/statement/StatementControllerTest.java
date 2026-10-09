package dev.arsenii.ledger.statement;

import dev.arsenii.ledger.ledger.EntryType;
import dev.arsenii.ledger.ledger.LedgerEntry;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(StatementController.class)
class StatementControllerTest {

	private static final UUID ACCOUNT_ID = UUID.fromString("7f3c2a1e-5b4d-4c6e-9a8b-1d2e3f4a5b6c");

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private StatementService statementService;

	@Test
	void returnsStatement() throws Exception {
		LocalDate from = LocalDate.of(2026, 10, 1);
		LocalDate to = LocalDate.of(2026, 10, 31);
		List<LedgerEntry> entries = List.of(new LedgerEntry(ACCOUNT_ID, EntryType.DEPOSIT, new BigDecimal("50.00")));
		Statement statement = new Statement(ACCOUNT_ID, "EUR", from, to, new BigDecimal("100.00"),
				new BigDecimal("150.00"), new PageImpl<>(entries, PageRequest.of(0, 50), 1));
		when(statementService.getStatement(ACCOUNT_ID, from, to, 0, 50)).thenReturn(statement);

		mockMvc.perform(get("/accounts/{id}/statement", ACCOUNT_ID)
						.param("from", "2026-10-01")
						.param("to", "2026-10-31"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.openingBalance").value(100.0))
				.andExpect(jsonPath("$.closingBalance").value(150.0))
				.andExpect(jsonPath("$.page").value(0))
				.andExpect(jsonPath("$.size").value(50))
				.andExpect(jsonPath("$.totalEntries").value(1))
				.andExpect(jsonPath("$.entries[0].type").value("DEPOSIT"))
				.andExpect(jsonPath("$.entries[0].amount").value(50.0));
	}

	@Test
	void rejectsPeriodThatEndsBeforeItStarts() throws Exception {
		mockMvc.perform(get("/accounts/{id}/statement", ACCOUNT_ID)
						.param("from", "2026-10-31")
						.param("to", "2026-10-01"))
				.andExpect(status().isBadRequest());

		verify(statementService, never()).getStatement(any(), any(), any(), anyInt(), anyInt());
	}

	@Test
	void rejectsTooLargePage() throws Exception {
		mockMvc.perform(get("/accounts/{id}/statement", ACCOUNT_ID)
						.param("from", "2026-10-01")
						.param("to", "2026-10-31")
						.param("size", "500"))
				.andExpect(status().isBadRequest());

		verify(statementService, never()).getStatement(any(), any(), any(), anyInt(), anyInt());
	}

	@Test
	void requiresPeriod() throws Exception {
		mockMvc.perform(get("/accounts/{id}/statement", ACCOUNT_ID))
				.andExpect(status().isBadRequest());
	}

}

package dev.arsenii.ledger.account;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AccountController.class)
class AccountControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private AccountService accountService;

	@Test
	void opensAccount() throws Exception {
		UUID id = UUID.fromString("7f3c2a1e-5b4d-4c6e-9a8b-1d2e3f4a5b6c");
		when(accountService.openAccount("Anna Virtanen", "EUR")).thenReturn(accountWithId(id, "Anna Virtanen", "EUR"));

		mockMvc.perform(post("/accounts")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"ownerName\": \"Anna Virtanen\", \"currency\": \"EUR\"}"))
				.andExpect(status().isCreated())
				.andExpect(header().string("Location", "/accounts/" + id))
				.andExpect(jsonPath("$.ownerName").value("Anna Virtanen"))
				.andExpect(jsonPath("$.currency").value("EUR"))
				.andExpect(jsonPath("$.balance").value(0.0));
	}

	@Test
	void rejectsInvalidCurrency() throws Exception {
		mockMvc.perform(post("/accounts")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"ownerName\": \"Anna Virtanen\", \"currency\": \"euro\"}"))
				.andExpect(status().isBadRequest());

		verify(accountService, never()).openAccount(any(), any());
	}

	@Test
	void returnsNotFoundForUnknownAccount() throws Exception {
		UUID id = UUID.fromString("00000000-0000-0000-0000-000000000001");
		when(accountService.getAccount(id)).thenThrow(new AccountNotFoundException(id));

		mockMvc.perform(get("/accounts/{id}", id))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.detail").value("Account " + id + " not found"));
	}

	private Account accountWithId(UUID id, String ownerName, String currency) {
		Account account = new Account(ownerName, currency);
		ReflectionTestUtils.setField(account, "id", id);
		return account;
	}

}

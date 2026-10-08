package dev.arsenii.ledger.transfer;

import dev.arsenii.ledger.account.InsufficientFundsException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
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

@WebMvcTest(TransferController.class)
class TransferControllerTest {

	private static final UUID FROM_ID = UUID.fromString("3b1f8c2d-6a4e-4f7b-9c1d-2e3f4a5b6c7d");
	private static final UUID TO_ID = UUID.fromString("8d2e9f3a-1b5c-4d6e-8f7a-9b0c1d2e3f4a");

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private TransferService transferService;

	@Test
	void createsTransfer() throws Exception {
		UUID id = UUID.fromString("5c6d7e8f-9a0b-4c1d-8e2f-3a4b5c6d7e8f");
		Transfer transfer = new Transfer(FROM_ID, TO_ID, new BigDecimal("25.50"));
		ReflectionTestUtils.setField(transfer, "id", id);
		when(transferService.transfer(FROM_ID, TO_ID, new BigDecimal("25.50"))).thenReturn(transfer);

		mockMvc.perform(post("/transfers")
						.contentType(MediaType.APPLICATION_JSON)
						.content(transferJson("25.50")))
				.andExpect(status().isCreated())
				.andExpect(header().string("Location", "/transfers/" + id))
				.andExpect(jsonPath("$.fromAccountId").value(FROM_ID.toString()))
				.andExpect(jsonPath("$.toAccountId").value(TO_ID.toString()))
				.andExpect(jsonPath("$.amount").value(25.5));
	}

	@Test
	void rejectsZeroAmount() throws Exception {
		mockMvc.perform(post("/transfers")
						.contentType(MediaType.APPLICATION_JSON)
						.content(transferJson("0")))
				.andExpect(status().isBadRequest());

		verify(transferService, never()).transfer(any(), any(), any());
	}

	@Test
	void returnsUnprocessableContentWhenFundsAreInsufficient() throws Exception {
		when(transferService.transfer(FROM_ID, TO_ID, new BigDecimal("500.00")))
				.thenThrow(new InsufficientFundsException(FROM_ID));

		mockMvc.perform(post("/transfers")
						.contentType(MediaType.APPLICATION_JSON)
						.content(transferJson("500.00")))
				.andExpect(status().isUnprocessableContent())
				.andExpect(jsonPath("$.detail").value("Account " + FROM_ID + " has insufficient funds"));
	}

	@Test
	void returnsNotFoundForUnknownTransfer() throws Exception {
		UUID id = UUID.fromString("00000000-0000-0000-0000-000000000003");
		when(transferService.getTransfer(id)).thenThrow(new TransferNotFoundException(id));

		mockMvc.perform(get("/transfers/{id}", id))
				.andExpect(status().isNotFound());
	}

	private String transferJson(String amount) {
		return "{\"fromAccountId\": \"" + FROM_ID + "\", \"toAccountId\": \"" + TO_ID + "\", \"amount\": " + amount + "}";
	}

}

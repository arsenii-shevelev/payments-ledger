package dev.arsenii.ledger.account;

import dev.arsenii.ledger.TestcontainersConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

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

	@Test
	void opensAccountAndReadsItBack() throws Exception {
		String location = mockMvc.perform(post("/accounts")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"ownerName\": \"Mikko Laine\", \"currency\": \"EUR\"}"))
				.andExpect(status().isCreated())
				.andReturn()
				.getResponse()
				.getHeader("Location");

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

}

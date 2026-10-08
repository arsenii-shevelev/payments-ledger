package dev.arsenii.ledger.transfer;

import dev.arsenii.ledger.account.Account;
import dev.arsenii.ledger.account.AccountRepository;
import dev.arsenii.ledger.ledger.EntryType;
import dev.arsenii.ledger.ledger.LedgerEntry;
import dev.arsenii.ledger.ledger.LedgerEntryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransferServiceTest {

	private static final UUID FIRST_ID = UUID.fromString("00000000-0000-0000-0000-00000000000a");
	private static final UUID SECOND_ID = UUID.fromString("00000000-0000-0000-0000-00000000000b");

	@Mock
	private AccountRepository accountRepository;

	@Mock
	private TransferRepository transferRepository;

	@Mock
	private LedgerEntryRepository ledgerEntryRepository;

	@InjectMocks
	private TransferService transferService;

	@Test
	void movesMoneyAndWritesTwoLedgerEntries() {
		Account from = account(FIRST_ID, "EUR", "100.00");
		Account to = account(SECOND_ID, "EUR", "0.00");
		when(accountRepository.findByIdForUpdate(FIRST_ID)).thenReturn(Optional.of(from));
		when(accountRepository.findByIdForUpdate(SECOND_ID)).thenReturn(Optional.of(to));
		when(transferRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

		transferService.transfer(FIRST_ID, SECOND_ID, new BigDecimal("40.00"), null);

		assertThat(from.getBalance()).isEqualByComparingTo("60.00");
		assertThat(to.getBalance()).isEqualByComparingTo("40.00");

		ArgumentCaptor<LedgerEntry> entries = ArgumentCaptor.forClass(LedgerEntry.class);
		verify(ledgerEntryRepository, times(2)).save(entries.capture());
		List<LedgerEntry> saved = entries.getAllValues();
		assertThat(saved).extracting(LedgerEntry::getType)
				.containsExactly(EntryType.TRANSFER_OUT, EntryType.TRANSFER_IN);
		assertThat(saved.get(0).getAmount().add(saved.get(1).getAmount())).isEqualByComparingTo("0");
	}

	@Test
	void locksAccountsInIdOrder() {
		when(accountRepository.findByIdForUpdate(FIRST_ID)).thenReturn(Optional.of(account(FIRST_ID, "EUR", "0.00")));
		when(accountRepository.findByIdForUpdate(SECOND_ID)).thenReturn(Optional.of(account(SECOND_ID, "EUR", "50.00")));
		when(transferRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

		transferService.transfer(SECOND_ID, FIRST_ID, new BigDecimal("10.00"), null);

		InOrder inOrder = inOrder(accountRepository);
		inOrder.verify(accountRepository).findByIdForUpdate(FIRST_ID);
		inOrder.verify(accountRepository).findByIdForUpdate(SECOND_ID);
	}

	@Test
	void rejectsTransferToSameAccount() {
		assertThatThrownBy(() -> transferService.transfer(FIRST_ID, FIRST_ID, new BigDecimal("10.00"), null))
				.isInstanceOf(InvalidTransferException.class);

		verifyNoInteractions(accountRepository, transferRepository, ledgerEntryRepository);
	}

	@Test
	void rejectsAccountsWithDifferentCurrencies() {
		Account from = account(FIRST_ID, "EUR", "100.00");
		Account to = account(SECOND_ID, "SEK", "0.00");
		when(accountRepository.findByIdForUpdate(FIRST_ID)).thenReturn(Optional.of(from));
		when(accountRepository.findByIdForUpdate(SECOND_ID)).thenReturn(Optional.of(to));

		assertThatThrownBy(() -> transferService.transfer(FIRST_ID, SECOND_ID, new BigDecimal("10.00"), null))
				.isInstanceOf(InvalidTransferException.class);

		assertThat(from.getBalance()).isEqualByComparingTo("100.00");
		verify(ledgerEntryRepository, never()).save(any());
	}

	@Test
	void returnsExistingTransferWhenKeyIsRepeated() {
		Account from = account(FIRST_ID, "EUR", "60.00");
		Account to = account(SECOND_ID, "EUR", "40.00");
		Transfer existing = new Transfer(FIRST_ID, SECOND_ID, new BigDecimal("40.00"), "order-1001");
		when(accountRepository.findByIdForUpdate(FIRST_ID)).thenReturn(Optional.of(from));
		when(accountRepository.findByIdForUpdate(SECOND_ID)).thenReturn(Optional.of(to));
		when(transferRepository.findByIdempotencyKey("order-1001")).thenReturn(Optional.of(existing));

		Transfer result = transferService.transfer(FIRST_ID, SECOND_ID, new BigDecimal("40.00"), "order-1001");

		assertThat(result).isSameAs(existing);
		assertThat(from.getBalance()).isEqualByComparingTo("60.00");
		assertThat(to.getBalance()).isEqualByComparingTo("40.00");
		verify(transferRepository, never()).save(any());
		verify(ledgerEntryRepository, never()).save(any());
	}

	@Test
	void rejectsKeyUsedForDifferentTransfer() {
		when(accountRepository.findByIdForUpdate(FIRST_ID)).thenReturn(Optional.of(account(FIRST_ID, "EUR", "100.00")));
		when(accountRepository.findByIdForUpdate(SECOND_ID)).thenReturn(Optional.of(account(SECOND_ID, "EUR", "0.00")));
		when(transferRepository.findByIdempotencyKey("order-1001"))
				.thenReturn(Optional.of(new Transfer(FIRST_ID, SECOND_ID, new BigDecimal("40.00"), "order-1001")));

		assertThatThrownBy(() -> transferService.transfer(FIRST_ID, SECOND_ID, new BigDecimal("50.00"), "order-1001"))
				.isInstanceOf(InvalidTransferException.class);

		verify(ledgerEntryRepository, never()).save(any());
	}

	private Account account(UUID id, String currency, String balance) {
		Account account = new Account("Anna Virtanen", currency);
		ReflectionTestUtils.setField(account, "id", id);
		account.deposit(new BigDecimal(balance));
		return account;
	}

}

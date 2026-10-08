package dev.arsenii.ledger.common;

import dev.arsenii.ledger.account.AccountNotFoundException;
import dev.arsenii.ledger.account.InsufficientFundsException;
import dev.arsenii.ledger.transfer.InvalidTransferException;
import dev.arsenii.ledger.transfer.TransferNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

	@ExceptionHandler({AccountNotFoundException.class, TransferNotFoundException.class})
	public ProblemDetail handleNotFound(RuntimeException ex) {
		return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
	}

	@ExceptionHandler({InsufficientFundsException.class, InvalidTransferException.class})
	public ProblemDetail handleRejectedTransfer(RuntimeException ex) {
		return ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_CONTENT, ex.getMessage());
	}

}

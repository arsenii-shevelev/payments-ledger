package dev.arsenii.ledger.transfer;

public class InvalidTransferException extends RuntimeException {

	public InvalidTransferException(String message) {
		super(message);
	}

}

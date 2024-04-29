package com.paymybuddy.paymybuddy.exceptions;

public class InsufficientBalanceException extends RuntimeException {
	/**
	 * Exception thrown when a user tries to sign in with an existing email.
	 *     @param message Exception message.
	 */
	public InsufficientBalanceException(String message) {
		super(message);
	}
}

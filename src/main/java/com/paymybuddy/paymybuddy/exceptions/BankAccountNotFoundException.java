package com.paymybuddy.paymybuddy.exceptions;

/**
 * Exception when user has no bank account
 */
public class BankAccountNotFoundException extends RuntimeException {

	/**
	 * Exception thrown when the provided user does not own a bank account
	 *
	 * @param message Exception message.
	 */
	public BankAccountNotFoundException(String message) {
		super(message);
	}
}

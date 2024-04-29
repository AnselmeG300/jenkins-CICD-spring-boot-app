package com.paymybuddy.paymybuddy.exceptions;

/**
 * Email already exists exception.
 */
public class EmailAlreadyUsedException extends RuntimeException {

	/**
	 * Exception thrown when a user tries to sign in with an existing email.
	 *
	 * @param message Exception message.
	 */
	public EmailAlreadyUsedException(String message) {
		super(message);
	}
}

package com.paymybuddy.paymybuddy.exceptions;

/**
 * Exception for when amount is negative or higher than user's balance.
 */
public class InvalidPayeeException extends RuntimeException {

	/**
	 * Exception thrown when amount is not valid.
	 *
	 * @param message Exception message.
	 */
	public InvalidPayeeException(String message) {
		super(message);
	}
}

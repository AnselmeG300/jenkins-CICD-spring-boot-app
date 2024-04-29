package com.paymybuddy.paymybuddy.exceptions;

/**
 * Exception for when amount is negative or higher than user's balance.
 */
public class InvalidAmountException extends RuntimeException {

	/**
	 * Exception thrown when amount is not valid.
	 *
	 * @param message Exception message.
	 */
	public InvalidAmountException(String message) {
		super(message);
	}
}

package com.paymybuddy.paymybuddy.exceptions;

/**
 * Exception when email is not in database.
 */
public class BuddyNotFoundException extends RuntimeException {

	/**
	 * Exception thrown when the provided user for a connection does not exist.
	 *
	 * @param message Exception message.
	 */
	public BuddyNotFoundException(String message) {
		super(message);
	}
}

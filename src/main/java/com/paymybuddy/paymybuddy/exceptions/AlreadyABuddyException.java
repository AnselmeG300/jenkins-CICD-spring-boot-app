package com.paymybuddy.paymybuddy.exceptions;

/**
 * Buddy is already a connection exception.
 */
public class AlreadyABuddyException extends RuntimeException {

	/**
	 * Exception thrown when a user tries to add a connection who is already in their connections list.
	 *
	 * @param message Exception message.
	 */
	public AlreadyABuddyException(String message) {
		super(message);
	}
}

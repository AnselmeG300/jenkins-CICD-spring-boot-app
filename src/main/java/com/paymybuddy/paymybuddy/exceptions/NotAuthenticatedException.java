package com.paymybuddy.paymybuddy.exceptions;

import org.springframework.security.access.AccessDeniedException;

/**
 *
 */
public class NotAuthenticatedException extends AccessDeniedException {

	public NotAuthenticatedException(String message) {
		super(message);
	}
}

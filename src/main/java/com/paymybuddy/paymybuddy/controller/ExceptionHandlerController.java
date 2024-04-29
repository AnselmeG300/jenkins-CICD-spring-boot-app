package com.paymybuddy.paymybuddy.controller;

import com.paymybuddy.paymybuddy.exceptions.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Exception thrown when a resource is not found.
 */
@Slf4j
@RestControllerAdvice
public class ExceptionHandlerController {


    @ExceptionHandler(BuddyNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String buddyNotFoundException(BuddyNotFoundException notFoundException) {
        log.error("Buddy not found.", notFoundException);
        return "Buddy not found:\n" + notFoundException.getMessage();
    }

    @ExceptionHandler(BankAccountNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String bankAccountNotFoundException(BankAccountNotFoundException notFoundException) {
        log.error("Bank account not found.", notFoundException);
        return "Bank account not found:\n" + notFoundException.getMessage();
    }

    @ExceptionHandler(InvalidAmountException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String illegalValueException(InvalidAmountException invalidAmountException) {
        log.error("Illegal argument value.", invalidAmountException);
        return "Illegal argument value:\n" + invalidAmountException.getMessage();
    }

    @ExceptionHandler(InvalidPayeeException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String illegalValueException(InvalidPayeeException invalidPayeeException) {
        log.error("Illegal argument value.", invalidPayeeException);
        return "Illegal argument value:\n" + invalidPayeeException.getMessage();
    }

    @ExceptionHandler(InsufficientBalanceException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String illegalValueException(InsufficientBalanceException insufficientBalanceException) {
        log.error("Illegal argument value.", insufficientBalanceException);
        return "Illegal argument value:\n" + insufficientBalanceException.getMessage();
    }

    @ExceptionHandler(AlreadyABuddyException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String illegalValueException(AlreadyABuddyException alreadyABuddyException) {
        log.error("Illegal argument value.", alreadyABuddyException);
        return "Illegal argument value:\n" + alreadyABuddyException.getMessage();
    }

    @ExceptionHandler(EmailAlreadyUsedException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String illegalValueException(EmailAlreadyUsedException emailAlreadyUsedException) {
        log.error("Illegal argument value.", emailAlreadyUsedException);
        return "Illegal argument value:\n" + emailAlreadyUsedException.getMessage();
    }

    @ExceptionHandler(NotAuthenticatedException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public String notAuthenticatedException(NotAuthenticatedException notAuthenticatedException) {
        log.error("Not authenticated", notAuthenticatedException);
        return "Unauthorized.\n" + notAuthenticatedException.getMessage();
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String returnMessage(Exception exception) {
        log.error("An error occurred.", exception);
        return "An error occurred:\n " + exception.getMessage();
    }
}

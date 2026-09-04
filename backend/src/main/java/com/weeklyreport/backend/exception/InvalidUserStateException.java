package com.weeklyreport.backend.exception;

import org.springframework.http.HttpStatus;

/** The requested transition conflicts with the user's current status. */
public class InvalidUserStateException extends ApiException {

    public InvalidUserStateException(String message) {
        super(HttpStatus.CONFLICT, message);
    }
}

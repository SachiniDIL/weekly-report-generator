package com.weeklyreport.backend.exception;

import org.springframework.http.HttpStatus;

/**
 * Raised for both an unknown email and a wrong password — the two cases are intentionally
 * indistinguishable to the client so the API cannot be used to enumerate accounts.
 */
public class InvalidCredentialsException extends ApiException {

    public InvalidCredentialsException() {
        super(HttpStatus.UNAUTHORIZED, "Invalid email or password");
    }
}

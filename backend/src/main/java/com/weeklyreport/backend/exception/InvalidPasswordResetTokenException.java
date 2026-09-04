package com.weeklyreport.backend.exception;

import org.springframework.http.HttpStatus;

/** The supplied reset token is unknown, already used, or expired — not distinguished. */
public class InvalidPasswordResetTokenException extends ApiException {

    public InvalidPasswordResetTokenException() {
        super(HttpStatus.BAD_REQUEST, "This password reset link is invalid or has expired");
    }
}

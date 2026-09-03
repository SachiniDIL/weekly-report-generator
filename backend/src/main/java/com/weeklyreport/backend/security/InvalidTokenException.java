package com.weeklyreport.backend.security;

/**
 * Thrown when a JWT fails signature or expiry verification, or is otherwise
 * unparseable. Calling code can catch this single type instead of the various
 * jjwt exceptions; the original failure is preserved as the cause.
 */
public class InvalidTokenException extends RuntimeException {

    public InvalidTokenException(Throwable cause) {
        super("JWT is invalid or expired", cause);
    }
}

package com.weeklyreport.backend.exception;

import org.springframework.http.HttpStatus;

/**
 * Base type for errors that map to a deliberate HTTP response. Carrying the status here
 * lets {@link GlobalExceptionHandler} translate any subtype with a single handler.
 */
public abstract class ApiException extends RuntimeException {

    private final HttpStatus status;

    protected ApiException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}

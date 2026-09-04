package com.weeklyreport.backend.exception;

import org.springframework.http.HttpStatus;

/** The requested action conflicts with the report's current status. */
public class InvalidReportStateException extends ApiException {

    public InvalidReportStateException(String message) {
        super(HttpStatus.CONFLICT, message);
    }
}

package com.weeklyreport.backend.exception;

import org.springframework.http.HttpStatus;

/** A member already has a report for this project and reporting week. */
public class DuplicateReportException extends ApiException {

    public DuplicateReportException() {
        super(HttpStatus.CONFLICT, "You already have a report for this project and week.");
    }
}

package com.weeklyreport.backend.exception;

import org.springframework.http.HttpStatus;

/** A report can only cover the current or a past week, never one that hasn't started. */
public class FutureReportWeekException extends ApiException {

    public FutureReportWeekException() {
        super(HttpStatus.UNPROCESSABLE_ENTITY, "You can't create a report for an upcoming week.");
    }
}

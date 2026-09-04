package com.weeklyreport.backend.exception;

import org.springframework.http.HttpStatus;

public class ReportNotFoundException extends ApiException {

    public ReportNotFoundException(long id) {
        super(HttpStatus.NOT_FOUND, "Report " + id + " not found");
    }
}

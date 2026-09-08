package com.weeklyreport.backend.exception;

import org.springframework.http.HttpStatus;

/** Admin accounts are load-bearing — removing one is blocked outright, whoever asks. */
public class AdminNotRemovableException extends ApiException {

    public AdminNotRemovableException() {
        super(HttpStatus.FORBIDDEN, "An admin account cannot be removed.");
    }
}

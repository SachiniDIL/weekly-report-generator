package com.weeklyreport.backend.exception;

import org.springframework.http.HttpStatus;

public class EmailAlreadyUsedException extends ApiException {

    public EmailAlreadyUsedException() {
        super(HttpStatus.CONFLICT, "An account with this email already exists");
    }
}

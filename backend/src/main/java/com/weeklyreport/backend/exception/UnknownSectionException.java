package com.weeklyreport.backend.exception;

import org.springframework.http.HttpStatus;

public class UnknownSectionException extends ApiException {

    public UnknownSectionException(String requested) {
        super(
                HttpStatus.BAD_REQUEST,
                "Unknown section '" + requested + "'; expected blockers or achievements");
    }
}

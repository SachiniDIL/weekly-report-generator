package com.weeklyreport.backend.exception;

import org.springframework.http.HttpStatus;

public class UserNotFoundException extends ApiException {

    public UserNotFoundException(long id) {
        super(HttpStatus.NOT_FOUND, "User " + id + " not found");
    }
}

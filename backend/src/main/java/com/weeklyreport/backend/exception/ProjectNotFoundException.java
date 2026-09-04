package com.weeklyreport.backend.exception;

import org.springframework.http.HttpStatus;

public class ProjectNotFoundException extends ApiException {

    public ProjectNotFoundException(long id) {
        super(HttpStatus.NOT_FOUND, "Project " + id + " not found");
    }
}

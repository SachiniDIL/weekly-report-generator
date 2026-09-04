package com.weeklyreport.backend.exception;

import org.springframework.http.HttpStatus;

public class ProjectAlreadyArchivedException extends ApiException {

    public ProjectAlreadyArchivedException(long id) {
        super(HttpStatus.CONFLICT, "Project " + id + " is already archived");
    }
}

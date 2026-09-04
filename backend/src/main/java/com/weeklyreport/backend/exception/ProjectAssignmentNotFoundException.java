package com.weeklyreport.backend.exception;

import org.springframework.http.HttpStatus;

public class ProjectAssignmentNotFoundException extends ApiException {

    public ProjectAssignmentNotFoundException(long projectId, long userId) {
        super(HttpStatus.NOT_FOUND, "User " + userId + " is not assigned to project " + projectId);
    }
}

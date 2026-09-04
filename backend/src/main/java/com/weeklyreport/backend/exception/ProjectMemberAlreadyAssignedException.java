package com.weeklyreport.backend.exception;

import org.springframework.http.HttpStatus;

public class ProjectMemberAlreadyAssignedException extends ApiException {

    public ProjectMemberAlreadyAssignedException(long projectId, long userId) {
        super(
                HttpStatus.CONFLICT,
                "User " + userId + " is already assigned to project " + projectId);
    }
}

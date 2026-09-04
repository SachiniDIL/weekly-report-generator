package com.weeklyreport.backend.exception;

import org.springframework.http.HttpStatus;

/** Projects are about work assignment; Admins don't file reports, so they aren't assignable. */
public class AdminNotAssignableException extends ApiException {

    public AdminNotAssignableException() {
        super(HttpStatus.CONFLICT, "Admins cannot be assigned to a project");
    }
}

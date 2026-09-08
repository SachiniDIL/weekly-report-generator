package com.weeklyreport.backend.exception;

import org.springframework.http.HttpStatus;

/** The system must keep at least one active admin, so the last one can't be demoted. */
public class LastAdminException extends ApiException {

    public LastAdminException() {
        super(
                HttpStatus.CONFLICT,
                "This is the only admin. Make someone else an admin before changing this role.");
    }
}

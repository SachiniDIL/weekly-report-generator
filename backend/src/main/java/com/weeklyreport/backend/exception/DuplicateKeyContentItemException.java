package com.weeklyreport.backend.exception;

import org.springframework.http.HttpStatus;

/**
 * Surfaces the "at most one key blocker / key highlight per version" partial-unique-index rule
 * (see V4's migration) as a clean client error instead of a raw database exception.
 */
public class DuplicateKeyContentItemException extends ApiException {

    public DuplicateKeyContentItemException() {
        super(
                HttpStatus.CONFLICT,
                "A report version can have at most one key blocker and one key achievement");
    }
}

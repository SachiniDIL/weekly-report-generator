package com.weeklyreport.backend.exception;

import org.springframework.http.HttpStatus;

/**
 * Raised when valid credentials belong to an account still awaiting admin approval.
 * Revealing this (rather than a generic failure) is a deliberate trade-off: the
 * credentials were already proven correct, so nothing new is leaked, and the user needs
 * to know why they cannot log in.
 */
public class AccountPendingApprovalException extends ApiException {

    public AccountPendingApprovalException() {
        super(HttpStatus.FORBIDDEN, "Your account is pending admin approval");
    }
}

package com.weeklyreport.backend.controller;

import com.weeklyreport.backend.dto.ForgotPasswordRequest;
import com.weeklyreport.backend.dto.MessageResponse;
import com.weeklyreport.backend.dto.ResetPasswordRequest;
import com.weeklyreport.backend.service.PasswordResetService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class PasswordResetController {

    private static final String FORGOT_PASSWORD_MESSAGE =
            "If an account exists for that email, a password reset link has been sent";
    private static final String RESET_SUCCESS_MESSAGE =
            "Your password has been reset — you can now log in with your new password";

    private final PasswordResetService passwordResetService;

    public PasswordResetController(PasswordResetService passwordResetService) {
        this.passwordResetService = passwordResetService;
    }

    @PostMapping("/forgot-password")
    public MessageResponse forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        passwordResetService.requestReset(request.email());
        return new MessageResponse(FORGOT_PASSWORD_MESSAGE);
    }

    @PostMapping("/reset-password")
    public MessageResponse resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        passwordResetService.resetPassword(request.token(), request.newPassword());
        return new MessageResponse(RESET_SUCCESS_MESSAGE);
    }
}

package com.weeklyreport.backend.service;

import com.weeklyreport.backend.domain.User;
import com.weeklyreport.backend.domain.UserStatus;
import com.weeklyreport.backend.dto.LoginRequest;
import com.weeklyreport.backend.dto.LoginResponse;
import com.weeklyreport.backend.dto.RegisterRequest;
import com.weeklyreport.backend.dto.RegisterResponse;
import com.weeklyreport.backend.exception.AccountPendingApprovalException;
import com.weeklyreport.backend.exception.InvalidCredentialsException;
import com.weeklyreport.backend.repository.UserRepository;
import com.weeklyreport.backend.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Self-service identity operations: account registration and password login. */
@Service
public class AuthService {

    private static final String REGISTRATION_MESSAGE =
            "Registration submitted — your account is pending admin approval";

    private final UserRegistrar userRegistrar;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(
            UserRegistrar userRegistrar,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService) {
        this.userRegistrar = userRegistrar;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        userRegistrar.create(request.name(), request.email(), request.password(), UserStatus.PENDING, null);
        return new RegisterResponse(REGISTRATION_MESSAGE);
    }

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        User user = userRepository
                .findByEmail(request.email())
                .filter(candidate -> passwordEncoder.matches(request.password(), candidate.getPasswordHash()))
                .orElseThrow(InvalidCredentialsException::new);

        if (user.getStatus() == UserStatus.PENDING) {
            throw new AccountPendingApprovalException();
        }
        if (user.getStatus() != UserStatus.ACTIVE) {
            // A removed account behaves exactly like one that never existed.
            throw new InvalidCredentialsException();
        }

        String token = jwtService.generateToken(user);
        return new LoginResponse(
                token, new LoginResponse.UserSummary(user.getId(), user.getName(), user.getRole()));
    }
}

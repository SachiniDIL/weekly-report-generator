package com.weeklyreport.backend.service;

import com.weeklyreport.backend.domain.User;
import com.weeklyreport.backend.domain.UserStatus;
import com.weeklyreport.backend.dto.LoginRequest;
import com.weeklyreport.backend.dto.LoginResponse;
import com.weeklyreport.backend.dto.RegisterRequest;
import com.weeklyreport.backend.dto.RegisterResponse;
import com.weeklyreport.backend.exception.AccountPendingApprovalException;
import com.weeklyreport.backend.exception.EmailAlreadyUsedException;
import com.weeklyreport.backend.exception.InvalidCredentialsException;
import com.weeklyreport.backend.repository.UserRepository;
import com.weeklyreport.backend.security.JwtService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Self-service identity operations: account registration and password login. */
@Service
public class AuthService {

    private static final String REGISTRATION_MESSAGE =
            "Registration submitted — your account is pending admin approval";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(
            UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        if (userRepository.findByEmail(request.email()).isPresent()) {
            throw new EmailAlreadyUsedException();
        }

        User user = new User();
        user.setName(request.name());
        user.setEmail(request.email());
        user.setPasswordHash(passwordEncoder.encode(request.password()));

        try {
            userRepository.saveAndFlush(user);
        } catch (DataIntegrityViolationException raceWithConcurrentRegistration) {
            throw new EmailAlreadyUsedException();
        }

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

        String token = jwtService.generateToken(user);
        return new LoginResponse(
                token, new LoginResponse.UserSummary(user.getId(), user.getName(), user.getRole()));
    }
}

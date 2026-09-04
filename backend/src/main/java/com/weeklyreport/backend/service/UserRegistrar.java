package com.weeklyreport.backend.service;

import com.weeklyreport.backend.domain.Role;
import com.weeklyreport.backend.domain.User;
import com.weeklyreport.backend.domain.UserStatus;
import com.weeklyreport.backend.exception.EmailAlreadyUsedException;
import com.weeklyreport.backend.repository.UserRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Creates and persists new user accounts with a hashed password and a unique email.
 * Shared by public self-registration and admin direct-create so the uniqueness handling
 * lives in exactly one place; the caller decides the initial status and role.
 */
@Component
class UserRegistrar {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    UserRegistrar(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    User create(String name, String email, String rawPassword, UserStatus status, Role role) {
        if (userRepository.findByEmail(email).isPresent()) {
            throw new EmailAlreadyUsedException();
        }

        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(rawPassword));
        user.setStatus(status);
        user.setRole(role);

        try {
            return userRepository.saveAndFlush(user);
        } catch (DataIntegrityViolationException raceWithConcurrentCreation) {
            throw new EmailAlreadyUsedException();
        }
    }
}

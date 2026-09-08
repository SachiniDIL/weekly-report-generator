package com.weeklyreport.backend.service;

import com.weeklyreport.backend.domain.Role;
import com.weeklyreport.backend.domain.User;
import com.weeklyreport.backend.domain.UserStatus;
import com.weeklyreport.backend.dto.AdminCreateUserRequest;
import com.weeklyreport.backend.dto.AdminUserView;
import com.weeklyreport.backend.dto.ApproveUserRequest;
import com.weeklyreport.backend.dto.ChangeRoleRequest;
import com.weeklyreport.backend.exception.AdminNotRemovableException;
import com.weeklyreport.backend.exception.InvalidUserStateException;
import com.weeklyreport.backend.exception.LastAdminException;
import com.weeklyreport.backend.exception.UserNotFoundException;
import com.weeklyreport.backend.repository.UserRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Admin-only user provisioning and lifecycle — distinct from self-service {@code AuthService}. */
@Service
public class AdminUserService {

    private static final Sort NEWEST_FIRST = Sort.by(Sort.Direction.DESC, "createdAt");

    private final UserRepository userRepository;
    private final UserRegistrar userRegistrar;

    public AdminUserService(UserRepository userRepository, UserRegistrar userRegistrar) {
        this.userRepository = userRepository;
        this.userRegistrar = userRegistrar;
    }

    @Transactional(readOnly = true)
    public List<AdminUserView> listUsers(Optional<UserStatus> statusFilter) {
        List<User> users = statusFilter
                .map(status -> userRepository.findByStatus(status, NEWEST_FIRST))
                .orElseGet(() -> userRepository.findAll(NEWEST_FIRST));
        return users.stream().map(AdminUserView::from).toList();
    }

    @Transactional
    public AdminUserView createUser(AdminCreateUserRequest request) {
        User user = userRegistrar.create(
                request.name(), request.email(), request.password(), UserStatus.ACTIVE, request.role());
        return AdminUserView.from(user);
    }

    @Transactional
    public AdminUserView approveUser(long id, ApproveUserRequest request) {
        User user = getUser(id);
        if (user.getStatus() != UserStatus.PENDING) {
            throw new InvalidUserStateException("Only a pending account can be approved");
        }
        user.setStatus(UserStatus.ACTIVE);
        user.setRole(request.role());
        return AdminUserView.from(user);
    }

    @Transactional
    public void removeUser(long id) {
        User user = getUser(id);
        if (user.getRole() == Role.ADMIN) {
            throw new AdminNotRemovableException();
        }
        switch (user.getStatus()) {
            case PENDING -> userRepository.delete(user);
            case ACTIVE -> {
                user.setStatus(UserStatus.REMOVED);
                // Invalidate every token already issued to this user, same lever as password reset.
                user.setTokenVersion(user.getTokenVersion() + 1);
            }
            case REMOVED -> throw new InvalidUserStateException("Account is already removed");
        }
    }

    @Transactional
    public AdminUserView changeRole(long id, ChangeRoleRequest request) {
        User user = getUser(id);
        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new InvalidUserStateException("Only an active account's role can be changed");
        }
        if (isDemotingLastAdmin(user, request.role())) {
            throw new LastAdminException();
        }
        user.setRole(request.role());
        return AdminUserView.from(user);
    }

    /** The system must always keep at least one active admin. */
    private boolean isDemotingLastAdmin(User user, Role newRole) {
        return user.getRole() == Role.ADMIN
                && newRole != Role.ADMIN
                && userRepository.countByStatusAndRole(UserStatus.ACTIVE, Role.ADMIN) <= 1;
    }

    private User getUser(long id) {
        return userRepository.findById(id).orElseThrow(() -> new UserNotFoundException(id));
    }
}

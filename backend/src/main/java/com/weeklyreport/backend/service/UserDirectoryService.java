package com.weeklyreport.backend.service;

import com.weeklyreport.backend.domain.Role;
import com.weeklyreport.backend.domain.User;
import com.weeklyreport.backend.domain.UserStatus;
import com.weeklyreport.backend.dto.UserBasicView;
import com.weeklyreport.backend.repository.UserRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Read-only directory of active users for assignment pickers — separate from the admin-only
 * lifecycle logic in {@link AdminUserService}. PENDING and REMOVED accounts are never listed.
 */
@Service
public class UserDirectoryService {

    private static final Sort BY_NAME = Sort.by("name").ascending();

    private final UserRepository userRepository;

    public UserDirectoryService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<UserBasicView> listActiveUsers(Optional<Role> roleFilter) {
        List<User> users = roleFilter
                .map(role -> userRepository.findByStatusAndRole(UserStatus.ACTIVE, role, BY_NAME))
                .orElseGet(() -> userRepository.findByStatus(UserStatus.ACTIVE, BY_NAME));
        return users.stream().map(UserBasicView::from).toList();
    }
}

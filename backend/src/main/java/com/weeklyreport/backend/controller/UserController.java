package com.weeklyreport.backend.controller;

import com.weeklyreport.backend.domain.Role;
import com.weeklyreport.backend.dto.UserBasicView;
import com.weeklyreport.backend.service.UserDirectoryService;
import java.util.List;
import java.util.Optional;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Basic active-user directory — Manager and Admin can read it (e.g. to pick project members). */
@RestController
@RequestMapping("/users")
@PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
public class UserController {

    private final UserDirectoryService userDirectoryService;

    public UserController(UserDirectoryService userDirectoryService) {
        this.userDirectoryService = userDirectoryService;
    }

    @GetMapping
    public List<UserBasicView> listUsers(@RequestParam(required = false) Role role) {
        return userDirectoryService.listActiveUsers(Optional.ofNullable(role));
    }
}

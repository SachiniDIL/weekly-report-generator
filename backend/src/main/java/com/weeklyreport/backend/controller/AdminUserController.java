package com.weeklyreport.backend.controller;

import com.weeklyreport.backend.domain.UserStatus;
import com.weeklyreport.backend.dto.AdminCreateUserRequest;
import com.weeklyreport.backend.dto.AdminUserView;
import com.weeklyreport.backend.dto.ApproveUserRequest;
import com.weeklyreport.backend.dto.ChangeRoleRequest;
import com.weeklyreport.backend.service.AdminUserService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/users")
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

    private final AdminUserService adminUserService;

    public AdminUserController(AdminUserService adminUserService) {
        this.adminUserService = adminUserService;
    }

    @GetMapping
    public List<AdminUserView> listUsers(@RequestParam(required = false) UserStatus status) {
        return adminUserService.listUsers(Optional.ofNullable(status));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AdminUserView createUser(@Valid @RequestBody AdminCreateUserRequest request) {
        return adminUserService.createUser(request);
    }

    @PostMapping("/{id}/approve")
    public AdminUserView approveUser(
            @PathVariable long id, @Valid @RequestBody ApproveUserRequest request) {
        return adminUserService.approveUser(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeUser(@PathVariable long id) {
        adminUserService.removeUser(id);
    }

    @PatchMapping("/{id}/role")
    public AdminUserView changeRole(
            @PathVariable long id, @Valid @RequestBody ChangeRoleRequest request) {
        return adminUserService.changeRole(id, request);
    }
}

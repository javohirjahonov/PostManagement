package org.example.postproject.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.postproject.api.dtos.request.UserDetailsForFront;
import org.example.postproject.api.dtos.request.UserUpdateRequest;
import org.example.postproject.api.dtos.response.StandardResponse;
import org.example.postproject.service.user.UserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    @PutMapping("/update-profile")
    @PreAuthorize("isAuthenticated()") // Ensure user is authenticated
    public StandardResponse<UserDetailsForFront> updateProfile(
            @Valid @RequestBody UserUpdateRequest updateRequest,
            Principal principal
    ) {
        if (principal == null) {
            throw new SecurityException("Unauthorized access");
        }
        return userService.updateProfile(updateRequest, principal);
    }

    @GetMapping("/get-all-users")
    @PreAuthorize("hasAuthority('ADMIN')") // Allow only admins to access all users
    public StandardResponse<List<UserDetailsForFront>> getAllUsers(
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "10") int size
    ) {
        return userService.getAll(page, size);
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()") // Ensure user is authenticated
    public StandardResponse<UserDetailsForFront> getCurrentUser(Principal principal) {
        if (principal == null) {
            throw new SecurityException("Unauthorized access");
        }
        return userService.getMeByToken(principal.getName());
    }
}

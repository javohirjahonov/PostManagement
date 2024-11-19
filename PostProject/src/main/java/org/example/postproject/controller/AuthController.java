package org.example.postproject.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.postproject.api.dtos.request.LoginRequestDto;
import org.example.postproject.api.dtos.request.UpdatePasswordDto;
import org.example.postproject.api.dtos.request.UserRequestDto;
import org.example.postproject.api.dtos.request.VerifyCodeDto;
import org.example.postproject.api.dtos.response.JwtResponse;
import org.example.postproject.api.dtos.response.StandardResponse;
import org.example.postproject.service.user.UserService;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @PostMapping("/register")
    public StandardResponse<JwtResponse> register(
            @Valid @RequestBody UserRequestDto userDto,
            BindingResult bindingResult
    ) {
        if (bindingResult.hasErrors()) {
            throw new IllegalArgumentException(
                    "Invalid registration request: " + bindingResult.getAllErrors());
        }
        return userService.save(userDto);
    }

    @PostMapping("/login")
    public StandardResponse<JwtResponse> login(@Valid @RequestBody LoginRequestDto loginDto) {
        return userService.signIn(loginDto);
    }

    @GetMapping("/verify-email")
    public StandardResponse<String> verifyEmail(
            @RequestParam String code,
            Principal principal
    ) {
        if (principal == null) {
            throw new SecurityException("Unauthorized access");
        }
        return userService.verify(principal, code);
    }

    @PostMapping("/send-verification-code")
    public StandardResponse<String> sendVerificationCode(Principal principal) {
        if (principal == null) {
            throw new SecurityException("Unauthorized access");
        }
        return userService.sendVerificationCode(principal.getName());
    }

    @GetMapping("/access-token")
    public StandardResponse<JwtResponse> getAccessToken(Principal principal) {
        if (principal == null) {
            throw new SecurityException("Unauthorized access");
        }
        return userService.getNewAccessToken(principal);
    }

    @PostMapping("/refresh-token")
    public StandardResponse<JwtResponse> refreshToken(Principal principal) {
        if (principal == null) {
            throw new SecurityException("Unauthorized access");
        }
        return userService.getNewAccessToken(principal);
    }

    @PostMapping("/forgot-password")
    public StandardResponse<String> forgotPassword(@RequestParam String email) {
        return userService.forgottenPassword(email);
    }

    @PostMapping("/password/verify-code")
    public StandardResponse<String> verifyCodeForPasswordUpdate(
            @Valid @RequestBody VerifyCodeDto verifyCodeDto
    ) {
        return userService.verifyPasswordForUpdatePassword(verifyCodeDto);
    }

    @PutMapping("/password/update")
    public StandardResponse<String> updatePassword(
            @Valid @RequestBody UpdatePasswordDto updatePasswordDto
    ) {
        return userService.updatePassword(updatePasswordDto);
    }
}

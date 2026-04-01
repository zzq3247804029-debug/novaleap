package com.novaleap.api.controller;

import com.novaleap.api.common.Result;
import com.novaleap.api.dto.LoginRequest;
import com.novaleap.api.dto.PasswordResetRequest;
import com.novaleap.api.dto.ProfileUpdateRequest;
import com.novaleap.api.dto.RegisterRequest;
import com.novaleap.api.module.auth.service.AuthApplicationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthApplicationService authApplicationService;

    public AuthController(AuthApplicationService authApplicationService) {
        this.authApplicationService = authApplicationService;
    }

    @PostMapping("/login")
    public Result<Map<String, Object>> login(
            @RequestBody @Valid LoginRequest request,
            HttpServletRequest httpServletRequest
    ) {
        return Result.success(authApplicationService.login(request, httpServletRequest));
    }

    @PostMapping("/guest")
    public Result<Map<String, Object>> guestLogin() {
        return Result.success(authApplicationService.guestLogin());
    }

    @PostMapping("/register")
    public Result<Map<String, Object>> register(
            @RequestBody @Valid RegisterRequest request,
            HttpServletRequest httpServletRequest
    ) {
        return Result.success(authApplicationService.register(request, httpServletRequest));
    }

    @PostMapping("/logout")
    public Result<Void> logout() {
        return Result.success();
    }

    @PostMapping("/password/reset")
    public Result<Void> resetPassword(
            @RequestBody @Valid PasswordResetRequest request,
            HttpServletRequest httpServletRequest
    ) {
        authApplicationService.resetPassword(request, httpServletRequest);
        return Result.success();
    }

    @PostMapping("/email/send-code")
    public Result<Void> sendEmailCode(@RequestBody Map<String, String> request, HttpServletRequest httpServletRequest) {
        String email = request.get("email");
        String type = request.getOrDefault("type", "register");
        authApplicationService.sendEmailCode(email, type, httpServletRequest);
        return Result.success();
    }

    @GetMapping("/profile")
    public Result<Map<String, Object>> getProfile(Authentication authentication) {
        return Result.success(authApplicationService.getProfile(authentication));
    }

    @PutMapping("/profile")
    public Result<Map<String, Object>> updateProfile(
            Authentication authentication,
            @RequestBody @Valid ProfileUpdateRequest request
    ) {
        return Result.success(authApplicationService.updateProfile(authentication, request));
    }

    @GetMapping("/streak")
    public Result<Map<String, Object>> getCheckinStreak(Authentication authentication) {
        return Result.success(authApplicationService.getCheckinStreak(authentication));
    }
}

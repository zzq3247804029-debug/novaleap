package com.novaleap.api.module.auth.service;

import com.novaleap.api.dto.LoginRequest;
import com.novaleap.api.dto.PasswordResetRequest;
import com.novaleap.api.dto.ProfileUpdateRequest;
import com.novaleap.api.dto.RegisterRequest;
import com.novaleap.api.module.system.security.CurrentUser;
import com.novaleap.api.module.system.security.CurrentUserService;
import com.novaleap.api.module.system.web.ClientRequestService;
import com.novaleap.api.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Map;

@Service
public class AuthApplicationService {

    private final AuthService authService;
    private final CurrentUserService currentUserService;
    private final ClientRequestService clientRequestService;
    private final EmailService emailService;

    public AuthApplicationService(
            AuthService authService,
            CurrentUserService currentUserService,
            ClientRequestService clientRequestService,
            EmailService emailService
    ) {
        this.authService = authService;
        this.currentUserService = currentUserService;
        this.clientRequestService = clientRequestService;
        this.emailService = emailService;
    }

    public Map<String, Object> login(LoginRequest request, HttpServletRequest httpRequest) {
        String clientIp = clientRequestService.resolveClientIp(httpRequest);
        if (request.isCodeLogin()) {
            String email = emailService.normalizeEmail(request.getUsername());
            emailService.assertValidEmail(email);
            String emailCode = request.getEmailCode();
            if (!StringUtils.hasText(emailCode)) {
                throw new IllegalArgumentException("验证码不能为空");
            }
            if (!emailService.verifyCode(email, "login", emailCode)) {
                throw new IllegalArgumentException("验证码错误或已过期");
            }
            Map<String, Object> result = authService.loginByEmailCode(email, clientIp, request.getTurnstileToken());
            emailService.consumeCode(email, "login");
            return result;
        }
        return authService.login(
                request.getUsername(),
                request.getPassword(),
                clientIp,
                request.getTurnstileToken()
        );
    }

    public Map<String, Object> guestLogin() {
        return authService.guestLogin();
    }

    public Map<String, Object> register(RegisterRequest request, HttpServletRequest httpRequest) {
        String email = emailService.normalizeEmail(request.getUsername());
        if (!emailService.verifyCode(email, "register", request.getEmailCode())) {
            throw new IllegalArgumentException("验证码错误或已过期");
        }

        Map<String, Object> result = authService.register(
                email,
                request.getPassword(),
                request.getConfirmPassword(),
                request.getNickname(),
                request.getConsent(),
                clientRequestService.resolveClientIp(httpRequest),
                request.getTurnstileToken()
        );
        emailService.consumeCode(email, "register");
        return result;
    }

    public void sendEmailCode(String email, String type, HttpServletRequest httpRequest) {
        String normalizedEmail = emailService.normalizeEmail(email);
        emailService.assertValidEmail(normalizedEmail);

        String normalizedType = "reset".equalsIgnoreCase(type) ? "reset" : "register";
        if ("login".equalsIgnoreCase(type)) {
            normalizedType = "login";
        }
        boolean accountExists = authService.emailExists(normalizedEmail);

        if ("register".equals(normalizedType) && accountExists) {
            throw new IllegalArgumentException("该邮箱已注册");
        }
        if (("reset".equals(normalizedType) || "login".equals(normalizedType)) && !accountExists) {
            throw new IllegalArgumentException("该邮箱尚未注册");
        }

        String clientIp = clientRequestService.resolveClientIp(httpRequest);
        emailService.sendVerificationCode(normalizedEmail, normalizedType, clientIp);
    }

    public void resetPassword(PasswordResetRequest request, HttpServletRequest httpRequest) {
        String email = emailService.normalizeEmail(request.getUsername());
        if (!emailService.verifyCode(email, "reset", request.getEmailCode())) {
            throw new IllegalArgumentException("验证码错误或已过期");
        }

        authService.resetPassword(
                email,
                request.getNewPassword(),
                clientRequestService.resolveClientIp(httpRequest),
                resolveUserAgent(httpRequest)
        );
        emailService.consumeCode(email, "reset");
    }

    public Map<String, Object> getProfile(Authentication authentication) {
        CurrentUser currentUser = requireAuthenticatedUser(authentication);
        return authService.getProfile(currentUser.safeUsername(), currentUser.role());
    }

    public Map<String, Object> updateProfile(Authentication authentication, ProfileUpdateRequest request) {
        CurrentUser currentUser = requireAuthenticatedUser(authentication);
        return authService.updateProfile(
                currentUser.safeUsername(),
                currentUser.role(),
                request.getNickname(),
                request.getPassword(),
                request.getAvatar()
        );
    }

    public Map<String, Object> getCheckinStreak(Authentication authentication) {
        CurrentUser currentUser = requireAuthenticatedUser(authentication);
        return authService.getCheckinInfo(currentUser.safeUsername(), currentUser.role());
    }

    private CurrentUser requireAuthenticatedUser(Authentication authentication) {
        currentUserService.requireUsername(authentication);
        return currentUserService.current(authentication);
    }

    private String resolveUserAgent(HttpServletRequest request) {
        return request == null ? "" : request.getHeader("User-Agent");
    }
}

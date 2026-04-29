package com.vserv.features.auth.controller;

import com.vserv.features.auth.mapper.AuthMapper;
import com.vserv.features.auth.service.AuthService;
import com.vserv.core.util.SecurityUtils;
import com.vserv.features.auth.dto.ForgotPasswordRequestDto;
import com.vserv.features.auth.dto.LoginRequest;
import com.vserv.features.auth.dto.LoginResponse;
import com.vserv.features.auth.dto.ResetPasswordRequest;
import com.vserv.features.auth.dto.RefreshTokenRequest;
import com.vserv.features.auth.dto.RegisterRequest;
import com.vserv.features.auth.dto.VerifyOtpRequest;
import com.vserv.entity.AppUser;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

	private final AuthService authService;
	private final SecurityUtils securityUtils;

	public AuthController(AuthService authService, SecurityUtils securityUtils) {
		this.authService = authService;
		this.securityUtils = securityUtils;
	}

	@PostMapping("/login")
	public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest req) {
		return ResponseEntity.status(HttpStatus.OK).body(authService.login(req));
	}

	@PostMapping("/logout")
	public ResponseEntity<Map<String, String>> logout(
			@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization,
			@RequestBody(required = false) RefreshTokenRequest request) {
		String accessToken = authorization != null && authorization.startsWith("Bearer ") ? authorization.substring(7)
				: null;
		authService.logout(accessToken, request != null ? request.getRefreshToken() : null);
		return ResponseEntity.status(HttpStatus.OK).body(Map.of("message", "Logged out successfully"));
	}

	@GetMapping("/me")
	public ResponseEntity<LoginResponse> getAuthenticatedUser() {
		AppUser user = securityUtils.requireCurrentUser();
		return ResponseEntity.status(HttpStatus.OK).body(AuthMapper.toLoginResponse(user));
	}

	@PostMapping("/register")
	public ResponseEntity<Map<String, String>> register(@Valid @RequestBody RegisterRequest req) {
		authService.register(req);
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(Map.of("message", "Account created successfully. Please log in."));
	}

	@PostMapping("/forgot-password/request")
	public ResponseEntity<Map<String, String>> requestForgotPassword(@Valid @RequestBody ForgotPasswordRequestDto req) {
		authService.requestOtp(req.getEmail());
		return ResponseEntity.status(HttpStatus.OK)
				.body(Map.of("message", "If this email is registered, an OTP has been sent."));
	}

	@PostMapping("/forgot-password/verify")
	public ResponseEntity<Map<String, String>> verifyForgotPasswordOtp(@Valid @RequestBody VerifyOtpRequest req) {
		return ResponseEntity.status(HttpStatus.OK)
				.body(Map.of("resetToken", authService.verifyOtp(req.getEmail(), req.getOtp())));
	}

	@PostMapping("/forgot-password/reset")
	public ResponseEntity<Map<String, String>> resetForgotPassword(@Valid @RequestBody ResetPasswordRequest req) {
		authService.resetPassword(req.getResetToken(), req.getNewPassword());
		return ResponseEntity.status(HttpStatus.OK)
				.body(Map.of("message", "Password reset successfully. Please log in."));
	}

	@PostMapping("/refresh")
	public ResponseEntity<LoginResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
		return ResponseEntity.status(HttpStatus.OK).body(authService.refresh(request));
	}
}

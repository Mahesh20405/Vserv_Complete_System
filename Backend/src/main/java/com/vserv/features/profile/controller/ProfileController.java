package com.vserv.features.profile.controller;

import com.vserv.core.exception.NotFoundException;
import com.vserv.core.util.SecurityUtils;
import com.vserv.entity.AppUser;
import com.vserv.features.auth.service.AuthService;
import com.vserv.features.profile.dto.CompletePasswordChangeRequest;
import com.vserv.features.profile.dto.UpdateProfileRequest;
import com.vserv.features.profile.dto.VerifyProfileOtpRequest;
import com.vserv.features.user.service.UserService;
import com.vserv.features.user.dto.UserDto;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {

	private final UserService userService;
	private final SecurityUtils securityUtils;
	private final AuthService authService;

	public ProfileController(UserService userService, SecurityUtils securityUtils, AuthService authService) {
		this.userService = userService;
		this.securityUtils = securityUtils;
		this.authService = authService;
	}

	@GetMapping
	public ResponseEntity<UserDto> getProfile() {
		AppUser me = securityUtils.requireCurrentUser();
		AppUser user = userService.findById(me.getUserId()).orElseThrow(() -> new NotFoundException("User not found."));
		return ResponseEntity.status(HttpStatus.OK)
				.body(UserDto.from(user, userService.findAdvisorByUserId(user.getUserId()).orElse(null)));
	}

	@PutMapping
	public ResponseEntity<UserDto> updateProfile(@Valid @RequestBody UpdateProfileRequest req) {
		AppUser me = securityUtils.requireCurrentUser();
		AppUser updated = userService.updateProfile(me.getUserId(), req.getFullName(), req.getPhone());
		return ResponseEntity.status(HttpStatus.OK)
				.body(UserDto.from(updated, userService.findAdvisorByUserId(updated.getUserId()).orElse(null)));
	}

	@PostMapping("/change-password/request-otp")
	public ResponseEntity<Map<String, String>> requestPasswordChangeOtp() {
		AppUser me = securityUtils.requireCurrentUser();
		String fallbackOtp = authService.requestProfilePasswordOtp(me.getUserId());
		Map<String, String> response = new LinkedHashMap<>();
		if (fallbackOtp != null) {
			response.put("message", "Email delivery is unavailable right now. Use the default OTP below.");
			response.put("fallbackOtp", fallbackOtp);
		} else {
			response.put("message", "OTP sent to your registered email address.");
		}
		return ResponseEntity.status(HttpStatus.OK).body(response);
	}

	@PostMapping("/change-password/verify-otp")
	public ResponseEntity<Map<String, String>> verifyPasswordChangeOtp(
			@Valid @RequestBody VerifyProfileOtpRequest req) {
		AppUser me = securityUtils.requireCurrentUser();
		return ResponseEntity.status(HttpStatus.OK)
				.body(Map.of("resetToken", authService.verifyProfilePasswordOtp(me.getUserId(), req.getOtp())));
	}

	@PostMapping("/change-password")
	public ResponseEntity<Map<String, String>> changePassword(@Valid @RequestBody CompletePasswordChangeRequest req) {
		AppUser me = securityUtils.requireCurrentUser();
		authService.changeProfilePassword(me.getUserId(), req.getResetToken(), req.getNewPassword());
		return ResponseEntity.status(HttpStatus.OK).body(Map.of("message", "Password changed successfully."));
	}
}

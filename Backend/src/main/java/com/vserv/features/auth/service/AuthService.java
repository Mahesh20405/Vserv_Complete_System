package com.vserv.features.auth.service;

import com.vserv.features.auth.dto.LoginRequest;
import com.vserv.features.auth.dto.LoginResponse;
import com.vserv.features.auth.dto.RefreshTokenRequest;
import com.vserv.features.auth.dto.RegisterRequest;

public interface AuthService {
	LoginResponse login(LoginRequest req);

	void register(RegisterRequest req);

	LoginResponse refresh(RefreshTokenRequest request);

	void logout(String accessToken, String refreshToken);

	void requestOtp(String email);

	String verifyOtp(String email, String otp);

	void resetPassword(String resetToken, String newPassword);

	String requestProfilePasswordOtp(Integer userId);

	String verifyProfilePasswordOtp(Integer userId, String otp);

	void changeProfilePassword(Integer userId, String resetToken, String newPassword);
}

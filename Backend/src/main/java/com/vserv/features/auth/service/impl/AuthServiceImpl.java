package com.vserv.features.auth.service.impl;

import com.vserv.entity.PasswordResetToken;
import com.vserv.features.auth.repository.PasswordResetTokenRepository;
import com.vserv.features.auth.repository.RevokedTokenRepository;
import com.vserv.features.auth.service.EmailService;
import com.vserv.features.auth.service.AuthService;
import com.vserv.entity.RevokedToken;
import com.vserv.core.config.JwtUtil;
import com.vserv.core.config.VservUserDetails;
import com.vserv.core.exception.BusinessException;
import com.vserv.core.exception.ConflictException;
import com.vserv.core.exception.ForbiddenException;
import com.vserv.features.auth.dto.LoginRequest;
import com.vserv.features.auth.dto.LoginResponse;
import com.vserv.features.auth.dto.RefreshTokenRequest;
import com.vserv.features.auth.dto.RegisterRequest;
import com.vserv.entity.AppUser;
import com.vserv.features.user.service.UserService;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthServiceImpl implements AuthService {
	private static final Logger log = LoggerFactory.getLogger(AuthServiceImpl.class);

	private static final SecureRandom SECURE_RANDOM = new SecureRandom();
	private static final String DEFAULT_FALLBACK_OTP = "123456";

	private final AuthenticationManager authManager;
	private final UserService userService;
	private final JwtUtil jwtUtil;
	private final RevokedTokenRepository revokedTokenRepository;
	private final PasswordResetTokenRepository passwordResetTokenRepository;
	private final PasswordEncoder passwordEncoder;
	private final EmailService emailService;

	public AuthServiceImpl(AuthenticationManager authManager, UserService userService, JwtUtil jwtUtil,
			RevokedTokenRepository revokedTokenRepository, PasswordResetTokenRepository passwordResetTokenRepository,
			PasswordEncoder passwordEncoder, EmailService emailService) {
		this.authManager = authManager;
		this.userService = userService;
		this.jwtUtil = jwtUtil;
		this.revokedTokenRepository = revokedTokenRepository;
		this.passwordResetTokenRepository = passwordResetTokenRepository;
		this.passwordEncoder = passwordEncoder;
		this.emailService = emailService;
	}

	public LoginResponse login(LoginRequest req) {
		log.info("Login attempt for email={}", req.getEmail());
		String normalizedEmail = normalizeEmail(req.getEmail());
		Authentication auth;
		try {
			auth = authManager
					.authenticate(new UsernamePasswordAuthenticationToken(normalizedEmail, req.getPassword()));
		} catch (DisabledException ex) {
			log.warn("Login blocked – account inactive for email={}", normalizedEmail);
			throw new ForbiddenException("Your account is inactive. Please contact admin.");
		}
		VservUserDetails userDetails = (VservUserDetails) auth.getPrincipal();

		AppUser user = userService.updateLastLogin(normalizedEmail);
		log.info("Login successful for email={}", normalizedEmail);
		return new LoginResponse(user.getUserId(), user.getFullName(), user.getEmail(), userDetails.getRoleName(),
				jwtUtil.generateAccessToken(userDetails), jwtUtil.generateRefreshToken(userDetails), "Bearer");
	}

	public void register(RegisterRequest req) {
		log.info("Registering new customer email={}", req.getEmail());
		if (userService.emailExists(req.getEmail())) {
			log.warn("Registration rejected – email already exists: {}", req.getEmail());
			throw new ConflictException("Email is already registered.");
		}
		if (req.getPhone() != null && !req.getPhone().isBlank() && userService.phoneExists(req.getPhone().trim())) {
			throw new ConflictException("Mobile number is already registered.");
		}
		userService.registerCustomer(req.getFullName().trim(), req.getEmail().trim().toLowerCase(), req.getPassword(),
				req.getPhone(), req.getGender());
	}

	public LoginResponse refresh(RefreshTokenRequest request) {
		log.info("Token refresh requested");
		String refreshToken = request.getRefreshToken();
		if (revokedTokenRepository.existsByToken(refreshToken) || !jwtUtil.isRefreshToken(refreshToken)) {
			log.warn("Invalid or revoked refresh token presented");
			throw new BusinessException("Invalid or expired refresh token.");
		}

		AppUser user = userService.requireActiveUserByEmail(jwtUtil.extractUsername(refreshToken));
		VservUserDetails userDetails = new VservUserDetails(user);

		return new LoginResponse(user.getUserId(), user.getFullName(), user.getEmail(), userDetails.getRoleName(),
				jwtUtil.generateAccessToken(userDetails), refreshToken, "Bearer");
	}

	@Transactional
	public void logout(String accessToken, String refreshToken) {
		log.info("Logout requested");
		revokeToken(accessToken);
		revokeToken(refreshToken);
	}

	@Transactional
	public void requestOtp(String email) {
		log.info("OTP requested for email={}", email);
		if (email == null || email.isBlank()) {
			return;
		}

		Optional<AppUser> userOptional = userService.findActiveByEmail(email);
		if (userOptional.isEmpty()) {
			return;
		}

		AppUser user = userOptional.get();
		invalidateActiveTokens(user);
		dispatchOtp(user);
	}

	@Transactional
	public String requestProfilePasswordOtp(Integer userId) {
		AppUser user = userService.findById(userId).orElseThrow(() -> new BusinessException("Invalid request."));
		invalidateActiveTokens(user);
		return dispatchOtp(user);
	}

	private String dispatchOtp(AppUser user) {
		String otp = String.valueOf(100000 + SECURE_RANDOM.nextInt(900000));
		boolean emailDelivered = emailService.sendOtpEmail(user.getEmail(), user.getFullName(), otp);
		String effectiveOtp = emailDelivered ? otp : DEFAULT_FALLBACK_OTP;
		PasswordResetToken record = new PasswordResetToken();
		record.setUser(user);
		record.setOtpHash(passwordEncoder.encode(effectiveOtp));
		record.setOtpVerified(false);
		record.setUsed(false);
		record.setToken(null);
		record.setExpiresAt(LocalDateTime.now().plusMinutes(10));
		passwordResetTokenRepository.save(record);
		if (!emailDelivered) {
			log.warn("OTP email fallback activated for userId={} email={}. Default OTP={}", user.getUserId(),
					user.getEmail(), DEFAULT_FALLBACK_OTP);
			return DEFAULT_FALLBACK_OTP;
		}
		return null;
	}

	@Transactional
	public String verifyOtp(String email, String otp) {
		log.info("OTP verification attempt for email={}", email);
		AppUser user = userService.findActiveByEmail(email)
				.orElseThrow(() -> new BusinessException("Invalid request."));

		PasswordResetToken record = passwordResetTokenRepository
				.findTopByUserAndUsedFalseAndOtpVerifiedFalseAndExpiresAtAfterOrderByCreatedAtDesc(user,
						LocalDateTime.now())
				.orElseThrow(() -> new BusinessException("OTP expired or invalid."));

		if (!passwordEncoder.matches(otp, record.getOtpHash())) {
			log.warn("Incorrect OTP for email={}", email);
			throw new BusinessException("Incorrect OTP.");
		}

		record.setOtpVerified(true);
		record.setToken(UUID.randomUUID().toString());
		record.setExpiresAt(LocalDateTime.now().plusMinutes(15));
		passwordResetTokenRepository.save(record);
		return record.getToken();
	}

	@Transactional
	public String verifyProfilePasswordOtp(Integer userId, String otp) {
		AppUser user = userService.findById(userId).orElseThrow(() -> new BusinessException("Invalid request."));
		PasswordResetToken record = passwordResetTokenRepository
				.findTopByUserAndUsedFalseAndOtpVerifiedFalseAndExpiresAtAfterOrderByCreatedAtDesc(user,
						LocalDateTime.now())
				.orElseThrow(() -> new BusinessException("OTP expired or invalid."));

		if (!passwordEncoder.matches(otp, record.getOtpHash())) {
			throw new BusinessException("Incorrect OTP.");
		}

		record.setOtpVerified(true);
		record.setToken(UUID.randomUUID().toString());
		record.setExpiresAt(LocalDateTime.now().plusMinutes(15));
		passwordResetTokenRepository.save(record);
		return record.getToken();
	}

	@Transactional
	public void resetPassword(String resetToken, String newPassword) {
		log.info("Password reset attempt with token");
		PasswordResetToken record = passwordResetTokenRepository.findByToken(resetToken)
				.orElseThrow(() -> new BusinessException("Invalid or expired reset link."));

		if (!Boolean.TRUE.equals(record.getOtpVerified()) || Boolean.TRUE.equals(record.getUsed())
				|| record.getExpiresAt() == null || !record.getExpiresAt().isAfter(LocalDateTime.now())) {
			log.warn("Password reset failed – session invalid or expired");
			throw new BusinessException("Reset session expired. Please start again.");
		}

		AppUser user = record.getUser();
		if (passwordEncoder.matches(newPassword, user.getPassword())) {
			throw new BusinessException("New password must be different.");
		}

		user.setPassword(passwordEncoder.encode(newPassword));
		log.info("Password reset successful for userId={}", user.getUserId());
		userService.save(user);
		record.setUsed(true);
		passwordResetTokenRepository.save(record);
	}

	@Transactional
	public void changeProfilePassword(Integer userId, String resetToken, String newPassword) {
		PasswordResetToken record = passwordResetTokenRepository.findByToken(resetToken)
				.orElseThrow(() -> new BusinessException("Invalid or expired reset session."));

		if (record.getUser() == null || !userId.equals(record.getUser().getUserId())) {
			throw new BusinessException("Invalid or expired reset session.");
		}

		if (!Boolean.TRUE.equals(record.getOtpVerified()) || Boolean.TRUE.equals(record.getUsed())
				|| record.getExpiresAt() == null || !record.getExpiresAt().isAfter(LocalDateTime.now())) {
			throw new BusinessException("Reset session expired. Please request a new OTP.");
		}

		AppUser user = record.getUser();
		if (passwordEncoder.matches(newPassword, user.getPassword())) {
			throw new BusinessException("New password must be different.");
		}

		user.setPassword(passwordEncoder.encode(newPassword));
		userService.save(user);
		record.setUsed(true);
		passwordResetTokenRepository.save(record);
	}

	private void revokeToken(String token) {
		if (token == null || token.isBlank() || revokedTokenRepository.existsByToken(token)) {
			return;
		}
		revokedTokenRepository.save(new RevokedToken(token));
	}

	private String normalizeEmail(String email) {
		return email == null ? null : email.trim().toLowerCase();
	}

	private void invalidateActiveTokens(AppUser user) {
		passwordResetTokenRepository.findByUserAndUsedFalse(user).ifPresent(existing -> {
			existing.setUsed(true);
			passwordResetTokenRepository.save(existing);
		});
	}
}

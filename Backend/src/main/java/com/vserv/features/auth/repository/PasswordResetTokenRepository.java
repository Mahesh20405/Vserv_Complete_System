package com.vserv.features.auth.repository;

import com.vserv.entity.AppUser;
import com.vserv.entity.PasswordResetToken;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Integer> {
	Optional<PasswordResetToken> findByToken(String token);

	Optional<PasswordResetToken> findByUserAndUsedFalse(AppUser user);

	Optional<PasswordResetToken> findTopByUserAndUsedFalseAndOtpVerifiedFalseAndExpiresAtAfterOrderByCreatedAtDesc(
			AppUser user, LocalDateTime now);

	void deleteByExpiresAtBefore(LocalDateTime now);
}

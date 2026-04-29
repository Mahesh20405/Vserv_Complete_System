package com.vserv.features.auth.repository;

import com.vserv.entity.RevokedToken;

import org.springframework.data.jpa.repository.JpaRepository;

public interface RevokedTokenRepository extends JpaRepository<RevokedToken, Long> {

	boolean existsByToken(String token);
}

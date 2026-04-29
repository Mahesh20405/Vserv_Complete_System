package com.vserv.core.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Base64;
import java.util.Date;
import java.util.Map;

@Component
public class JwtUtil {

	private final Key signingKey;
	private final long accessTokenExpirationMs;
	private final long refreshTokenExpirationMs;

	public JwtUtil(@Value("${jwt.secret}") String secret,
			@Value("${jwt.access-token-expiration-ms}") long accessTokenExpirationMs,
			@Value("${jwt.refresh-token-expiration-ms}") long refreshTokenExpirationMs) {
		this.signingKey = Keys.hmacShaKeyFor(Base64.getDecoder().decode(secret));
		this.accessTokenExpirationMs = accessTokenExpirationMs;
		this.refreshTokenExpirationMs = refreshTokenExpirationMs;
	}

	public String generateAccessToken(VservUserDetails userDetails) {
		return buildToken(userDetails.getUsername(), userDetails.getRoleName(), "access", accessTokenExpirationMs);
	}

	public String generateRefreshToken(VservUserDetails userDetails) {
		return buildToken(userDetails.getUsername(), userDetails.getRoleName(), "refresh", refreshTokenExpirationMs);
	}

	public String extractUsername(String token) {
		return extractAllClaims(token).getSubject();
	}

	public String extractRole(String token) {
		return extractAllClaims(token).get("role", String.class);
	}

	public String extractTokenType(String token) {
		return extractAllClaims(token).get("type", String.class);
	}

	public boolean isAccessToken(String token) {
		return "access".equals(extractTokenType(token));
	}

	public boolean isRefreshToken(String token) {
		return "refresh".equals(extractTokenType(token));
	}

	public boolean isTokenExpired(String token) {
		return extractAllClaims(token).getExpiration().before(new Date());
	}

	public boolean isTokenValid(String token, UserDetails userDetails) {
		return userDetails.getUsername().equals(extractUsername(token)) && !isTokenExpired(token);
	}

	private String buildToken(String subject, String role, String type, long expirationMs) {
		Date now = new Date();
		return Jwts.builder().setClaims(Map.of("role", role, "type", type)).setSubject(subject).setIssuedAt(now)
				.setExpiration(new Date(now.getTime() + expirationMs)).signWith(signingKey, SignatureAlgorithm.HS256)
				.compact();
	}

	private Claims extractAllClaims(String token) {
		return Jwts.parserBuilder().setSigningKey(signingKey).build().parseClaimsJws(token).getBody();
	}
}

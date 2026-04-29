package com.vserv.core.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vserv.features.auth.repository.RevokedTokenRepository;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	private final JwtUtil jwtUtil;
	private final VservUserDetailsService userDetailsService;
	private final RevokedTokenRepository revokedTokenRepository;
	private final ObjectMapper objectMapper;

	public JwtAuthenticationFilter(JwtUtil jwtUtil, VservUserDetailsService userDetailsService,
			RevokedTokenRepository revokedTokenRepository, ObjectMapper objectMapper) {
		this.jwtUtil = jwtUtil;
		this.userDetailsService = userDetailsService;
		this.revokedTokenRepository = revokedTokenRepository;
		this.objectMapper = objectMapper;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		String header = request.getHeader(HttpHeaders.AUTHORIZATION);
		if (header == null || !header.startsWith("Bearer ")) {
			filterChain.doFilter(request, response);
			return;
		}

		String token = header.substring(7);
		try {
			if (revokedTokenRepository.existsByToken(token) || !jwtUtil.isAccessToken(token)) {
				writeUnauthorized(response, "Invalid or expired token");
				return;
			}

			if (SecurityContextHolder.getContext().getAuthentication() == null) {
				String username = jwtUtil.extractUsername(token);
				VservUserDetails userDetails = (VservUserDetails) userDetailsService.loadUserByUsername(username);
				if (jwtUtil.isTokenValid(token, userDetails)) {
					UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
							userDetails, null, userDetails.getAuthorities());
					authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
					SecurityContextHolder.getContext().setAuthentication(authentication);
				} else {
					writeUnauthorized(response, "Invalid or expired token");
					return;
				}
			}

			filterChain.doFilter(request, response);
		} catch (JwtException | IllegalArgumentException ex) {
			writeUnauthorized(response, "Invalid or expired token");
		}
	}

	private void writeUnauthorized(HttpServletResponse response, String message) throws IOException {
		response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		objectMapper.writeValue(response.getWriter(), Map.of("error", "Unauthorized", "message", message));
	}
}

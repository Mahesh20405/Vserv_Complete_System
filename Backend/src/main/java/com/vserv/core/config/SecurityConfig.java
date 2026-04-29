package com.vserv.core.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

	private final JwtAuthenticationFilter jwtAuthenticationFilter;
	private final CorsConfigurationSource corsConfigurationSource;

	public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter,
			CorsConfigurationSource corsConfigurationSource) {
		this.jwtAuthenticationFilter = jwtAuthenticationFilter;
		this.corsConfigurationSource = corsConfigurationSource;
	}

	@Bean
	PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
		return config.getAuthenticationManager();
	}

	@Bean
	SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		http.csrf(csrf -> csrf.disable()).cors(cors -> cors.configurationSource(corsConfigurationSource))

				.authorizeHttpRequests(
						auth -> auth.requestMatchers(org.springframework.http.HttpMethod.OPTIONS, "/**").permitAll()
								.requestMatchers("/api/auth/login", "/api/auth/logout", "/api/auth/register",
										"/api/auth/forgot-password/**", "/api/auth/refresh")
								.permitAll().requestMatchers("/api/catalog").permitAll().requestMatchers("/api/**")
								.authenticated().anyRequest().permitAll())

				.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

		return http.build();
	}
}

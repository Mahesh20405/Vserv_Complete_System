package com.vserv.features.auth.mapper;

import com.vserv.entity.AppUser;
import com.vserv.features.auth.dto.LoginResponse;

public final class AuthMapper {
	private AuthMapper() {
	}

	public static LoginResponse toLoginResponse(AppUser user) {
		String role = user.getRole() != null ? user.getRole().getRoleName().name() : "UNKNOWN";
		return new LoginResponse(user.getUserId(), user.getFullName(), user.getEmail(), role);
	}
}

package com.vserv.core.config;

import com.vserv.entity.AppUser;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.Collection;
import java.util.Collections;

public class VservUserDetails implements UserDetails {

	private static final long serialVersionUID = 1L;

	private final AppUser user;

	public VservUserDetails(AppUser user) {
		this.user = user;
	}

	public AppUser getUser() {
		return user;
	}

	public Integer getUserId() {
		return user.getUserId();
	}

	public String getFullName() {
		return user.getFullName();
	}

	public String getRoleName() {
		return user.getRole().getRoleName().name();
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + user.getRole().getRoleName().name()));
	}

	@Override
	public String getPassword() {
		return user.getPassword();
	}

	@Override
	public String getUsername() {
		return user.getEmail();
	}

	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		return !Boolean.TRUE.equals(user.getIsDeleted());
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}

	@Override
	public boolean isEnabled() {
		return user.getStatus() == AppUser.Status.ACTIVE && !Boolean.TRUE.equals(user.getIsDeleted());
	}
}

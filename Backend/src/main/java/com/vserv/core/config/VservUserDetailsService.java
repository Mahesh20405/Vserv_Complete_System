package com.vserv.core.config;

import com.vserv.features.user.repository.UserRepository;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class VservUserDetailsService implements UserDetailsService {

	private final UserRepository userRepository;

	public VservUserDetailsService(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	@Override
	public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
		String normalizedEmail = email == null ? null : email.trim().toLowerCase();
		return userRepository.findByEmailAndIsDeletedFalse(normalizedEmail).map(VservUserDetails::new)
				.orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));
	}
}

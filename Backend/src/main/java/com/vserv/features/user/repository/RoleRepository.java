package com.vserv.features.user.repository;

import com.vserv.entity.Role;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Integer> {
	Optional<Role> findByRoleName(Role.RoleName roleName);
}

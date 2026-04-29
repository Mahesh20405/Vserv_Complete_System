package com.vserv.features.user.repository;

import com.vserv.entity.AppUser;
import com.vserv.entity.Role;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<AppUser, Integer> {
	Optional<AppUser> findByEmailAndIsDeletedFalse(String email);

	Optional<AppUser> findByUserIdAndIsDeletedFalse(Integer userId);

	boolean existsByEmailAndIsDeletedFalse(String email);

	boolean existsByPhoneAndIsDeletedFalse(String phone);

	boolean existsByPhoneAndUserIdNotAndIsDeletedFalse(String phone, Integer userId);

	List<AppUser> findByRoleAndIsDeletedFalse(Role role);

	List<AppUser> findByIsDeletedFalse();

	long countByIsDeletedFalse();

	@Query("""
			SELECT u FROM AppUser u
			WHERE u.isDeleted=false
			  AND (
			      LOWER(u.fullName) LIKE LOWER(CONCAT('%', :q, '%'))
			      OR LOWER(u.email) LIKE LOWER(CONCAT('%', :q, '%'))
			      OR LOWER(COALESCE(u.phone, '')) LIKE LOWER(CONCAT('%', :q, '%'))
			  )
			""")
	List<AppUser> searchActive(@Param("q") String q);

	@Query("""
			SELECT u FROM AppUser u
			WHERE u.isDeleted=false
			  AND u.role = :role
			  AND (
			      LOWER(u.fullName) LIKE LOWER(CONCAT('%', :q, '%'))
			      OR LOWER(u.email) LIKE LOWER(CONCAT('%', :q, '%'))
			      OR LOWER(COALESCE(u.phone, '')) LIKE LOWER(CONCAT('%', :q, '%'))
			  )
			""")
	List<AppUser> searchActiveByRole(@Param("q") String q, @Param("role") Role role);
}

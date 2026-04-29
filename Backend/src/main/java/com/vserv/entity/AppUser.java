package com.vserv.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "user")
public class AppUser {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "user_id")
	private Integer userId;

	@Column(name = "full_name", nullable = false, length = 100)
	private String fullName;

	@Column(unique = true, nullable = false, length = 100)
	private String email;

	@Column(nullable = false, length = 255)
	private String password;

	@Column(length = 15)
	private String phone;

	@Enumerated(EnumType.STRING)
	private Gender gender;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "role_id", nullable = false)
	private Role role;

	@Enumerated(EnumType.STRING)
	private Status status = Status.ACTIVE;

	@Column(name = "is_deleted")
	private Boolean isDeleted = false;

	@Column(name = "deleted_at")
	private LocalDateTime deletedAt;

	@Column(name = "created_at")
	private LocalDateTime createdAt = LocalDateTime.now();

	@Column(name = "last_login")
	private LocalDateTime lastLogin;

	@Column(name = "loyalty_badge")
	private Boolean loyaltyBadge = false;

	public AppUser() {
	}

	public Integer getUserId() {
		return userId;
	}

	public void setUserId(Integer userId) {
		this.userId = userId;
	}

	public String getFullName() {
		return fullName;
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public Gender getGender() {
		return gender;
	}

	public void setGender(Gender gender) {
		this.gender = gender;
	}

	public Role getRole() {
		return role;
	}

	public void setRole(Role role) {
		this.role = role;
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	public Boolean getIsDeleted() {
		return isDeleted;
	}

	public void setIsDeleted(Boolean isDeleted) {
		this.isDeleted = isDeleted;
	}

	public LocalDateTime getDeletedAt() {
		return deletedAt;
	}

	public void setDeletedAt(LocalDateTime deletedAt) {
		this.deletedAt = deletedAt;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public LocalDateTime getLastLogin() {
		return lastLogin;
	}

	public void setLastLogin(LocalDateTime lastLogin) {
		this.lastLogin = lastLogin;
	}

	public Boolean getLoyaltyBadge() {
		return loyaltyBadge;
	}

	public void setLoyaltyBadge(Boolean loyaltyBadge) {
		this.loyaltyBadge = loyaltyBadge;
	}

	public enum Gender {
		MALE, FEMALE, PREFER_NOT_TO_SAY
	}

	public enum Status {
		ACTIVE, INACTIVE
	}
}

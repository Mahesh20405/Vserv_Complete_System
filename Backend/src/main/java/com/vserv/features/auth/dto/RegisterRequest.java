package com.vserv.features.auth.dto;

import com.vserv.core.util.ValidationPatterns;
import com.vserv.entity.AppUser;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class RegisterRequest {
	@NotBlank(message = "Full name is required")
	@Size(min = 2, max = 100, message = "Full name must be between 2 and 100 characters")
	@Pattern(regexp = ValidationPatterns.NAME, message = "Full name can only contain letters, spaces, hyphens, apostrophes, and dots")
	private String fullName;
	
	@NotBlank(message = "Email is required")
	@Email(message = "Email must be valid")
	@Size(max = 100, message = "Email must be at most 100 characters")
	private String email;
	
	@Pattern(regexp = ValidationPatterns.PHONE, message = "Enter a valid Indian mobile number")
	private String phone;
	
	private AppUser.Gender gender;
	
	@NotBlank(message = "Password is required")
	@Size(min = 8, max = 255, message = "Password must be between 8 and 255 characters")
	@Pattern(regexp = ValidationPatterns.PASSWORD, message = "Password must include uppercase, lowercase, digit, and special character")
	private String password;

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

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public AppUser.Gender getGender() {
		return gender;
	}

	public void setGender(AppUser.Gender gender) {
		this.gender = gender;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
}

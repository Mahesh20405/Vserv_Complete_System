package com.vserv.features.auth.service;

public interface EmailService {
	boolean sendOtpEmail(String toEmail, String toName, String otp);
}

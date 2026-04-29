package com.vserv.features.auth.service.impl;

import com.vserv.core.config.ResendProperties;
import com.vserv.features.auth.service.EmailService;
import java.util.List;
import java.util.Map;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class ResendEmailService implements EmailService {
	private static final Logger log = LoggerFactory.getLogger(ResendEmailService.class);

	private final RestClient restClient;
	private final String resendApiKey;
	private final String senderEmail;
	private final String senderName;

	public ResendEmailService(ResendProperties resendProperties) {
		this.restClient = RestClient.builder().baseUrl(resendProperties.getApi().getBaseUrl()).build();
		this.resendApiKey = resendProperties.getApi().getKey();
		this.senderEmail = resendProperties.getSender().getEmail();
		this.senderName = resendProperties.getSender().getName();
	}

	@Override
	public boolean sendOtpEmail(String toEmail, String toName, String otp) {
		if (resendApiKey == null || resendApiKey.isBlank() || senderEmail == null || senderEmail.isBlank()) {
			log.warn(
					"Resend email service is not configured. Falling back to local OTP logging for {}. OTP: {}. Configure RESEND_API_KEY and RESEND_SENDER_EMAIL to enable email delivery.",
					toEmail, otp);
			return false;
		}

		String safeName = (toName == null || toName.isBlank()) ? "there" : toName;
		String html = "<p>Hi " + safeName + ",</p>" + "<p>Your OTP is <strong>" + otp
				+ "</strong>. Valid for 10 minutes.</p>" + "<p>If you didn't request this, ignore this email.</p>";

		try {
			restClient.post().uri("/emails").contentType(MediaType.APPLICATION_JSON)
					.header("Authorization", "Bearer " + resendApiKey)
					.body(Map.of("from", senderName + " <" + senderEmail + ">", "to", List.of(toEmail), "subject",
							"Your VServ Password Reset OTP", "html", html))
					.retrieve().toBodilessEntity();
			return true;
		} catch (HttpClientErrorException.Forbidden ex) {
			log.warn(
					"Resend rejected OTP email for {} because the sender is still in testing mode or the domain is unverified. Falling back to local OTP logging. OTP: {}. Response: {}",
					toEmail, otp, ex.getResponseBodyAsString());
			return false;
		} catch (ResourceAccessException ex) {
			log.warn(
					"Resend OTP email could not be delivered for {}. Falling back to local OTP logging. OTP: {}. Cause: {}",
					toEmail, otp, rootCauseMessage(ex));
			return false;
		} catch (RestClientException ex) {
			log.warn(
					"Resend OTP email request failed for {}. Falling back to local OTP logging. OTP: {}. Cause: {}",
					toEmail, otp, rootCauseMessage(ex));
			return false;
		}
	}

	private String rootCauseMessage(Throwable throwable) {
		Throwable current = throwable;
		while (current != null && current.getCause() != null) {
			current = current.getCause();
		}
		if (current == null) {
			return "Unknown error";
		}
		return current.getClass().getSimpleName() + (current.getMessage() == null ? "" : ": " + current.getMessage());
	}
}

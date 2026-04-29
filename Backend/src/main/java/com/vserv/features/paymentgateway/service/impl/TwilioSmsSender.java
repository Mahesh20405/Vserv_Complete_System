package com.vserv.features.paymentgateway.service.impl;

import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import com.vserv.core.config.TwilioProperties;
import com.vserv.features.paymentgateway.service.SmsSender;

import jakarta.annotation.PostConstruct;

@Service
public class TwilioSmsSender implements SmsSender {
	private static final Logger log = LoggerFactory.getLogger(TwilioSmsSender.class);
	private static final Pattern NON_DIGIT = Pattern.compile("[^0-9]");
	private static final String TEST_LOCAL_NUMBER = "7010610585";
	private final TwilioProperties properties;
	private volatile boolean twilioInitialized;

	public TwilioSmsSender(TwilioProperties properties) {
		this.properties = properties;
	}

	@PostConstruct
	void initializeTwilioClient() {
		if (!properties.isEnabled() || !StringUtils.hasText(properties.getAccountSid())
				|| !StringUtils.hasText(properties.getAuthToken())) {
			return;
		}
		Twilio.init(properties.getAccountSid(), properties.getAuthToken());
		twilioInitialized = true;
	}

	@Override
	public void send(String to, String body) {
		String normalizedTo = normalizeToE164(to);
		if (!properties.isEnabled() || !StringUtils.hasText(normalizedTo) || !StringUtils.hasText(body)
				|| !StringUtils.hasText(properties.getAccountSid()) || !StringUtils.hasText(properties.getAuthToken())
				|| !StringUtils.hasText(properties.getPhoneNumber())) {
			return;
		}
		try {
			if (!twilioInitialized) {
				initializeTwilioClient();
			}
			Message.creator(new PhoneNumber(normalizedTo), new PhoneNumber(properties.getPhoneNumber()), body).create();
		} catch (Exception ex) {
			log.warn("Twilio SMS send failed rawTo={} normalizedTo={} message={}", to, normalizedTo, ex.getMessage());
		}
	}

	private String normalizeToE164(String rawPhone) {
		if (!StringUtils.hasText(rawPhone)) {
			return null;
		}
		String trimmed = rawPhone.trim();
		if (trimmed.startsWith("+")) {
			String digits = NON_DIGIT.matcher(trimmed.substring(1)).replaceAll("");
			return digits.isEmpty() ? null : "+" + digits;
		}

		String digitsOnly = NON_DIGIT.matcher(trimmed).replaceAll("");
		if (TEST_LOCAL_NUMBER.equals(digitsOnly)) {
			return "+91" + digitsOnly;
		}
		if (digitsOnly.length() >= 11 && digitsOnly.length() <= 15) {
			return "+" + digitsOnly;
		}
		return trimmed;
	}
}

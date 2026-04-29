package com.vserv.core.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "razorpay")
public class RazorpayProperties {
	private boolean enabled;
	private String keyId = "";
	private String keySecret = "";
	private String currency = "INR";
	private String companyName = "VServ";

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public String getKeyId() {
		return keyId;
	}

	public void setKeyId(String keyId) {
		this.keyId = keyId;
	}

	public String getKeySecret() {
		return keySecret;
	}

	public void setKeySecret(String keySecret) {
		this.keySecret = keySecret;
	}

	public String getCurrency() {
		return currency;
	}

	public void setCurrency(String currency) {
		this.currency = currency;
	}

	public String getCompanyName() {
		return companyName;
	}

	public void setCompanyName(String companyName) {
		this.companyName = companyName;
	}
}

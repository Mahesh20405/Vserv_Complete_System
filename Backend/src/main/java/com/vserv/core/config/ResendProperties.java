package com.vserv.core.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "resend")
public class ResendProperties {
	private final Api api = new Api();
	private final Sender sender = new Sender();

	public Api getApi() {
		return api;
	}

	public Sender getSender() {
		return sender;
	}

	public static class Api {
		private String key = "";
		private String baseUrl = "https://api.resend.com";

		public String getKey() {
			return key;
		}

		public void setKey(String key) {
			this.key = key;
		}

		public String getBaseUrl() {
			return baseUrl;
		}

		public void setBaseUrl(String baseUrl) {
			this.baseUrl = baseUrl;
		}
	}

	public static class Sender {
		private String email = "";
		private String name = "VServ";

		public String getEmail() {
			return email;
		}

		public void setEmail(String email) {
			this.email = email;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}
	}
}

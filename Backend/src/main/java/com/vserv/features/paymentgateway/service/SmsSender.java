package com.vserv.features.paymentgateway.service;

public interface SmsSender {
	void send(String to, String body);
}

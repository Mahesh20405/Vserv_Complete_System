package com.vserv.core.response;

import java.time.LocalDateTime;

public class ApiSuccess<T> {

	private final LocalDateTime timestamp;
	private final T data;

	public ApiSuccess(T data) {
		this.timestamp = LocalDateTime.now();
		this.data = data;
	}

	public LocalDateTime getTimestamp() {
		return timestamp;
	}

	public T getData() {
		return data;
	}
}

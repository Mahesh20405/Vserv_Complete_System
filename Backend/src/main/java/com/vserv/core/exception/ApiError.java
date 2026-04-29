package com.vserv.core.exception;

import java.time.LocalDateTime;

public class ApiError {
	private int status;
	private String error;
	private String message;
	private LocalDateTime timestamp = LocalDateTime.now();

	public ApiError(int status, String error, String message) {
		this.status = status;
		this.error = error;
		this.message = message;
	}

	public int getStatus() {
		return status;
	}

	public String getError() {
		return error;
	}

	public String getMessage() {
		return message;
	}

	public LocalDateTime getTimestamp() {
		return timestamp;
	}
}

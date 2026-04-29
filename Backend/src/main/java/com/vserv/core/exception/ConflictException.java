package com.vserv.core.exception;

public class ConflictException extends BusinessException {
	private static final long serialVersionUID = 1L;

	public ConflictException(String message) {
		super(message);
	}
}

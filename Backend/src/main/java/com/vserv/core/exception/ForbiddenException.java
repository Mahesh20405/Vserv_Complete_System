package com.vserv.core.exception;

public class ForbiddenException extends BusinessException {
	private static final long serialVersionUID = 1L;

	public ForbiddenException(String message) {
		super(message);
	}
}

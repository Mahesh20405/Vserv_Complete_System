package com.vserv.core.util;

public final class ValidationPatterns {

	private ValidationPatterns() {
	}

	public static final String NAME = "^[a-zA-Z\\s.'-]+$";
	public static final String PHONE = "^[6-9][0-9]{9}$";
	public static final String PASSWORD = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&#^()_+=\\-])[A-Za-z\\d@$!%*?&#^()_+=\\-]{8,}$";
	public static final String REGISTRATION_NUMBER = "^[A-Z]{2}[0-9]{1,2}[A-Z]{1,3}[0-9]{4}$";
	public static final String SLOT = "^[A-Za-z0-9\\s:.-]+$";
	public static final String REASON = "^[a-zA-Z0-9_\\s.,!?'\\-\\/():]+$";
	public static final String TRANSACTION_REFERENCE = "^[A-Za-z0-9._\\-/]+$";
}

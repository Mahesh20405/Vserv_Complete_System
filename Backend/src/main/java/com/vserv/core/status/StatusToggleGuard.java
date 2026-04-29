package com.vserv.core.status;

public class StatusToggleGuard {
	private final boolean allowed;
	private final String reason;

	private StatusToggleGuard(boolean allowed, String reason) {
		this.allowed = allowed;
		this.reason = reason;
	}

	public static StatusToggleGuard allowed() {
		return new StatusToggleGuard(true, null);
	}

	public static StatusToggleGuard blocked(String reason) {
		return new StatusToggleGuard(false, reason);
	}

	public boolean isAllowed() {
		return allowed;
	}

	public String getReason() {
		return reason;
	}
}

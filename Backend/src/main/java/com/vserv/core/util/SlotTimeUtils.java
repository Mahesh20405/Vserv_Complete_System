package com.vserv.core.util;

import java.time.LocalTime;

public final class SlotTimeUtils {

	private SlotTimeUtils() {
	}

	public static LocalTime parseSlotStartTime(String slot) {
		if (slot == null || slot.isBlank()) {
			return null;
		}
		try {
			return LocalTime.parse(slot.split("-")[0].trim());
		} catch (RuntimeException ignored) {
			return null;
		}
	}
}

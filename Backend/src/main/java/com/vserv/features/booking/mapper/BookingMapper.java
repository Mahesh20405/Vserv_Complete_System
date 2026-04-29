package com.vserv.features.booking.mapper;

import com.vserv.entity.BookingHistory;
import com.vserv.entity.Invoice;
import com.vserv.entity.ServiceBooking;
import com.vserv.entity.ServiceRecord;
import com.vserv.features.booking.dto.BookingDto;
import com.vserv.features.booking.dto.BookingHistoryDto;

public final class BookingMapper {
	private BookingMapper() {
	}

	public static BookingDto toDto(ServiceBooking booking, ServiceRecord record, Invoice invoice) {
		return BookingDto.from(booking, record, invoice);
	}

	public static BookingDto toDto(ServiceBooking booking, ServiceRecord record, Invoice invoice,
			com.vserv.entity.Payment bookingChargePayment) {
		return BookingDto.from(booking, record, invoice, bookingChargePayment);
	}

	public static BookingHistoryDto toHistoryDto(BookingHistory history) {
		return BookingHistoryDto.from(history);
	}
}

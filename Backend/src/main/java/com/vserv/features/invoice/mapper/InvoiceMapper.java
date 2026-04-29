package com.vserv.features.invoice.mapper;

import com.vserv.entity.Invoice;
import com.vserv.features.invoice.dto.InvoiceDto;

import java.math.BigDecimal;

public final class InvoiceMapper {
	private InvoiceMapper() {
	}

	public static InvoiceDto toDto(Invoice invoice, BigDecimal remainingBalance) {
		return InvoiceDto.from(invoice, remainingBalance);
	}
}

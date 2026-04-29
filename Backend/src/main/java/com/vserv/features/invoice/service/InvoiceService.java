package com.vserv.features.invoice.service;

import com.vserv.entity.AppUser;
import com.vserv.entity.Invoice;
import com.vserv.entity.Payment;
import com.vserv.entity.ServiceRecord;
import com.vserv.features.invoice.dto.BookingPaymentInfoDto;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface InvoiceService {
	List<Invoice> findAll();

	Optional<Invoice> findById(Integer id);

	Optional<Invoice> findByServiceRecordId(Integer serviceId);

	Optional<Invoice> findByBookingId(Integer bookingId);

	Optional<Payment> findLatestSuccessfulBookingChargeByBookingId(Integer bookingId);

	Optional<BookingPaymentInfoDto> findBookingPaymentInfo(Integer bookingId);

	List<Invoice> findByCustomer(Integer userId);

	List<Invoice> findByCustomerAndStatus(Integer userId, Invoice.PaymentStatus status);

	List<Invoice> findByStatus(Invoice.PaymentStatus status);

	long countPending();

	BigDecimal totalRevenue();

	Invoice createBookingChargeInvoice(ServiceRecord record, BigDecimal bookingCharge, Payment.PaymentMethod method,
			String txRef, AppUser customer, boolean waivedByLoyalty);

	Invoice getOrCreateForRecord(ServiceRecord record);

	Invoice save(Invoice invoice);

	Payment recordPayment(Integer invoiceId, Payment.PaymentMethod method, BigDecimal amount, String txRef,
			AppUser customer);

	void validateDigitalPaymentIntent(Invoice invoice, Payment.PaymentMethod method, BigDecimal amount, AppUser actor);

	BigDecimal successfulAmountByPurpose(Invoice invoice, Payment.PaymentPurpose purpose);

	BigDecimal remainingBalance(Invoice invoice);

	Invoice refreshInvoicePaymentStatus(Invoice invoice);
}

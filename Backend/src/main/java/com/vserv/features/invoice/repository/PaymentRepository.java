package com.vserv.features.invoice.repository;

import com.vserv.entity.Payment;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Integer> {
	List<Payment> findByInvoiceInvoiceId(Integer invoiceId);

	Optional<Payment> findFirstByInvoiceInvoiceIdAndTransactionReferenceAndPaymentPurposeAndPaymentStatus(
			Integer invoiceId, String transactionReference, Payment.PaymentPurpose purpose,
			Payment.PaymentStatus paymentStatus);

	@Query("""
			SELECT COALESCE(SUM(p.amount), 0)
			FROM Payment p
			WHERE p.invoice.invoiceId = :invoiceId
			  AND p.paymentStatus = 'SUCCESS'
			""")
	BigDecimal sumSuccessfulAmountByInvoiceId(@Param("invoiceId") Integer invoiceId);

	@Query("""
			SELECT COALESCE(SUM(p.amount), 0)
			FROM Payment p
			WHERE p.invoice.invoiceId = :invoiceId
			  AND p.paymentStatus = 'SUCCESS'
			  AND p.paymentPurpose = :purpose
			""")
	BigDecimal sumSuccessfulAmountByInvoiceIdAndPurpose(@Param("invoiceId") Integer invoiceId,
			@Param("purpose") Payment.PaymentPurpose purpose);

	@Query("""
			SELECT p
			FROM Payment p
			JOIN FETCH p.invoice i
			JOIN i.serviceRecord sr
			JOIN sr.booking b
			WHERE b.bookingId = :bookingId
			  AND p.paymentStatus = com.vserv.entity.Payment.PaymentStatus.SUCCESS
			  AND p.paymentPurpose = com.vserv.entity.Payment.PaymentPurpose.BOOKING_CHARGE
			ORDER BY p.paymentDate DESC, p.paymentId DESC
			""")
	List<Payment> findSuccessfulBookingChargePaymentsByBookingId(@Param("bookingId") Integer bookingId);
}

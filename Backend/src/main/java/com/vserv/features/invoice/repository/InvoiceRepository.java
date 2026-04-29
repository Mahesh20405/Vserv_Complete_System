package com.vserv.features.invoice.repository;

import com.vserv.entity.Invoice;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface InvoiceRepository extends JpaRepository<Invoice, Integer> {
	Optional<Invoice> findByServiceRecordServiceId(Integer serviceId);

	@Query("""
			SELECT DISTINCT i FROM Invoice i
			JOIN FETCH i.serviceRecord sr
			JOIN FETCH sr.booking b
			LEFT JOIN FETCH i.payments p
			WHERE b.bookingId = :bookingId
			""")
	Optional<Invoice> findByBookingIdWithPayments(@Param("bookingId") Integer bookingId);

	@Query(value = """
			SELECT
				i.payment_status,
				i.booking_charge,
				p.payment_method,
				p.transaction_reference,
				p.payment_date
			FROM service_booking b
			LEFT JOIN service_record sr
				ON sr.booking_id = b.booking_id
			LEFT JOIN invoice i
				ON i.service_id = sr.service_id
			LEFT JOIN payment p
				ON p.payment_id = (
					SELECT p2.payment_id
					FROM payment p2
					WHERE p2.invoice_id = i.invoice_id
					  AND p2.payment_status = 'SUCCESS'
					  AND p2.payment_purpose = 'BOOKING_CHARGE'
					ORDER BY p2.payment_date DESC, p2.payment_id DESC
					LIMIT 1
				)
			WHERE b.booking_id = :bookingId
			""", nativeQuery = true)
	List<Object[]> findBookingPaymentInfoRow(@Param("bookingId") Integer bookingId);

	@Query("SELECT i FROM Invoice i WHERE i.invoiceId = :id")
	Optional<Invoice> findByIdForUpdate(@Param("id") Integer id);

	@Query("""
			SELECT i FROM Invoice i
			JOIN i.serviceRecord sr
			WHERE sr.status = com.vserv.entity.ServiceRecord.ServiceStatus.COMPLETED
			ORDER BY i.invoiceDate DESC
			""")
	List<Invoice> findVisibleInvoicesOrderByInvoiceDateDesc();

	@Query("""
			SELECT i FROM Invoice i
			JOIN i.serviceRecord sr
			WHERE sr.status = com.vserv.entity.ServiceRecord.ServiceStatus.COMPLETED
			AND i.paymentStatus = :status
			ORDER BY i.invoiceDate DESC
			""")
	List<Invoice> findVisibleInvoicesByPaymentStatusOrderByInvoiceDateDesc(
			@Param("status") Invoice.PaymentStatus status);

	@Query("""
			SELECT COUNT(i) FROM Invoice i
			JOIN i.serviceRecord sr
			WHERE sr.status = com.vserv.entity.ServiceRecord.ServiceStatus.COMPLETED
			AND i.paymentStatus = :status
			""")
	long countVisibleInvoicesByPaymentStatus(@Param("status") Invoice.PaymentStatus status);

	@Query("""
			SELECT COALESCE(SUM(i.totalAmount),0) FROM Invoice i
			JOIN i.serviceRecord sr
			WHERE sr.status = com.vserv.entity.ServiceRecord.ServiceStatus.COMPLETED
			AND i.paymentStatus = com.vserv.entity.Invoice.PaymentStatus.PAID
			""")
	BigDecimal sumVisiblePaidAmount();

	@Query("""
			SELECT i FROM Invoice i
			JOIN i.serviceRecord sr
			JOIN sr.booking b
			JOIN b.vehicle v
			WHERE v.user.userId = :userId
			AND sr.status = com.vserv.entity.ServiceRecord.ServiceStatus.COMPLETED
			ORDER BY i.invoiceDate DESC
			""")
	List<Invoice> findVisibleByCustomerId(@Param("userId") Integer userId);

	@Query("""
			SELECT i FROM Invoice i
			JOIN i.serviceRecord sr
			JOIN sr.booking b
			JOIN b.vehicle v
			WHERE v.user.userId = :userId
			AND sr.status = com.vserv.entity.ServiceRecord.ServiceStatus.COMPLETED
			AND i.paymentStatus = :status
			ORDER BY i.invoiceDate DESC
			""")
	List<Invoice> findVisibleByCustomerIdAndPaymentStatus(@Param("userId") Integer userId,
			@Param("status") Invoice.PaymentStatus status);

	@Query("""
			SELECT COUNT(i) > 0
			FROM Invoice i
			JOIN i.serviceRecord sr
			JOIN sr.booking b
			JOIN b.vehicle v
			WHERE v.user.userId = :userId
			AND i.paymentStatus IN :statuses
			""")
	boolean existsUnsettledInvoicesForCustomer(@Param("userId") Integer userId,
			@Param("statuses") Collection<Invoice.PaymentStatus> statuses);

	@Query("""
			SELECT COUNT(i) > 0
			FROM Invoice i
			JOIN i.serviceRecord sr
			JOIN sr.booking b
			WHERE b.vehicle.vehicleId = :vehicleId
			AND i.paymentStatus IN :statuses
			""")
	boolean existsUnsettledInvoicesForVehicle(@Param("vehicleId") Integer vehicleId,
			@Param("statuses") Collection<Invoice.PaymentStatus> statuses);

	@Query("""
			SELECT YEAR(i.invoiceDate), MONTH(i.invoiceDate), COALESCE(SUM(i.totalAmount), 0)
			FROM Invoice i
			WHERE i.paymentStatus = com.vserv.entity.Invoice.PaymentStatus.PAID
			AND i.invoiceDate >= :since
			GROUP BY YEAR(i.invoiceDate), MONTH(i.invoiceDate)
			ORDER BY YEAR(i.invoiceDate), MONTH(i.invoiceDate)
			""")
	List<Object[]> sumPaidByMonthSince(@Param("since") LocalDate since);
}

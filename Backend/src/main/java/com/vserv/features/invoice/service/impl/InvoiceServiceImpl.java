package com.vserv.features.invoice.service.impl;

import com.vserv.features.invoice.repository.PaymentRepository;

import com.vserv.features.invoice.repository.InvoiceRepository;
import com.vserv.features.invoice.dto.BookingPaymentInfoDto;

import com.vserv.features.invoice.service.InvoiceService;

import com.vserv.entity.Invoice;
import com.vserv.entity.Payment;
import com.vserv.entity.ServiceBooking;
import com.vserv.entity.ServiceRecord;

import com.vserv.core.exception.BusinessException;
import com.vserv.core.exception.ConflictException;
import com.vserv.core.exception.NotFoundException;
import com.vserv.entity.AppUser;
import com.vserv.entity.Notification;
import com.vserv.features.notification.service.NotificationService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

@Service
public class InvoiceServiceImpl implements InvoiceService {
	private static final Logger log = LoggerFactory.getLogger(InvoiceServiceImpl.class);
	private static final String GATEWAY_PROVIDER_RAZORPAY = "razorpay";
	private static final String GATEWAY_PROVIDER_CASH = "cash";
	private static final String CASH_REFERENCE = "cash";

	private static final Pattern TXN_REF_PATTERN = Pattern.compile("^[A-Za-z0-9._\\-/#]{4,40}$");

	private final InvoiceRepository invoiceRepo;
	private final PaymentRepository paymentRepo;
	private final NotificationService notificationService;

	public InvoiceServiceImpl(InvoiceRepository invoiceRepo, PaymentRepository paymentRepo,
			NotificationService notificationService) {
		this.invoiceRepo = invoiceRepo;
		this.paymentRepo = paymentRepo;
		this.notificationService = notificationService;
	}

	public List<Invoice> findAll() {
		return invoiceRepo.findVisibleInvoicesOrderByInvoiceDateDesc();
	}

	public Optional<Invoice> findById(Integer id) {
		return invoiceRepo.findById(id);
	}

	public Optional<Invoice> findByServiceRecordId(Integer serviceId) {
		return invoiceRepo.findByServiceRecordServiceId(serviceId);
	}

	public Optional<Invoice> findByBookingId(Integer bookingId) {
		return invoiceRepo.findByBookingIdWithPayments(bookingId);
	}

	public Optional<Payment> findLatestSuccessfulBookingChargeByBookingId(Integer bookingId) {
		return paymentRepo.findSuccessfulBookingChargePaymentsByBookingId(bookingId).stream().findFirst();
	}

	public Optional<BookingPaymentInfoDto> findBookingPaymentInfo(Integer bookingId) {
		return findByBookingId(bookingId).map(invoice -> {
			Payment bookingChargePayment = invoice.getPayments() == null ? null
					: invoice.getPayments().stream()
							.filter(payment -> payment.getPaymentStatus() == Payment.PaymentStatus.SUCCESS)
							.filter(payment -> payment.getPaymentPurpose() == Payment.PaymentPurpose.BOOKING_CHARGE)
							.max(java.util.Comparator.comparing(Payment::getPaymentDate)
									.thenComparing(Payment::getPaymentId))
							.orElse(null);

			BigDecimal totalAmount = invoice.getTotalAmount() != null ? invoice.getTotalAmount() : BigDecimal.ZERO;
			BigDecimal successfulPaidAmount = invoice.getPayments() == null ? BigDecimal.ZERO
					: invoice.getPayments().stream()
							.filter(payment -> payment.getPaymentStatus() == Payment.PaymentStatus.SUCCESS)
							.map(Payment::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);

			String paymentStatus;
			if (successfulPaidAmount.compareTo(BigDecimal.ZERO) <= 0) {
				paymentStatus = invoice.getPaymentStatus() != null ? invoice.getPaymentStatus().name() : "PENDING";
			} else if (totalAmount.compareTo(BigDecimal.ZERO) > 0 && successfulPaidAmount.compareTo(totalAmount) >= 0) {
				paymentStatus = "PAID";
			} else {
				paymentStatus = "PARTIALLY_PAID";
			}

			return new BookingPaymentInfoDto(paymentStatus, invoice.getBookingCharge(),
					bookingChargePayment != null && bookingChargePayment.getPaymentMethod() != null
							? bookingChargePayment.getPaymentMethod().name()
							: null,
					bookingChargePayment != null ? bookingChargePayment.getTransactionReference() : null,
					bookingChargePayment != null ? bookingChargePayment.getPaymentDate() : null);
		});
	}

	public List<Invoice> findByCustomer(Integer userId) {
		return invoiceRepo.findVisibleByCustomerId(userId);
	}

	public List<Invoice> findByCustomerAndStatus(Integer userId, Invoice.PaymentStatus status) {
		return invoiceRepo.findVisibleByCustomerIdAndPaymentStatus(userId, status);
	}

	public List<Invoice> findByStatus(Invoice.PaymentStatus status) {
		return invoiceRepo.findVisibleInvoicesByPaymentStatusOrderByInvoiceDateDesc(status);
	}

	public long countPending() {
		return invoiceRepo.countVisibleInvoicesByPaymentStatus(Invoice.PaymentStatus.PENDING);
	}

	public BigDecimal totalRevenue() {
		return invoiceRepo.sumVisiblePaidAmount();
	}

	@Transactional
	public Invoice createBookingChargeInvoice(ServiceRecord record, BigDecimal bookingCharge,
			Payment.PaymentMethod method, String txRef, AppUser customer, boolean waivedByLoyalty) {
		BigDecimal chargeAmount = bookingCharge != null ? bookingCharge : BigDecimal.ZERO;
		if (chargeAmount.compareTo(BigDecimal.ZERO) < 0) {
			throw new BusinessException("Booking charge cannot be negative.");
		}
		ServiceBooking booking = record.getBooking();
		String normalizedTxRef = normalizeTransactionReference(method, txRef);

		Invoice invoice = getOrCreateForRecord(record);
		invoice.setServiceRecord(record);
		invoice.setItemsTotal(BigDecimal.ZERO);
		invoice.setOvertimeCharge(BigDecimal.ZERO);
		BigDecimal basePrice = booking != null && booking.getCatalog() != null
				&& booking.getCatalog().getBasePrice() != null ? booking.getCatalog().getBasePrice() : BigDecimal.ZERO;
		invoice.setBaseServicePrice(basePrice);
		invoice.setBookingCharge(chargeAmount);
		invoice.setTotalAmount(basePrice.add(chargeAmount));
		invoice.setAdvanceAmount(chargeAmount);
		invoice.setAdvancePaid(chargeAmount.compareTo(BigDecimal.ZERO) > 0);
		Invoice savedInvoice = invoiceRepo.save(invoice);

		if (chargeAmount.compareTo(BigDecimal.ZERO) == 0) {
			savedInvoice.setPaymentStatus(Invoice.PaymentStatus.PENDING);
			return invoiceRepo.save(savedInvoice);
		}
		if (method == null) {
			throw new BusinessException("Booking charge payment method is required.");
		}
		Payment payment = new Payment();
		payment.setInvoice(savedInvoice);
		payment.setPaymentMethod(method);
		payment.setAmount(chargeAmount);
		payment.setTransactionReference(normalizedTxRef);
		payment.setGatewayProvider(resolveGatewayProvider(method));
		payment.setPaymentStatus(Payment.PaymentStatus.SUCCESS);
		payment.setPaymentPurpose(Payment.PaymentPurpose.BOOKING_CHARGE);
		validatePayment(savedInvoice, method, chargeAmount, normalizedTxRef, false,
				Payment.PaymentPurpose.BOOKING_CHARGE);
		paymentRepo.save(payment);
		refreshInvoicePaymentStatus(savedInvoice);

		log.info("Booking charge payment recorded serviceId={} amount={} method={}", record.getServiceId(),
				chargeAmount, method);
		notificationService.send(customer, Notification.NotificationType.PAYMENT_REMINDER, "Booking Charge Received",
				"Booking charge of \u20b9" + chargeAmount + " received for booking #" + booking.getBookingId()
						+ (normalizedTxRef != null ? ". Transaction reference: " + normalizedTxRef + "." : "."),
				booking);
		return savedInvoice;
	}

	public Invoice getOrCreateForRecord(ServiceRecord record) {
		return invoiceRepo.findByServiceRecordServiceId(record.getServiceId()).orElseGet(() -> {
			Invoice invoice = new Invoice();
			invoice.setServiceRecord(record);
			return invoice;
		});
	}

	@Transactional
	public Invoice save(Invoice invoice) {
		return invoiceRepo.save(invoice);
	}

	@Transactional
	public Payment recordPayment(Integer invoiceId, Payment.PaymentMethod method, BigDecimal amount, String txRef,
			AppUser customer) {
		String normalizedTxRef = normalizeTransactionReference(method, txRef);
		Invoice inv = invoiceRepo.findByIdForUpdate(invoiceId)
				.orElseThrow(() -> new NotFoundException("Invoice not found."));
		validatePaymentActor(inv, customer, method);
		Payment existingPayment = findMatchingSuccessfulPayment(inv.getInvoiceId(), normalizedTxRef,
				Payment.PaymentPurpose.FINAL_INVOICE);
		if (existingPayment != null) {
			validateExistingPayment(existingPayment, method, amount);
			refreshInvoicePaymentStatus(inv);
			return existingPayment;
		}
		validatePayment(inv, method, amount, normalizedTxRef, false, Payment.PaymentPurpose.FINAL_INVOICE);

		Payment p = new Payment();
		p.setInvoice(inv);
		p.setPaymentMethod(method);
		p.setAmount(amount);
		p.setTransactionReference(normalizedTxRef);
		p.setGatewayProvider(resolveGatewayProvider(method));
		p.setPaymentStatus(Payment.PaymentStatus.SUCCESS);
		p.setPaymentPurpose(Payment.PaymentPurpose.FINAL_INVOICE);
		log.info("Payment recorded invoiceId={} amount={} method={}", invoiceId, amount, method);
		paymentRepo.save(p);
		refreshInvoicePaymentStatus(inv);

		if (customer != null)
			notificationService.send(customer, Notification.NotificationType.PAYMENT_REMINDER, "Payment Received",
					"Payment of \u20b9" + amount + " received for Invoice #" + invoiceId, null);
		return p;
	}

	public void validateDigitalPaymentIntent(Invoice invoice, Payment.PaymentMethod method, BigDecimal amount,
			AppUser actor) {
		validatePaymentActor(invoice, actor, method);
		validatePayment(invoice, method, amount, "gateway-initiation", true, Payment.PaymentPurpose.FINAL_INVOICE);
	}

	public BigDecimal successfulAmountByPurpose(Invoice invoice, Payment.PaymentPurpose purpose) {
		if (invoice == null || invoice.getInvoiceId() == null) {
			return BigDecimal.ZERO;
		}
		return paymentRepo.sumSuccessfulAmountByInvoiceIdAndPurpose(invoice.getInvoiceId(), purpose);
	}

	public BigDecimal remainingBalance(Invoice invoice) {
		BigDecimal total = invoice.getTotalAmount() != null ? invoice.getTotalAmount() : BigDecimal.ZERO;
		if (invoice.getInvoiceId() == null) {
			return total;
		}
		BigDecimal paid = paymentRepo.sumSuccessfulAmountByInvoiceId(invoice.getInvoiceId());
		BigDecimal remaining = total.subtract(paid);
		return remaining.compareTo(BigDecimal.ZERO) > 0 ? remaining : BigDecimal.ZERO;
	}

	public Invoice refreshInvoicePaymentStatus(Invoice invoice) {
		BigDecimal paid = paymentRepo.sumSuccessfulAmountByInvoiceId(invoice.getInvoiceId());
		boolean serviceCompleted = invoice.getServiceRecord() != null
				&& invoice.getServiceRecord().getStatus() == ServiceRecord.ServiceStatus.COMPLETED;
		if (paid.compareTo(BigDecimal.ZERO) <= 0) {
			invoice.setPaymentStatus(Invoice.PaymentStatus.PENDING);
		} else if (!serviceCompleted) {
			invoice.setPaymentStatus(Invoice.PaymentStatus.PARTIALLY_PAID);
		} else if (paid.compareTo(invoice.getTotalAmount()) >= 0) {
			invoice.setPaymentStatus(Invoice.PaymentStatus.PAID);
		} else {
			invoice.setPaymentStatus(Invoice.PaymentStatus.PARTIALLY_PAID);
		}
		return invoiceRepo.save(invoice);
	}

	private String normalizeTransactionReference(Payment.PaymentMethod method, String txRef) {
		if (method == Payment.PaymentMethod.CASH) {
			return CASH_REFERENCE;
		}
		if (txRef == null || txRef.isBlank()) {
			return null;
		}
		return txRef.trim();
	}

	private String resolveGatewayProvider(Payment.PaymentMethod method) {
		return method == Payment.PaymentMethod.CASH ? GATEWAY_PROVIDER_CASH : GATEWAY_PROVIDER_RAZORPAY;
	}

	private Payment findMatchingSuccessfulPayment(Integer invoiceId, String txRef, Payment.PaymentPurpose purpose) {
		if (invoiceId == null || txRef == null) {
			return null;
		}
		return paymentRepo.findFirstByInvoiceInvoiceIdAndTransactionReferenceAndPaymentPurposeAndPaymentStatus(
				invoiceId, txRef, purpose, Payment.PaymentStatus.SUCCESS).orElse(null);
	}

	private void validateExistingPayment(Payment existingPayment, Payment.PaymentMethod method, BigDecimal amount) {
		if (existingPayment.getPaymentMethod() != method || existingPayment.getAmount() == null
				|| existingPayment.getAmount().compareTo(amount) != 0) {
			throw new ConflictException("A payment with this transaction reference was already recorded.");
		}
	}

	private void validatePaymentActor(Invoice invoice, AppUser customer, Payment.PaymentMethod method) {
		if (customer == null || customer.getRole() == null || customer.getRole().getRoleName() == null) {
			throw new BusinessException("Authenticated user is required to record invoice payment.");
		}
		com.vserv.entity.Role.RoleName roleName = customer.getRole().getRoleName();
		if (roleName == com.vserv.entity.Role.RoleName.ADMIN) {
			return;
		}
		if (roleName != com.vserv.entity.Role.RoleName.CUSTOMER) {
			throw new BusinessException("Only admins or the customer can record invoice payment.");
		}
		Integer ownerId = invoice.getServiceRecord() != null && invoice.getServiceRecord().getBooking() != null
				&& invoice.getServiceRecord().getBooking().getVehicle() != null
				&& invoice.getServiceRecord().getBooking().getVehicle().getUser() != null
						? invoice.getServiceRecord().getBooking().getVehicle().getUser().getUserId()
						: null;
		if (ownerId == null || !ownerId.equals(customer.getUserId())) {
			throw new BusinessException("Only the customer who owns this invoice can pay it.");
		}
		if (method == Payment.PaymentMethod.CASH) {
			throw new BusinessException("Customers can settle invoices only through digital payment methods.");
		}
	}

	private void validatePayment(Invoice invoice, Payment.PaymentMethod method, BigDecimal amount, String txRef,
			boolean gatewayInitiation, Payment.PaymentPurpose purpose) {
		if (method == null) {
			throw new BusinessException("Payment method is required.");
		}
		if (amount == null) {
			throw new BusinessException("Payment amount is required.");
		}
		if (amount.compareTo(BigDecimal.ZERO) <= 0) {
			throw new BusinessException("Payment amount must be greater than 0.");
		}
		BigDecimal remaining = remainingBalance(invoice);
		log.warn("Payment attempt on already-paid invoiceId={}", invoice.getInvoiceId());
		if (remaining.compareTo(BigDecimal.ZERO) <= 0 || invoice.getPaymentStatus() == Invoice.PaymentStatus.PAID) {
			throw new ConflictException("This invoice is already fully paid.");
		}
		if (amount.compareTo(remaining) > 0) {
			throw new BusinessException("Payment amount cannot exceed the remaining balance.");
		}
		if (!gatewayInitiation && method != Payment.PaymentMethod.CASH && (txRef == null || txRef.isBlank())) {
			throw new BusinessException("Transaction reference is required for non-cash payments.");
		}
		if (txRef != null && !txRef.isBlank() && !TXN_REF_PATTERN.matcher(txRef.trim()).matches()) {
			throw new BusinessException(
					"Transaction reference must be 4-40 characters and use only letters, numbers, . _ - / #.");
		}
	}
}

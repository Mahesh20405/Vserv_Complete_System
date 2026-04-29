package com.vserv.features.invoice.controller;

import com.vserv.features.invoice.mapper.InvoiceMapper;

import com.vserv.core.pagination.PaginationUtils;
import com.vserv.features.invoice.service.InvoiceService;

import com.vserv.entity.AppUser;
import com.vserv.entity.Invoice;

import com.vserv.features.invoice.dto.InvoiceDto;
import com.vserv.features.invoice.dto.PaymentRequest;
import com.vserv.features.paymentgateway.dto.CheckoutOrderRequest;
import com.vserv.features.paymentgateway.dto.CheckoutOrderResponse;
import com.vserv.features.paymentgateway.dto.VerifyPaymentRequest;
import com.vserv.features.paymentgateway.service.RazorpayGatewayService;
import com.vserv.core.exception.ForbiddenException;
import com.vserv.core.util.SecurityUtils;
import com.vserv.entity.ServiceRecord;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.List;
import com.vserv.core.exception.BusinessException;
import java.util.Map;

@RestController
@RequestMapping("/api/invoices")
public class InvoiceController {

	private final InvoiceService invoiceService;
	private final SecurityUtils securityUtils;
	private final RazorpayGatewayService razorpayGatewayService;

	public InvoiceController(InvoiceService invoiceService, SecurityUtils securityUtils,
			RazorpayGatewayService razorpayGatewayService) {
		this.invoiceService = invoiceService;
		this.securityUtils = securityUtils;
		this.razorpayGatewayService = razorpayGatewayService;
	}

	private void requireCompletedInvoice(Invoice invoice) {
		if (invoice == null || invoice.getServiceRecord() == null
				|| invoice.getServiceRecord().getStatus() != ServiceRecord.ServiceStatus.COMPLETED) {
			throw new BusinessException("Invoice will be available after the service is completed.");
		}
	}

	/** GET /api/invoices?status= */
	@GetMapping
	public ResponseEntity<?> listInvoices(@RequestParam(required = false) Invoice.PaymentStatus status,
			@RequestParam(required = false) String q,
			@RequestParam(required = false) String paymentMethod,
			@RequestParam(required = false, defaultValue = "newest") String sort,
			@RequestParam(required = false) Integer page,
			@RequestParam(required = false) Integer size) {
		AppUser me = securityUtils.requireCurrentUser();

		List<Invoice> invoices = securityUtils.isAdmin()
				? (status != null ? invoiceService.findByStatus(status) : invoiceService.findAll())
				: status != null ? invoiceService.findByCustomerAndStatus(me.getUserId(), status)
						: invoiceService.findByCustomer(me.getUserId());

		List<InvoiceDto> items = invoices.stream().map(i -> InvoiceMapper.toDto(i, invoiceService.remainingBalance(i)))
				.filter(dto -> matchesInvoiceQuery(dto, q))
				.filter(dto -> paymentMethod == null || paymentMethod.isBlank()
						|| paymentMethod.equalsIgnoreCase(dto.getPaymentMethod()))
				.sorted(invoiceComparator(sort)).toList();
		if (PaginationUtils.isPaged(page, size)) {
			return ResponseEntity.status(HttpStatus.OK).body(PaginationUtils.paginate(items, page, size));
		}
		return ResponseEntity.status(HttpStatus.OK).body(items);
	}

	/** GET /api/invoices/{id} */
	@GetMapping("/{id}")
	public ResponseEntity<InvoiceDto> getInvoiceById(@PathVariable Integer id) {
		securityUtils.requireCurrentUser();
		Invoice invoice = invoiceService.findById(id).orElseThrow(() -> new BusinessException("Invoice not found."));
		requireCompletedInvoice(invoice);
		securityUtils.requireInvoiceAccess(invoice);
		return ResponseEntity.status(HttpStatus.OK)
				.body(InvoiceMapper.toDto(invoice, invoiceService.remainingBalance(invoice)));
	}

	/** POST /api/invoices/{id}/pay */
	@PostMapping("/{id}/pay")
	public ResponseEntity<Map<String, String>> pay(@PathVariable Integer id,
			@Valid @RequestBody PaymentRequest req) {
		var me = securityUtils.requireCurrentUser();
		if (!securityUtils.isAdmin() && !securityUtils.isCustomer()) {
			throw new ForbiddenException("Only admins or customers can record invoice payments.");
		}
		Invoice invoice = invoiceService.findById(id).orElseThrow(() -> new BusinessException("Invoice not found."));
		requireCompletedInvoice(invoice);
		securityUtils.requireInvoiceAccess(invoice);
		invoiceService.recordPayment(id, req.getPaymentMethod(), req.getAmount(), req.getTransactionReference(), me);
		return ResponseEntity.status(HttpStatus.OK).body(Map.of("message", "Payment recorded successfully."));
	}

	@PostMapping("/{id}/checkout/order")
	public ResponseEntity<CheckoutOrderResponse> createCheckoutOrder(@PathVariable Integer id,
			@Valid @RequestBody CheckoutOrderRequest req) {
		var me = securityUtils.requireCurrentUser();
		if (!securityUtils.isAdmin() && !securityUtils.isCustomer()) {
			throw new ForbiddenException("Only admins or customers can create invoice payments.");
		}
		if (req.getPaymentMethod() == null || req.getPaymentMethod() == com.vserv.entity.Payment.PaymentMethod.CASH) {
			throw new ForbiddenException("Razorpay checkout supports only digital payment methods.");
		}
		Invoice invoice = invoiceService.findById(id).orElseThrow(() -> new BusinessException("Invoice not found."));
		requireCompletedInvoice(invoice);
		securityUtils.requireInvoiceAccess(invoice);
		invoiceService.validateDigitalPaymentIntent(invoice, req.getPaymentMethod(), req.getAmount(), me);
		return ResponseEntity.status(HttpStatus.OK)
				.body(razorpayGatewayService.createOrder(req.getAmount(),
						req.getDescription() != null && !req.getDescription().isBlank() ? req.getDescription().trim()
								: "Invoice Payment",
						"invoice-" + id + "-" + System.currentTimeMillis(),
						Map.of("invoiceId", String.valueOf(id), "userId", String.valueOf(me.getUserId()))));
	}

	@PostMapping("/{id}/checkout/verify")
	public ResponseEntity<Map<String, String>> verifyCheckout(@PathVariable Integer id,
			@Valid @RequestBody VerifyPaymentRequest req) {
		var me = securityUtils.requireCurrentUser();
		if (!securityUtils.isAdmin() && !securityUtils.isCustomer()) {
			throw new ForbiddenException("Only admins or customers can verify invoice payments.");
		}
		Invoice invoice = invoiceService.findById(id).orElseThrow(() -> new BusinessException("Invoice not found."));
		requireCompletedInvoice(invoice);
		securityUtils.requireInvoiceAccess(invoice);
		razorpayGatewayService.verifySignature(req.getRazorpayOrderId(), req.getRazorpayPaymentId(),
				req.getRazorpaySignature());
		invoiceService.recordPayment(id, req.getPaymentMethod(), req.getAmount(), req.getRazorpayPaymentId(), me);
		return ResponseEntity.status(HttpStatus.OK).body(Map.of("message", "Payment recorded successfully.",
				"transactionReference", req.getRazorpayPaymentId()));
	}

	private boolean matchesInvoiceQuery(InvoiceDto dto, String q) {
		String normalized = q == null ? "" : q.trim().toLowerCase();
		if (normalized.isBlank()) {
			return true;
		}
		String haystack = String.join(" ", dto.getInvoiceNumber() == null ? "" : dto.getInvoiceNumber(),
				dto.getServiceName() == null ? "" : dto.getServiceName(),
				dto.getCustomerName() == null ? "" : dto.getCustomerName(),
				dto.getTransactionReference() == null ? "" : dto.getTransactionReference()).toLowerCase();
		return haystack.contains(normalized);
	}

	private Comparator<InvoiceDto> invoiceComparator(String sort) {
		return switch (sort == null ? "newest" : sort) {
		case "oldest" ->
			Comparator.comparing(InvoiceDto::getInvoiceDate, Comparator.nullsLast(Comparator.naturalOrder()));
		default -> Comparator.comparing(InvoiceDto::getInvoiceDate, Comparator.nullsLast(Comparator.reverseOrder()));
		};
	}
}

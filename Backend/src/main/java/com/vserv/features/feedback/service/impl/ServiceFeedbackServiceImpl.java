package com.vserv.features.feedback.service.impl;

import com.vserv.features.feedback.mapper.ServiceFeedbackMapper;

import com.vserv.features.feedback.repository.ServiceFeedbackRepository;

import com.vserv.features.feedback.service.ServiceFeedbackService;

import com.vserv.core.exception.BusinessException;
import com.vserv.core.exception.ConflictException;
import com.vserv.core.exception.ForbiddenException;
import com.vserv.core.exception.NotFoundException;
import com.vserv.entity.AppUser;
import com.vserv.entity.Invoice;
import com.vserv.entity.ServiceBooking;
import com.vserv.entity.ServiceFeedback;
import com.vserv.entity.ServiceRecord;
import com.vserv.features.feedback.dto.ServiceFeedbackStatusDto;
import com.vserv.features.invoice.repository.InvoiceRepository;
import com.vserv.features.servicerecord.repository.ServiceRecordRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ServiceFeedbackServiceImpl implements ServiceFeedbackService {
	private static final Logger log = LoggerFactory.getLogger(ServiceFeedbackServiceImpl.class);

	private final ServiceFeedbackRepository feedbackRepo;
	private final ServiceRecordRepository serviceRecordRepo;
	private final InvoiceRepository invoiceRepo;

	public ServiceFeedbackServiceImpl(ServiceFeedbackRepository feedbackRepo, ServiceRecordRepository serviceRecordRepo,
			InvoiceRepository invoiceRepo) {
		this.feedbackRepo = feedbackRepo;
		this.serviceRecordRepo = serviceRecordRepo;
		this.invoiceRepo = invoiceRepo;
	}

	public List<ServiceFeedback> findAll() {
		return feedbackRepo.findAllByOrderBySubmittedAtDesc();
	}

	public ServiceFeedbackStatusDto getStatus(Integer serviceId, AppUser actor, boolean isAdmin) {
		ServiceRecord record = requireServiceRecord(serviceId);
		if (!isAdmin) {
			requireCustomerOwnership(record, actor);
		}
		Invoice invoice = invoiceRepo.findByServiceRecordServiceId(serviceId).orElse(null);
		ServiceFeedback feedback = feedbackRepo.findByServiceRecordServiceId(serviceId).orElse(null);
		boolean eligible = !isAdmin && isEligible(record, invoice, feedback);
		return new ServiceFeedbackStatusDto(serviceId, record.getStatus() != null ? record.getStatus().name() : null,
				invoice != null && invoice.getPaymentStatus() != null ? invoice.getPaymentStatus().name() : null,
				eligible, feedback != null, feedback != null ? ServiceFeedbackMapper.toDto(feedback) : null);
	}

	@Transactional
	public ServiceFeedback create(Integer serviceId, Integer rating, String feedbackText, AppUser customer) {
		ServiceRecord record = requireServiceRecord(serviceId);
		requireCustomerOwnership(record, customer);
		Invoice invoice = invoiceRepo.findByServiceRecordServiceId(serviceId)
				.orElseThrow(() -> new BusinessException("Feedback can only be submitted after invoice generation."));
		ServiceFeedback existing = feedbackRepo.findByServiceRecordServiceId(serviceId).orElse(null);
		log.warn("Feedback submission rejected serviceId={} customerId={} – not eligible", serviceId,
				customer.getUserId());
		if (!isEligible(record, invoice, existing)) {
			if (existing != null) {
				throw new ConflictException("Feedback has already been submitted for this service.");
			}
			if (record.getStatus() != ServiceRecord.ServiceStatus.COMPLETED) {
				throw new BusinessException("Feedback can only be submitted for completed services.");
			}
			throw new BusinessException("Feedback can only be submitted after the invoice is fully paid.");
		}

		ServiceFeedback feedback = new ServiceFeedback();
		feedback.setServiceRecord(record);
		feedback.setCustomer(customer);
		feedback.setRating(rating);
		feedback.setFeedbackText(trimToNull(feedbackText));
		log.info("Feedback submitted serviceId={} customerId={} rating={}", serviceId, customer.getUserId(), rating);
		return feedbackRepo.save(feedback);
	}

	private ServiceRecord requireServiceRecord(Integer serviceId) {
		return serviceRecordRepo.findById(serviceId)
				.orElseThrow(() -> new NotFoundException("Service record not found."));
	}

	private void requireCustomerOwnership(ServiceRecord record, AppUser actor) {
		ServiceBooking booking = record.getBooking();
		Integer ownerId = booking != null && booking.getVehicle() != null && booking.getVehicle().getUser() != null
				? booking.getVehicle().getUser().getUserId()
				: booking != null ? booking.getArchivedOwnerId() : null;
		if (actor == null || actor.getUserId() == null || ownerId == null || !ownerId.equals(actor.getUserId())) {
			throw new ForbiddenException("You can only view or submit feedback for your own completed services.");
		}
	}

	private boolean isEligible(ServiceRecord record, Invoice invoice, ServiceFeedback feedback) {
		return feedback == null && record.getStatus() == ServiceRecord.ServiceStatus.COMPLETED && invoice != null
				&& invoice.getPaymentStatus() == Invoice.PaymentStatus.PAID;
	}

	private String trimToNull(String value) {
		if (value == null) {
			return null;
		}
		String trimmed = value.trim();
		return trimmed.isEmpty() ? null : trimmed;
	}
}

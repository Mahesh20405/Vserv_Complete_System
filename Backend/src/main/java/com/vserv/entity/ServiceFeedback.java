package com.vserv.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "service_feedback")
public class ServiceFeedback {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "feedback_id")
	private Integer feedbackId;

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "service_id", unique = true, nullable = false)
	private ServiceRecord serviceRecord;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "customer_id", nullable = false)
	private AppUser customer;

	@Column(name = "rating")
	private Integer rating;

	@Column(name = "feedback_text", columnDefinition = "TEXT")
	private String feedbackText;

	@Column(name = "submitted_at", nullable = false)
	private LocalDateTime submittedAt = LocalDateTime.now();

	public Integer getFeedbackId() {
		return feedbackId;
	}

	public void setFeedbackId(Integer feedbackId) {
		this.feedbackId = feedbackId;
	}

	public ServiceRecord getServiceRecord() {
		return serviceRecord;
	}

	public void setServiceRecord(ServiceRecord serviceRecord) {
		this.serviceRecord = serviceRecord;
	}

	public AppUser getCustomer() {
		return customer;
	}

	public void setCustomer(AppUser customer) {
		this.customer = customer;
	}

	public Integer getRating() {
		return rating;
	}

	public void setRating(Integer rating) {
		this.rating = rating;
	}

	public String getFeedbackText() {
		return feedbackText;
	}

	public void setFeedbackText(String feedbackText) {
		this.feedbackText = feedbackText;
	}

	public LocalDateTime getSubmittedAt() {
		return submittedAt;
	}

	public void setSubmittedAt(LocalDateTime submittedAt) {
		this.submittedAt = submittedAt;
	}
}

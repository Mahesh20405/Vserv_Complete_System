package com.vserv.features.feedback.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class CreateServiceFeedbackRequest {

	@NotNull(message = "Rating is required.")
	@Min(value = 1, message = "Rating must be between 1 and 5.")
	@Max(value = 5, message = "Rating must be between 1 and 5.")
	private Integer rating;

	@Size(max = 2000, message = "Feedback must be at most 2000 characters.")
	private String feedbackText;

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
}

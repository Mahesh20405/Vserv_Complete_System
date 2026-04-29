package com.vserv.features.feedback.repository;

import com.vserv.entity.ServiceFeedback;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ServiceFeedbackRepository extends JpaRepository<ServiceFeedback, Integer> {
	Optional<ServiceFeedback> findByServiceRecordServiceId(Integer serviceId);

	boolean existsByServiceRecordServiceId(Integer serviceId);

	List<ServiceFeedback> findAllByOrderBySubmittedAtDesc();
}

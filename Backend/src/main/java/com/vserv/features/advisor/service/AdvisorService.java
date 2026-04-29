package com.vserv.features.advisor.service;

import com.vserv.entity.AppUser;
import com.vserv.entity.ServiceAdvisor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface AdvisorService {
	int MAX_LOAD = 5;
	BigDecimal DEFAULT_OVERTIME_RATE = BigDecimal.valueOf(500);

	List<ServiceAdvisor> findAll();

	List<ServiceAdvisor> findActive();

	List<ServiceAdvisor> findAvailable();

	List<ServiceAdvisor> findTopActive();

	Optional<ServiceAdvisor> findById(Integer id);

	AppUser.Status toggleUserStatus(Integer advisorId);

	void incrementLoad(Integer advisorId);

	void decrementLoad(Integer advisorId);

	ServiceAdvisor update(Integer advisorId, String specialization, BigDecimal overtimeRate,
			ServiceAdvisor.AvailabilityStatus newStatus);
}

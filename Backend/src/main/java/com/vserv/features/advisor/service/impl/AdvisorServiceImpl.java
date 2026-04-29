package com.vserv.features.advisor.service.impl;

import com.vserv.core.status.StatusToggleGuard;
import com.vserv.core.status.StatusToggleGuardService;
import com.vserv.features.advisor.repository.ServiceAdvisorRepository;

import com.vserv.features.advisor.service.AdvisorService;

import com.vserv.entity.AppUser;
import com.vserv.entity.ServiceAdvisor;
import com.vserv.core.exception.BusinessException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class AdvisorServiceImpl implements AdvisorService {
	private static final Logger log = LoggerFactory.getLogger(AdvisorServiceImpl.class);

	public static final int MAX_LOAD = 5;
	public static final BigDecimal DEFAULT_OVERTIME_RATE = BigDecimal.valueOf(500);

	private final ServiceAdvisorRepository advisorRepo;
	private final StatusToggleGuardService statusToggleGuardService;

	public AdvisorServiceImpl(ServiceAdvisorRepository advisorRepo, StatusToggleGuardService statusToggleGuardService) {
		this.advisorRepo = advisorRepo;
		this.statusToggleGuardService = statusToggleGuardService;
	}

	public List<ServiceAdvisor> findAll() {
		return advisorRepo.findByUserIsDeletedFalse();
	}

	public List<ServiceAdvisor> findActive() {
		return advisorRepo.findByUserIsDeletedFalseAndUserStatusAndAvailabilityStatusNot(AppUser.Status.ACTIVE,
				ServiceAdvisor.AvailabilityStatus.RESIGNED);
	}

	public List<ServiceAdvisor> findAvailable() {
		return advisorRepo.findByAvailabilityStatusAndUserIsDeletedFalseAndUserStatus(
				ServiceAdvisor.AvailabilityStatus.AVAILABLE, AppUser.Status.ACTIVE);
	}

	public List<ServiceAdvisor> findTopActive() {
		return advisorRepo.findTop5ByUserIsDeletedFalseAndUserStatusAndAvailabilityStatusNotOrderByCurrentLoadDesc(
				AppUser.Status.ACTIVE, ServiceAdvisor.AvailabilityStatus.RESIGNED);
	}

	public Optional<ServiceAdvisor> findById(Integer id) {
		return advisorRepo.findByAdvisorIdAndUserIsDeletedFalse(id);
	}

	@Transactional
	public AppUser.Status toggleUserStatus(Integer advisorId) {
		log.info("Toggling user status for advisorId={}", advisorId);
		ServiceAdvisor advisor = advisorRepo.findByAdvisorIdAndUserIsDeletedFalse(advisorId)
				.orElseThrow(() -> new BusinessException("Advisor not found."));
		AppUser user = advisor.getUser();
		if (user == null || Boolean.TRUE.equals(user.getIsDeleted())) {
			throw new BusinessException("Advisor not found.");
		}
		if (user.getStatus() == AppUser.Status.ACTIVE) {
			StatusToggleGuard guard = statusToggleGuardService.evaluateAdvisorToggle(advisor);
			if (!guard.isAllowed()) {
				throw new BusinessException(guard.getReason());
			}
		}
		user.setStatus(user.getStatus() == AppUser.Status.ACTIVE ? AppUser.Status.INACTIVE : AppUser.Status.ACTIVE);
		advisorRepo.save(advisor);
		return user.getStatus();
	}

	@Transactional
	public void incrementLoad(Integer advisorId) {
		advisorRepo.findById(advisorId).ifPresent(a -> {
			int current = (a.getCurrentLoad() == null ? 0 : a.getCurrentLoad());
			log.warn("Advisor {} has reached max load {}", advisorId, MAX_LOAD);
			if (current >= MAX_LOAD)
				throw new BusinessException(
						"Advisor has reached maximum load (" + MAX_LOAD + " services). Assign to another advisor.");
			int newLoad = current + 1;
			a.setCurrentLoad(newLoad);
			a.setLastAssignedAt(LocalDateTime.now());
			if (a.getAvailabilityStatus() == ServiceAdvisor.AvailabilityStatus.AVAILABLE)
				a.setAvailabilityStatus(ServiceAdvisor.AvailabilityStatus.ASSIGNED);
			advisorRepo.save(a);
		});
	}

	@Transactional
	public void decrementLoad(Integer advisorId) {
		advisorRepo.findById(advisorId).ifPresent(a -> {
			int newLoad = Math.max(0, (a.getCurrentLoad() == null ? 0 : a.getCurrentLoad()) - 1);
			a.setCurrentLoad(newLoad);
			if (newLoad == 0 && a.getAvailabilityStatus() == ServiceAdvisor.AvailabilityStatus.ASSIGNED)
				a.setAvailabilityStatus(ServiceAdvisor.AvailabilityStatus.AVAILABLE);
			advisorRepo.save(a);
		});
	}

	@Transactional
	public ServiceAdvisor update(Integer advisorId, String specialization, BigDecimal overtimeRate,
			ServiceAdvisor.AvailabilityStatus newStatus) {
		ServiceAdvisor a = advisorRepo.findById(advisorId)
				.orElseThrow(() -> new BusinessException("Advisor not found."));
		if (a.getAvailabilityStatus() == ServiceAdvisor.AvailabilityStatus.ASSIGNED
				&& newStatus == ServiceAdvisor.AvailabilityStatus.AVAILABLE) {
			log.warn("Cannot manually set ASSIGNED advisor {} to AVAILABLE", advisorId);
			throw new BusinessException("Cannot manually set an ASSIGNED advisor to AVAILABLE. "
					+ "This happens automatically when all their services are completed.");
		}
		a.setSpecialization(specialization);
		a.setOvertimeRate(overtimeRate);
		a.setAvailabilityStatus(newStatus);
		return advisorRepo.save(a);
	}
}

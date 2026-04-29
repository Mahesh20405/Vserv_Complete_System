package com.vserv.features.advisor.repository;

import com.vserv.entity.AppUser;
import com.vserv.entity.ServiceAdvisor;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.List;

public interface ServiceAdvisorRepository extends JpaRepository<ServiceAdvisor, Integer> {
	List<ServiceAdvisor> findByUserIsDeletedFalse();

	List<ServiceAdvisor> findByUserIsDeletedFalseAndUserStatus(AppUser.Status status);

	List<ServiceAdvisor> findByUserIsDeletedFalseAndUserStatusAndAvailabilityStatusNot(AppUser.Status userStatus,
			ServiceAdvisor.AvailabilityStatus advisorStatus);

	List<ServiceAdvisor> findByAvailabilityStatusAndUserIsDeletedFalseAndUserStatus(
			ServiceAdvisor.AvailabilityStatus advisorStatus, AppUser.Status userStatus);

	List<ServiceAdvisor> findTop5ByUserIsDeletedFalseAndUserStatusAndAvailabilityStatusNotOrderByCurrentLoadDesc(
			AppUser.Status userStatus, ServiceAdvisor.AvailabilityStatus advisorStatus);

	Optional<ServiceAdvisor> findByAdvisorIdAndUserIsDeletedFalse(Integer advisorId);

	List<ServiceAdvisor> findByAvailabilityStatusNot(ServiceAdvisor.AvailabilityStatus status);

	List<ServiceAdvisor> findByAvailabilityStatus(ServiceAdvisor.AvailabilityStatus status);

	List<ServiceAdvisor> findTop5ByAvailabilityStatusNotOrderByCurrentLoadDesc(
			ServiceAdvisor.AvailabilityStatus status);

	Optional<ServiceAdvisor> findByUserUserId(Integer userId);

	List<ServiceAdvisor> findByUserUserIdIn(List<Integer> userIds);
}

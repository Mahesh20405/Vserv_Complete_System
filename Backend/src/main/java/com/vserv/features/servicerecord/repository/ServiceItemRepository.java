package com.vserv.features.servicerecord.repository;

import com.vserv.entity.ServiceItem;
import com.vserv.entity.ServiceRecord;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface ServiceItemRepository extends JpaRepository<ServiceItem, Integer> {
	List<ServiceItem> findByServiceRecord(ServiceRecord record);

	@Modifying
	@Transactional
	void deleteByServiceRecord(ServiceRecord record);
}

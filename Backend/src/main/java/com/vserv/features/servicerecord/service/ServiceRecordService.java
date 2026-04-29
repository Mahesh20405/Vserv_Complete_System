package com.vserv.features.servicerecord.service;

import com.vserv.entity.AppUser;
import com.vserv.entity.Invoice;
import com.vserv.entity.ServiceBooking;
import com.vserv.entity.ServiceItem;
import com.vserv.entity.ServiceRecord;
import com.vserv.entity.WorkItemCatalog;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface ServiceRecordService {
	Optional<ServiceRecord> findById(Integer id);

	Optional<ServiceRecord> findByBookingId(Integer bid);

	Map<Integer, ServiceRecord> findByBookingIds(Collection<Integer> bookingIds);

	List<ServiceRecord> findByAdvisor(AppUser u);

	List<ServiceRecord> findAll();

	List<ServiceRecord> findByStatus(ServiceRecord.ServiceStatus status);

	long countByStatus(ServiceRecord.ServiceStatus s);

	List<ServiceRecord> findByAdvisorAndStatus(AppUser advisor, ServiceRecord.ServiceStatus status);

	List<ServiceItem> getItems(ServiceRecord record);

	ServiceRecord assignAdvisor(Integer bookingId, AppUser advisor);

	ServiceRecord createPendingRecord(ServiceBooking booking);

	ServiceRecord reassignAdvisor(Integer bookingId, AppUser newAdvisor);

	boolean unassignAdvisor(Integer bookingId);

	void startService(Integer serviceId);

	void saveItems(Integer serviceId, List<ServiceItem> items);

	ServiceRecord updateRemarks(Integer serviceId, String remarks);

	WorkItemCatalog requireWorkItem(Integer workItemId);

	Invoice completeService(Integer serviceId, BigDecimal actualHours, String remarks, List<ServiceItem> newItems,
			AppUser advisor);
}

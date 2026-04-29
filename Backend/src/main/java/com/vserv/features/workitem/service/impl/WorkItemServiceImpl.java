package com.vserv.features.workitem.service.impl;

import com.vserv.features.workitem.repository.WorkItemCatalogRepository;

import com.vserv.features.workitem.service.WorkItemService;

import com.vserv.entity.WorkItemCatalog;
import com.vserv.core.exception.BusinessException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class WorkItemServiceImpl implements WorkItemService {
	private static final Logger log = LoggerFactory.getLogger(WorkItemServiceImpl.class);

	private final WorkItemCatalogRepository workItemRepo;

	public WorkItemServiceImpl(WorkItemCatalogRepository workItemRepo) {
		this.workItemRepo = workItemRepo;
	}

	public List<WorkItemCatalog> findAll() {
		return workItemRepo.findAll();
	}

	public List<WorkItemCatalog> findActive() {
		return workItemRepo.findByIsActiveTrue();
	}

	public Optional<WorkItemCatalog> findById(Integer id) {
		return workItemRepo.findById(id);
	}

	@Transactional
	public WorkItemCatalog create(String itemName, WorkItemCatalog.ItemType itemType,
			WorkItemCatalog.ItemCarType carType, BigDecimal unitPrice, String description) {
		WorkItemCatalog w = new WorkItemCatalog();
		w.setItemName(itemName);
		w.setItemType(itemType);
		w.setCarType(carType);
		w.setUnitPrice(unitPrice);
		w.setDescription(description);
		log.info("WorkItem created name={} type={}", w.getItemName(), w.getItemType());
		return workItemRepo.save(w);
	}

	@Transactional
	public WorkItemCatalog update(Integer id, String itemName, WorkItemCatalog.ItemType itemType,
			WorkItemCatalog.ItemCarType carType, BigDecimal unitPrice, String description) {
		WorkItemCatalog w = workItemRepo.findById(id).orElseThrow(() -> new BusinessException("Work item not found."));
		if (itemName != null)
			w.setItemName(itemName);
		if (itemType != null)
			w.setItemType(itemType);
		if (carType != null)
			w.setCarType(carType);
		if (unitPrice != null)
			w.setUnitPrice(unitPrice);
		if (description != null)
			w.setDescription(description);
		log.info("WorkItem updated id={}", id);
		return workItemRepo.save(w);
	}

	@Transactional
	public void toggle(Integer id) {
		log.info("Toggling WorkItem id={}", id);
		workItemRepo.findById(id).ifPresent(w -> {
			w.setIsActive(!w.getIsActive());
			workItemRepo.save(w);
		});
	}
}

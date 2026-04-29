package com.vserv.features.workitem.service;

import com.vserv.entity.WorkItemCatalog;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface WorkItemService {
	List<WorkItemCatalog> findAll();

	List<WorkItemCatalog> findActive();

	Optional<WorkItemCatalog> findById(Integer id);

	WorkItemCatalog create(String itemName, WorkItemCatalog.ItemType itemType, WorkItemCatalog.ItemCarType carType,
			BigDecimal unitPrice, String description);

	WorkItemCatalog update(Integer id, String itemName, WorkItemCatalog.ItemType itemType,
			WorkItemCatalog.ItemCarType carType, BigDecimal unitPrice, String description);

	void toggle(Integer id);
}

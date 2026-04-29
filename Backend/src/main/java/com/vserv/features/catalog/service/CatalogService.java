package com.vserv.features.catalog.service;

import com.vserv.entity.ServiceCatalog;
import com.vserv.features.catalog.dto.UpdateCatalogRequest;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface CatalogService {
	List<ServiceCatalog> findAllServices();

	List<ServiceCatalog> findActiveServices();

	Optional<ServiceCatalog> findServiceById(Integer id);

	ServiceCatalog updateService(Integer id, UpdateCatalogRequest body);

	ServiceCatalog createService(String name, ServiceCatalog.ServiceType type, String desc, BigDecimal price,
			ServiceCatalog.CatalogCarType carType, BigDecimal hours);

	void toggleService(Integer id);
}

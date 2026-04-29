package com.vserv.features.catalog.service.impl;

import com.vserv.features.catalog.repository.ServiceCatalogRepository;

import com.vserv.features.catalog.service.CatalogService;

import com.vserv.entity.ServiceCatalog;
import com.vserv.core.exception.BusinessException;
import com.vserv.features.catalog.dto.UpdateCatalogRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class CatalogServiceImpl implements CatalogService {
	private static final Logger log = LoggerFactory.getLogger(CatalogServiceImpl.class);

	private final ServiceCatalogRepository catalogRepo;

	public CatalogServiceImpl(ServiceCatalogRepository catalogRepo) {
		this.catalogRepo = catalogRepo;
	}

	public List<ServiceCatalog> findAllServices() {
		return catalogRepo.findAll();
	}

	public List<ServiceCatalog> findActiveServices() {
		return catalogRepo.findByIsActiveTrue();
	}

	public Optional<ServiceCatalog> findServiceById(Integer id) {
		return catalogRepo.findById(id);
	}

	@Transactional
	public ServiceCatalog updateService(Integer id, UpdateCatalogRequest body) {
		ServiceCatalog service = catalogRepo.findById(id)
				.orElseThrow(() -> new BusinessException("Catalog service not found."));
		if (body.getServiceName() != null)
			service.setServiceName(body.getServiceName());
		if (body.getServiceType() != null)
			service.setServiceType(body.getServiceType());
		if (body.getDescription() != null)
			service.setDescription(body.getDescription());
		if (body.getBasePrice() != null)
			service.setBasePrice(body.getBasePrice());
		service.setBookingCharge(ServiceCatalog.DEFAULT_BOOKING_CHARGE);
		if (body.getCarType() != null)
			service.setCarType(body.getCarType());
		if (body.getDurationHours() != null)
			service.setDurationHours(body.getDurationHours());
		log.info("Catalog service updated id={}", id);
		return catalogRepo.save(service);
	}

	@Transactional
	public ServiceCatalog createService(String name, ServiceCatalog.ServiceType type, String desc, BigDecimal price,
			ServiceCatalog.CatalogCarType carType, BigDecimal hours) {
		ServiceCatalog s = new ServiceCatalog();
		s.setServiceName(name);
		s.setServiceType(type);
		s.setDescription(desc);
		s.setBasePrice(price);
		s.setBookingCharge(ServiceCatalog.DEFAULT_BOOKING_CHARGE);
		s.setCarType(carType);
		s.setDurationHours(hours);
		log.info("Catalog service created name={} type={}", s.getServiceName(), s.getServiceType());
		return catalogRepo.save(s);
	}

	@Transactional
	public void toggleService(Integer id) {
		log.info("Toggling catalog service id={}", id);
		catalogRepo.findById(id).ifPresent(s -> {
			s.setIsActive(!s.getIsActive());
			catalogRepo.save(s);
		});
	}
}

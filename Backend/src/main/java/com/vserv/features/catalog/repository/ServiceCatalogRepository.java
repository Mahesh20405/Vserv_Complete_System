package com.vserv.features.catalog.repository;

import com.vserv.entity.ServiceCatalog;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ServiceCatalogRepository extends JpaRepository<ServiceCatalog, Integer> {
	List<ServiceCatalog> findByIsActiveTrue();
}

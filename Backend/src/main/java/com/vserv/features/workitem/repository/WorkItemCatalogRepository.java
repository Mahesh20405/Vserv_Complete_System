package com.vserv.features.workitem.repository;

import com.vserv.entity.WorkItemCatalog;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WorkItemCatalogRepository extends JpaRepository<WorkItemCatalog, Integer> {
	List<WorkItemCatalog> findByIsActiveTrue();
}

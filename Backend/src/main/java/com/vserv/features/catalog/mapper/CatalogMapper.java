package com.vserv.features.catalog.mapper;

import com.vserv.entity.ServiceCatalog;
import com.vserv.features.catalog.dto.CatalogDto;

public final class CatalogMapper {
	private CatalogMapper() {
	}

	public static CatalogDto toDto(ServiceCatalog catalog) {
		return CatalogDto.from(catalog);
	}
}

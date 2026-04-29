package com.vserv.features.workitem.mapper;

import com.vserv.entity.WorkItemCatalog;
import com.vserv.features.workitem.dto.WorkItemDto;

public final class WorkItemMapper {
	private WorkItemMapper() {
	}

	public static WorkItemDto toDto(WorkItemCatalog workItem) {
		return WorkItemDto.from(workItem);
	}
}

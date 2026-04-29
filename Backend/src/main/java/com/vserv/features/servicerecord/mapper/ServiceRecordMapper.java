package com.vserv.features.servicerecord.mapper;

import com.vserv.entity.ServiceItem;
import com.vserv.entity.ServiceRecord;
import com.vserv.features.servicerecord.dto.ServiceItemDto;
import com.vserv.features.servicerecord.dto.ServiceRecordDto;

public final class ServiceRecordMapper {
	private ServiceRecordMapper() {
	}

	public static ServiceRecordDto toDto(ServiceRecord record) {
		return ServiceRecordDto.from(record);
	}

	public static ServiceItemDto toItemDto(ServiceItem item) {
		return ServiceItemDto.from(item);
	}
}

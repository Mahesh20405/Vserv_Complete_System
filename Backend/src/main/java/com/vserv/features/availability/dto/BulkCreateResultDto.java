package com.vserv.features.availability.dto;

public class BulkCreateResultDto {
	private final int created;
	private final int skipped;

	public BulkCreateResultDto(int created, int skipped) {
		this.created = created;
		this.skipped = skipped;
	}

	public int getCreated() {
		return created;
	}

	public int getSkipped() {
		return skipped;
	}
}

package com.vserv.core.pagination;

import java.util.List;

public final class PaginationUtils {

	private static final int DEFAULT_PAGE = 1;
	private static final int DEFAULT_SIZE = 10;
	private static final int MAX_SIZE = 100;

	private PaginationUtils() {
	}

	public static boolean isPaged(Integer page, Integer size) {
		return page != null || size != null;
	}

	public static int resolvePage(Integer page) {
		return page == null || page < 1 ? DEFAULT_PAGE : page;
	}

	public static int resolveSize(Integer size) {
		if (size == null || size < 1) {
			return DEFAULT_SIZE;
		}
		return Math.min(size, MAX_SIZE);
	}

	public static <T> PagedResponse<T> paginate(List<T> items, Integer page, Integer size) {
		int resolvedPage = resolvePage(page);
		int resolvedSize = resolveSize(size);
		int totalElements = items.size();
		int totalPages = Math.max(1, (int) Math.ceil((double) totalElements / resolvedSize));
		int safePage = Math.min(resolvedPage, totalPages);
		int fromIndex = Math.min((safePage - 1) * resolvedSize, totalElements);
		int toIndex = Math.min(fromIndex + resolvedSize, totalElements);
		List<T> content = items.subList(fromIndex, toIndex);

		return new PagedResponse<>(content, safePage, resolvedSize, totalElements, totalPages, safePage == 1,
				safePage >= totalPages, safePage < totalPages, safePage > 1);
	}
}

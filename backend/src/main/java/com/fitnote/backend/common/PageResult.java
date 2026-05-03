package com.fitnote.backend.common;

import java.util.List;
import java.util.function.Function;

public record PageResult<T>(List<T> items, int page, int size, long total, int totalPages) {

    public static <T> PageResult<T> of(List<T> items, int page, int size, long total) {
        int totalPages = size > 0 ? (int) Math.ceil((double) total / size) : 0;
        return new PageResult<>(items, page, size, total, totalPages);
    }

    public static <T> PageResult<T> fromList(List<T> all, int page, int size) {
        long total = all.size();
        int fromIndex = page * size;
        int toIndex = Math.min(fromIndex + size, (int) total);
        List<T> items = fromIndex < total ? all.subList(fromIndex, toIndex) : List.of();
        return of(items, page, size, total);
    }
}

package com.meta.accesscontrol.controller.payload.response;

import org.springframework.data.domain.Page;
import java.util.List;

public record PaginationResponse<T>(
    List<T> content,
    int pageNumber,
    int pageSize,
    long totalElements,
    int totalPages,
    boolean isLast
) {
    public PaginationResponse(Page<T> page) {
        this(
            page.getContent(),
            page.getNumber(),
            page.getSize(),
            page.getTotalElements(),
            page.getTotalPages(),
            page.isLast()
        );
    }
}
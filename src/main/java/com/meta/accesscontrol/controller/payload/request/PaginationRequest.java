package com.meta.accesscontrol.controller.payload.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PaginationRequest {
    @Min(value = 0, message = "Page index must not be less than zero")
    private int page = 0;

    @Min(value = 1, message = "Page size must be at least 1")
    @Max(value = 100, message = "Page size must not be greater than 100")
    private int size = 10;

    private String[] sort;
}

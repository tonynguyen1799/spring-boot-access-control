package com.meta.accesscontrol.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
public final class PaginationUtils {

    private PaginationUtils() {}

    public static List<Sort.Order> buildSortOrders(String[] sortParams, Sort.Order... additionalSorts) {
        String[] normalized = normalizeSortPairs(sortParams);
        List<Sort.Order> orders = Arrays.stream(normalized)
                .filter(StringUtils::hasText)
                .map(PaginationUtils::parseCommaSeparatedOrder)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        if (additionalSorts != null) {
            orders.addAll(Arrays.asList(additionalSorts));
        }

        return orders;
    }

    private static Sort.Order parseCommaSeparatedOrder(String raw) {
        try {
            String[] parts = raw.split(",");
            String property = parts[0].trim();

            if (property.isEmpty() || !property.matches("^[a-zA-Z0-9_.]+$")) {
                throw new IllegalArgumentException("Invalid property format: " + property);
            }

            Sort.Direction direction = (parts.length > 1)
                    ? Sort.Direction.fromString(parts[1].trim().toUpperCase())
                    : Sort.Direction.ASC;
            return new Sort.Order(direction, property);
        } catch (Exception e) {
            log.warn("Invalid sort format found and skipped: '{}'", raw);
            return null;
        }
    }

    public static String[] normalizeSortPairs(String[] sort) {
        if (sort == null || sort.length == 0) {
            return new String[0];
        }

        if (Arrays.stream(sort).noneMatch(s -> s.equalsIgnoreCase("asc") || s.equalsIgnoreCase("desc"))) {
            return sort;
        }

        List<String> merged = new ArrayList<>();
        for (int i = 0; i < sort.length; i += 2) {
            if (i + 1 < sort.length) {
                merged.add(sort[i] + "," + sort[i + 1]);
            } else {
                merged.add(sort[i]);
            }
        }
        return merged.toArray(new String[0]);
    }
}
package com.meta.accesscontrol.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
public final class PaginationUtils {

    /**
     * Private constructor to prevent instantiation of this utility class.
     */
    private PaginationUtils() {
    }

    /**
     * Builds a list of Sort.Order objects from an array of sort parameters.
     *
     * @param sortParams An array of strings, where each string is a sort parameter (e.g., "username,asc").
     * @return A list of Sort.Order objects.
     */
    public static List<Sort.Order> buildSortOrders(String[] sortParams) {
        if (Objects.isNull(sortParams) || sortParams.length == 0) {
            return Collections.emptyList();
        }

        return Arrays.stream(sortParams)
                .filter(StringUtils::hasText)
                .map(PaginationUtils::parseSortOrder)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * Parses a single sort parameter string into a Sort.Order object.
     *
     * @param sortOrder The string to parse (e.g., "email,desc").
     * @return A Sort.Order object, or null if the parameter is malformed.
     */
    private static Sort.Order parseSortOrder(String sortOrder) {
        try {
            String[] sortParts = sortOrder.split(",");
            String property = sortParts[0].trim();
            if (property.isEmpty()) {
                return null; // Ignore empty properties
            }
            Sort.Direction direction = sortParts.length > 1 ? Sort.Direction.fromString(sortParts[1].trim()) : Sort.Direction.ASC;
            return new Sort.Order(direction, property);
        } catch (Exception e) {
            log.warn("Ignoring invalid sort parameter: {}", sortOrder, e);
            return null;
        }
    }
}

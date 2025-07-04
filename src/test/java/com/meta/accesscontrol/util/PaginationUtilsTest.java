package com.meta.accesscontrol.util;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Sort;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PaginationUtilsTest {

    @Test
    void testNormalizeSortPairs_withEvenNumberOfParams_shouldMergeCorrectly() {
        String[] input = {"username", "asc", "createdAt", "desc"};
        String[] expected = {"username,asc", "createdAt,desc"};
        assertArrayEquals(expected, PaginationUtils.normalizeSortPairs(input));
    }

    @Test
    void testNormalizeSortPairs_withOddNumberOfParams_shouldHandleSingleCorrectly() {
        String[] input = {"username", "asc", "email"};
        String[] expected = {"username,asc", "email"};
        assertArrayEquals(expected, PaginationUtils.normalizeSortPairs(input));
    }

    @Test
    void testNormalizeSortPairs_withAlreadyMergedParams_shouldReturnAsIs() {
        String[] input = {"username,asc", "createdAt,desc"};
        String[] expected = {"username,asc", "createdAt,desc"};
        assertArrayEquals(expected, PaginationUtils.normalizeSortPairs(input));
    }

    @Test
    void testNormalizeSortPairs_withNullInput_shouldReturnEmptyArray() {
        String[] expected = {};
        assertArrayEquals(expected, PaginationUtils.normalizeSortPairs(null));
    }

    @Test
    void testNormalizeSortPairs_withEmptyInput_shouldReturnEmptyArray() {
        String[] input = {};
        String[] expected = {};
        assertArrayEquals(expected, PaginationUtils.normalizeSortPairs(input));
    }

    @Test
    void testBuildSortOrders_withValidPairs_shouldCreateOrders() {
        String[] input = {"username,asc", "createdAt,desc"};
        List<Sort.Order> orders = PaginationUtils.buildSortOrders(input);

        assertNotNull(orders);
        assertEquals(2, orders.size());
        assertEquals("username", orders.get(0).getProperty());
        assertEquals(Sort.Direction.ASC, orders.get(0).getDirection());
        assertEquals("createdAt", orders.get(1).getProperty());
        assertEquals(Sort.Direction.DESC, orders.get(1).getDirection());
    }

    @Test
    void testBuildSortOrders_withDefaultSort_shouldUseDefault() {
        String[] input = {"username"};
        List<Sort.Order> orders = PaginationUtils.buildSortOrders(input);

        assertEquals(1, orders.size());
        assertEquals("username", orders.getFirst().getProperty());
        assertEquals(Sort.Direction.ASC, orders.getFirst().getDirection());
    }

    @Test
    void testBuildSortOrders_withAdditionalSorts_shouldAppendThem() {
        String[] input = {"username,asc"};
        Sort.Order additionalOrder = Sort.Order.desc("id");
        List<Sort.Order> orders = PaginationUtils.buildSortOrders(input, additionalOrder);

        assertEquals(2, orders.size());
        assertEquals("username", orders.get(0).getProperty());
        assertEquals("id", orders.get(1).getProperty());
        assertEquals(Sort.Direction.DESC, orders.get(1).getDirection());
    }

    @Test
    void testBuildSortOrders_withInvalidFormat_shouldSkipInvalid() {
        String[] input = {"username,asc", "invalid-format", "email,desc"};
        List<Sort.Order> orders = PaginationUtils.buildSortOrders(input);

        assertEquals(2, orders.size());
        assertEquals("username", orders.get(0).getProperty());
        assertEquals("email", orders.get(1).getProperty());
    }
}
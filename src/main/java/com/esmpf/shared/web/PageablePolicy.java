package com.esmpf.shared.web;

import java.util.LinkedHashSet;
import java.util.Set;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

@Component
public class PageablePolicy {

    public static final int DEFAULT_PAGE_SIZE = 20;
    public static final int MAX_PAGE_SIZE = 100;

    public Pageable normalize(Pageable pageable, Sort defaultSort, String... allowedSortFields) {
        Pageable source = pageable == null
                ? PageRequest.of(0, DEFAULT_PAGE_SIZE, defaultSort)
                : pageable;

        int size = Math.min(source.getPageSize(), MAX_PAGE_SIZE);
        Sort requestedSort = source.getSort().isSorted() ? source.getSort() : defaultSort;
        Set<String> allowed = new LinkedHashSet<>(Set.of(allowedSortFields));

        for (Sort.Order order : requestedSort) {
            if (!allowed.contains(order.getProperty())) {
                throw new IllegalArgumentException(
                        "Unsupported sort field: " + order.getProperty() + "; allowed: " + allowed);
            }
        }

        return PageRequest.of(source.getPageNumber(), size, requestedSort);
    }
}

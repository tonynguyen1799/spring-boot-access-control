package com.meta.accesscontrol.repository.specs;

import com.meta.accesscontrol.controller.admin.payload.UserFilterRequest;
import com.meta.accesscontrol.model.User;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@RequiredArgsConstructor
public class UserSpecification implements Specification<User> {

    private final UserFilterRequest filter;

    @Override
    public Predicate toPredicate(Root<User> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
        List<Predicate> predicates = new ArrayList<>();

        if (StringUtils.hasText(filter.username())) {
            predicates.add(cb.like(cb.lower(root.get("username")), "%" + filter.username().toLowerCase() + "%"));
        }

        if (StringUtils.hasText(filter.email())) {
            predicates.add(cb.like(cb.lower(root.get("email")), "%" + filter.email().toLowerCase() + "%"));
        }

        if (Objects.nonNull(filter.roleTextIds()) && !filter.roleTextIds().isEmpty()) {
            predicates.add(root.join("roles").get("textId").in(filter.roleTextIds()));
        }

        if (Objects.nonNull(filter.enabled())) {
            predicates.add(cb.equal(root.get("enabled"), filter.enabled()));
        }

        return cb.and(predicates.toArray(new Predicate[0]));
    }
}
package com.weeklyreport.backend.service;

import com.weeklyreport.backend.domain.Report;
import com.weeklyreport.backend.dto.ReportListFilters;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;

/** Builds the dynamic WHERE clause for GET /reports from whichever filters were supplied. */
final class ReportSpecifications {

    private ReportSpecifications() {}

    static Specification<Report> matching(ReportListFilters filters) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filters.projectId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("project").get("id"), filters.projectId()));
            }
            if (filters.userId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("user").get("id"), filters.userId()));
            }
            if (filters.status() != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), filters.status()));
            }
            if (filters.weekStart() != null) {
                predicates.add(criteriaBuilder.equal(root.get("weekStart"), filters.weekStart()));
            }
            if (filters.weekEnd() != null) {
                predicates.add(criteriaBuilder.equal(root.get("weekEnd"), filters.weekEnd()));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}

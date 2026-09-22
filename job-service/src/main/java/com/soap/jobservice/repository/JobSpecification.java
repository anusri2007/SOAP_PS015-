package com.soap.jobservice.repository;

import com.soap.jobservice.entity.EmploymentType;
import com.soap.jobservice.entity.Job;
import com.soap.jobservice.entity.JobStatus;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class JobSpecification {

    public static Specification<Job> hasKeyword(String keyword) {
        return (root, query, cb) -> {
            if (keyword == null || keyword.trim().isEmpty()) {
                return cb.conjunction();
            }
            String pattern = "%" + keyword.trim().toLowerCase() + "%";
            Predicate titlePredicate = cb.like(cb.lower(root.get("title")), pattern);
            Predicate descPredicate = cb.like(cb.lower(root.get("description")), pattern);
            Predicate companyPredicate = cb.like(cb.lower(root.get("company")), pattern);
            Predicate skillsPredicate = cb.like(cb.lower(root.get("skills")), pattern);

            return cb.or(titlePredicate, descPredicate, companyPredicate, skillsPredicate);
        };
    }

    public static Specification<Job> filter(
            String location,
            EmploymentType employmentType,
            JobStatus status,
            Double minSalary,
            Double maxSalary,
            String company) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (location != null && !location.trim().isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("location")), "%" + location.trim().toLowerCase() + "%"));
            }

            if (employmentType != null) {
                predicates.add(cb.equal(root.get("employmentType"), employmentType));
            }

            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }

            if (minSalary != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("salary"), minSalary));
            }

            if (maxSalary != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("salary"), maxSalary));
            }

            if (company != null && !company.trim().isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("company")), "%" + company.trim().toLowerCase() + "%"));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}

package com.employeemanagement.repository;

import com.employeemanagement.model.Institution;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InstitutionRepository extends JpaRepository<Institution, Long> {
    boolean existsByName(String name);
}

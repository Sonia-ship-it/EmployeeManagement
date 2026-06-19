package com.employeemanagement.repository;

import com.employeemanagement.model.Employee;
import com.employeemanagement.model.EmployeeStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    Optional<Employee> findByEmail(String email);
    Optional<Employee> findByEmployeeCode(String employeeCode);
    Optional<Employee> findByUserId(Long userId);
    boolean existsByEmail(String email);
    boolean existsByEmployeeCode(String employeeCode);
    List<Employee> findByStatus(EmployeeStatus status);
}

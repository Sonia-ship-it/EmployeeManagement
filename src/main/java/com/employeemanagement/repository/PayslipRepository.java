package com.employeemanagement.repository;

import com.employeemanagement.model.Payslip;
import com.employeemanagement.model.PayslipStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PayslipRepository extends JpaRepository<Payslip, Long> {
    boolean existsByEmployeeIdAndMonthAndYear(Long employeeId, Integer month, Integer year);
    boolean existsByMonthAndYear(Integer month, Integer year);
    List<Payslip> findByMonthAndYear(Integer month, Integer year);
    List<Payslip> findByEmployeeId(Long employeeId);
    boolean existsByEmployeeId(Long employeeId);
    List<Payslip> findByMonthAndYearAndStatus(Integer month, Integer year, PayslipStatus status);
    Optional<Payslip> findByEmployeeIdAndMonthAndYear(Long employeeId, Integer month, Integer year);
}

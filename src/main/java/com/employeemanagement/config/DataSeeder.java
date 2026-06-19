package com.employeemanagement.config;

import com.employeemanagement.model.*;
import com.employeemanagement.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Seeds the database with demo data on first startup (only when tables are empty).
 *
 * <p>Default login accounts:
 * <ul>
 *   <li>admin / admin123 (ADMIN)</li>
 *   <li>manager / manager123 (MANAGER)</li>
 *   <li>employee / employee123 (EMPLOYEE — linked to Mugabo Javis EMP001)</li>
 * </ul>
 * </p>
 */
@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final EmployeeRepository employeeRepository;
    private final DeductionRepository deductionRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        seedUsers();
        seedDepartments();
        seedDeductions();
        seedEmployees();
    }

    private void seedUsers() {
        if (userRepository.count() > 0) {
            return;
        }

        userRepository.save(User.builder()
                .username("admin")
                .email("admin@rca.gov.rw")
                .password(passwordEncoder.encode("admin123"))
                .role(Role.ADMIN)
                .build());

        userRepository.save(User.builder()
                .username("manager")
                .email("manager@rca.gov.rw")
                .password(passwordEncoder.encode("manager123"))
                .role(Role.MANAGER)
                .build());

        userRepository.save(User.builder()
                .username("employee")
                .email("employee@rca.gov.rw")
                .password(passwordEncoder.encode("employee123"))
                .role(Role.EMPLOYEE)
                .build());
    }

    private void seedDepartments() {
        if (departmentRepository.count() > 0) {
            return;
        }

        departmentRepository.save(Department.builder()
                .name("Engineering")
                .description("Software and IT")
                .build());

        departmentRepository.save(Department.builder()
                .name("Human Resources")
                .description("HR and recruitment")
                .build());
    }

    /** Deduction percentages match the exam paper specification. */
    private void seedDeductions() {
        if (deductionRepository.count() > 0) {
            return;
        }

        deductionRepository.save(Deduction.builder().name("EmployeeTax").percentage(new BigDecimal("30")).build());
        deductionRepository.save(Deduction.builder().name("Pension").percentage(new BigDecimal("6")).build());
        deductionRepository.save(Deduction.builder().name("MedicalInsurance").percentage(new BigDecimal("5")).build());
        deductionRepository.save(Deduction.builder().name("Others").percentage(new BigDecimal("5")).build());
        deductionRepository.save(Deduction.builder().name("House").percentage(new BigDecimal("14")).build());
        deductionRepository.save(Deduction.builder().name("Transport").percentage(new BigDecimal("14")).build());
    }

    private void seedEmployees() {
        if (employeeRepository.count() > 0) {
            return;
        }

        Department engineering = departmentRepository.findAll().stream()
                .filter(d -> d.getName().equals("Engineering"))
                .findFirst()
                .orElseThrow();

        User employeeUser = userRepository.findByUsername("employee").orElseThrow();

        employeeRepository.save(Employee.builder()
                .firstName("Mugabo")
                .lastName("Javis")
                .email("mugabo.javis@rca.gov.rw")
                .mobile("0788123456")
                .district("Gasabo")
                .dateOfBirth(LocalDate.of(1995, 3, 15))
                .employeeCode("EMP001")
                .position("Software Developer")
                .baseSalary(new BigDecimal("70000"))
                .joiningDate(LocalDate.of(2022, 1, 10))
                .status(EmployeeStatus.ACTIVE)
                .institutionName("Rwanda Coding Academy")
                .department(engineering)
                .user(employeeUser)
                .build());

        employeeRepository.save(Employee.builder()
                .firstName("Michou")
                .lastName("Michell")
                .email("michou.michell@rca.gov.rw")
                .mobile("0789234567")
                .district("Musanze")
                .dateOfBirth(LocalDate.of(1998, 7, 22))
                .employeeCode("EMP002")
                .position("Junior Developer")
                .baseSalary(new BigDecimal("35000"))
                .joiningDate(LocalDate.of(2023, 6, 1))
                .status(EmployeeStatus.ACTIVE)
                .institutionName("Rwanda Coding Academy")
                .department(engineering)
                .build());
    }
}

package com.employeemanagement.service;

import com.employeemanagement.dto.EmployeeRequest;
import com.employeemanagement.dto.EmployeeResponse;
import com.employeemanagement.dto.EmployeeUpdateRequest;
import com.employeemanagement.model.Department;
import com.employeemanagement.model.Employee;
import com.employeemanagement.repository.EmployeeRepository;
import com.employeemanagement.validation.EmployeeBusinessValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
/**
 * Employee registration and updates.
 *
 * <p>Validation order: map DTO → entity → {@link com.employeemanagement.validation.EmployeeBusinessValidator}
 * → save. Employee ID (employeeCode) cannot be changed after creation.</p>
 */
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final DepartmentService departmentService;
    private final EmployeeBusinessValidator employeeValidator;

    @Transactional
    public EmployeeResponse register(EmployeeRequest request) {
        Employee employee = mapToEntity(request);
        employeeValidator.validateForCreate(employee);
        return toResponse(employeeRepository.save(employee));
    }

    public List<EmployeeResponse> findAll() {
        return employeeRepository.findAll().stream().map(this::toResponse).toList();
    }

    public EmployeeResponse findById(Long id) {
        return toResponse(getEmployee(id));
    }

    @Transactional
    public EmployeeResponse update(Long id, EmployeeUpdateRequest request) {
        Employee employee = getEmployee(id);
        employeeValidator.validateEmployeeCodeImmutable(employee.getEmployeeCode(), null);

        if (!employee.getEmail().equals(request.getEmail())
                && employeeRepository.existsByEmail(request.getEmail())) {
            throw new com.employeemanagement.exception.BusinessValidationException("Validation failed",
                    java.util.Map.of("email", "Email already exists"));
        }

        applyUpdate(employee, request);
        employeeValidator.validateForUpdate(employee, id);
        return toResponse(employeeRepository.save(employee));
    }

    @Transactional
    public void delete(Long id) {
        if (!employeeRepository.existsById(id)) {
            throw new IllegalArgumentException("Employee not found: " + id);
        }
        employeeValidator.validateForDelete(id);
        employeeRepository.deleteById(id);
    }

    public Employee getEmployee(Long id) {
        return employeeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found: " + id));
    }

    private Employee mapToEntity(EmployeeRequest request) {
        return Employee.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .mobile(request.getMobile())
                .district(request.getDistrict())
                .dateOfBirth(request.getDateOfBirth())
                .employeeCode(request.getEmployeeCode())
                .position(request.getPosition())
                .baseSalary(request.getBaseSalary())
                .joiningDate(request.getJoiningDate())
                .status(request.getStatus())
                .institutionName(request.getInstitutionName())
                .department(resolveDepartment(request.getDepartmentId()))
                .build();
    }

    private void applyUpdate(Employee employee, EmployeeUpdateRequest request) {
        employee.setFirstName(request.getFirstName());
        employee.setLastName(request.getLastName());
        employee.setEmail(request.getEmail());
        employee.setMobile(request.getMobile());
        employee.setDistrict(request.getDistrict());
        employee.setDateOfBirth(request.getDateOfBirth());
        employee.setPosition(request.getPosition());
        employee.setBaseSalary(request.getBaseSalary());
        employee.setJoiningDate(request.getJoiningDate());
        employee.setStatus(request.getStatus());
        employee.setInstitutionName(request.getInstitutionName());
        employee.setDepartment(resolveDepartment(request.getDepartmentId()));
    }

    private Department resolveDepartment(Long departmentId) {
        if (departmentId == null) {
            throw new com.employeemanagement.exception.BusinessValidationException("Validation failed",
                    java.util.Map.of("departmentId", "Department is required"));
        }
        return departmentService.getDepartment(departmentId);
    }

    private EmployeeResponse toResponse(Employee employee) {
        return EmployeeResponse.builder()
                .id(employee.getId())
                .firstName(employee.getFirstName())
                .lastName(employee.getLastName())
                .email(employee.getEmail())
                .mobile(employee.getMobile())
                .district(employee.getDistrict())
                .dateOfBirth(employee.getDateOfBirth())
                .employeeCode(employee.getEmployeeCode())
                .position(employee.getPosition())
                .baseSalary(employee.getBaseSalary())
                .joiningDate(employee.getJoiningDate())
                .status(employee.getStatus())
                .institutionName(employee.getInstitutionName())
                .departmentId(employee.getDepartment() != null ? employee.getDepartment().getId() : null)
                .departmentName(employee.getDepartment() != null ? employee.getDepartment().getName() : null)
                .build();
    }
}

package com.employeemanagement.service;

import com.employeemanagement.dto.DepartmentRequest;
import com.employeemanagement.dto.DepartmentResponse;
import com.employeemanagement.model.Department;
import com.employeemanagement.repository.DepartmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DepartmentService {

    private final DepartmentRepository departmentRepository;

    @Transactional
    public DepartmentResponse create(DepartmentRequest request) {
        if (departmentRepository.existsByName(request.getName())) {
            throw new IllegalArgumentException("Department already exists: " + request.getName());
        }

        Department department = Department.builder()
                .name(request.getName())
                .description(request.getDescription())
                .build();

        return toResponse(departmentRepository.save(department));
    }

    public List<DepartmentResponse> findAll() {
        return departmentRepository.findAll().stream().map(this::toResponse).toList();
    }

    public DepartmentResponse findById(Long id) {
        return toResponse(getDepartment(id));
    }

    @Transactional
    public DepartmentResponse update(Long id, DepartmentRequest request) {
        Department department = getDepartment(id);
        department.setName(request.getName());
        department.setDescription(request.getDescription());
        return toResponse(departmentRepository.save(department));
    }

    @Transactional
    public void delete(Long id) {
        if (!departmentRepository.existsById(id)) {
            throw new IllegalArgumentException("Department not found: " + id);
        }
        departmentRepository.deleteById(id);
    }

    Department getDepartment(Long id) {
        return departmentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Department not found: " + id));
    }

    private DepartmentResponse toResponse(Department department) {
        return DepartmentResponse.builder()
                .id(department.getId())
                .name(department.getName())
                .description(department.getDescription())
                .build();
    }
}

package com.employeemanagement.service;

import com.employeemanagement.dto.DeductionRequest;
import com.employeemanagement.dto.DeductionResponse;
import com.employeemanagement.exception.BusinessValidationException;
import com.employeemanagement.model.Deduction;
import com.employeemanagement.repository.DeductionRepository;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class DeductionService {

    private final DeductionRepository deductionRepository;
    private final Validator validator;

    @Transactional
    public DeductionResponse create(DeductionRequest request) {
        validateRequest(request);
        if (deductionRepository.existsByNameIgnoreCase(request.getName())) {
            throw new BusinessValidationException("Validation failed",
                    Map.of("name", "Deduction name must be unique"));
        }
        Deduction deduction = Deduction.builder()
                .name(request.getName())
                .percentage(request.getPercentage())
                .build();
        validateEntity(deduction);
        return toResponse(deductionRepository.save(deduction));
    }

    public List<DeductionResponse> findAll() {
        return deductionRepository.findAll().stream().map(this::toResponse).toList();
    }

    public DeductionResponse findById(Long id) {
        return toResponse(getDeduction(id));
    }

    @Transactional
    public DeductionResponse update(Long id, DeductionRequest request) {
        validateRequest(request);
        Deduction deduction = getDeduction(id);
        deduction.setName(request.getName());
        deduction.setPercentage(request.getPercentage());
        validateEntity(deduction);
        return toResponse(deductionRepository.save(deduction));
    }

    @Transactional
    public void delete(Long id) {
        if (!deductionRepository.existsById(id)) {
            throw new IllegalArgumentException("Deduction not found: " + id);
        }
        deductionRepository.deleteById(id);
    }

    private void validateRequest(DeductionRequest request) {
        if (request.getPercentage().compareTo(BigDecimal.ZERO) < 0
                || request.getPercentage().compareTo(BigDecimal.valueOf(100)) > 0) {
            throw new BusinessValidationException("Validation failed",
                    Map.of("percentage", "Percentage must be between 0 and 100"));
        }
    }

    private void validateEntity(Deduction deduction) {
        Set<ConstraintViolation<Deduction>> violations = validator.validate(deduction);
        if (!violations.isEmpty()) {
            Map<String, String> errors = new LinkedHashMap<>();
            violations.forEach(v -> errors.put(v.getPropertyPath().toString(), v.getMessage()));
            throw new BusinessValidationException("Validation failed", errors);
        }
    }

    private Deduction getDeduction(Long id) {
        return deductionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Deduction not found: " + id));
    }

    private DeductionResponse toResponse(Deduction deduction) {
        return DeductionResponse.builder()
                .id(deduction.getId())
                .name(deduction.getName())
                .percentage(deduction.getPercentage())
                .build();
    }
}

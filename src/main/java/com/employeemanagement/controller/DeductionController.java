package com.employeemanagement.controller;

import com.employeemanagement.dto.DeductionRequest;
import com.employeemanagement.dto.DeductionResponse;
import com.employeemanagement.service.DeductionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/deductions")
@RequiredArgsConstructor
public class DeductionController {

    private final DeductionService deductionService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DeductionResponse create(@Valid @RequestBody DeductionRequest request) {
        return deductionService.create(request);
    }

    @GetMapping
    public List<DeductionResponse> findAll() {
        return deductionService.findAll();
    }

    @GetMapping("/{id}")
    public DeductionResponse findById(@PathVariable Long id) {
        return deductionService.findById(id);
    }

    @PutMapping("/{id}")
    public DeductionResponse update(@PathVariable Long id, @Valid @RequestBody DeductionRequest request) {
        return deductionService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        deductionService.delete(id);
    }
}

package com.employeemanagement.controller;

import com.employeemanagement.dto.InstitutionRequest;
import com.employeemanagement.dto.InstitutionResponse;
import com.employeemanagement.service.InstitutionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/institutions")
@RequiredArgsConstructor
public class InstitutionController {

    private final InstitutionService institutionService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public InstitutionResponse create(@Valid @RequestBody InstitutionRequest request) {
        return institutionService.create(request);
    }

    @GetMapping
    public List<InstitutionResponse> findAll() {
        return institutionService.findAll();
    }

    @GetMapping("/{id}")
    public InstitutionResponse findById(@PathVariable Long id) {
        return institutionService.findById(id);
    }

    @PutMapping("/{id}")
    public InstitutionResponse update(@PathVariable Long id, @Valid @RequestBody InstitutionRequest request) {
        return institutionService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        institutionService.delete(id);
    }
}

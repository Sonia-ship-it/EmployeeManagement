package com.employeemanagement.service;

import com.employeemanagement.dto.InstitutionRequest;
import com.employeemanagement.dto.InstitutionResponse;
import com.employeemanagement.model.Institution;
import com.employeemanagement.repository.InstitutionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InstitutionService {

    private final InstitutionRepository institutionRepository;

    @Transactional
    public InstitutionResponse create(InstitutionRequest request) {
        if (institutionRepository.existsByName(request.getName())) {
            throw new IllegalArgumentException("Institution already exists: " + request.getName());
        }
        Institution institution = Institution.builder()
                .name(request.getName())
                .location(request.getLocation())
                .build();
        return toResponse(institutionRepository.save(institution));
    }

    public List<InstitutionResponse> findAll() {
        return institutionRepository.findAll().stream().map(this::toResponse).toList();
    }

    public InstitutionResponse findById(Long id) {
        return toResponse(getInstitution(id));
    }

    public Institution getInstitution(Long id) {
        return getInstitutionEntity(id);
    }

    @Transactional
    public InstitutionResponse update(Long id, InstitutionRequest request) {
        Institution institution = getInstitutionEntity(id);
        institution.setName(request.getName());
        institution.setLocation(request.getLocation());
        return toResponse(institutionRepository.save(institution));
    }

    @Transactional
    public void delete(Long id) {
        if (!institutionRepository.existsById(id)) {
            throw new IllegalArgumentException("Institution not found: " + id);
        }
        institutionRepository.deleteById(id);
    }

    private Institution getInstitutionEntity(Long id) {
        return institutionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Institution not found: " + id));
    }

    private InstitutionResponse toResponse(Institution institution) {
        return InstitutionResponse.builder()
                .id(institution.getId())
                .name(institution.getName())
                .location(institution.getLocation())
                .build();
    }
}

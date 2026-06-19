package com.employeemanagement.controller;

import com.employeemanagement.dto.MessageResponse;
import com.employeemanagement.model.User;
import com.employeemanagement.repository.EmployeeRepository;
import com.employeemanagement.service.PayrollService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
@Tag(name = "Messages", description = "Retrieve payroll notification messages")
@SecurityRequirement(name = "Bearer Authentication")
public class MessageController {

    private final PayrollService payrollService;
    private final EmployeeRepository employeeRepository;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Get all payroll messages")
    public List<MessageResponse> getAllMessages() {
        return payrollService.getAllMessages();
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('EMPLOYEE')")
    @Operation(summary = "Get messages for logged-in employee")
    public List<MessageResponse> getMyMessages(@AuthenticationPrincipal User user) {
        Long employeeId = employeeRepository.findByUserId(user.getId())
                .orElseThrow(() -> new IllegalArgumentException("No employee profile linked to this user"))
                .getId();
        return payrollService.getMessagesForEmployee(employeeId);
    }
}

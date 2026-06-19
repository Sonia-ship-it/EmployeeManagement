package com.employeemanagement.controller;

import com.employeemanagement.dto.PayrollDownloadResult;
import com.employeemanagement.dto.PayrollProcessResponse;
import com.employeemanagement.dto.PayrollRunRequest;
import com.employeemanagement.dto.PayslipResponse;
import com.employeemanagement.model.User;
import com.employeemanagement.repository.EmployeeRepository;
import com.employeemanagement.service.PayrollService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Payroll endpoints: process payroll, view payslips, download CSV/PDF reports.
 *
 * <p>Role requirements are enforced via {@code @PreAuthorize} on each method.</p>
 */
@RestController
@RequestMapping("/api/payroll")
@RequiredArgsConstructor
@Tag(name = "Payroll", description = "Process payroll and retrieve payslips")
@SecurityRequirement(name = "Bearer Authentication")
public class PayrollController {

    private final PayrollService payrollService;
    private final EmployeeRepository employeeRepository;

    @PostMapping("/process")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Process payroll for all active employees (POST trigger)")
    public PayrollProcessResponse processPayroll(@Valid @RequestBody PayrollRunRequest request) {
        return payrollService.processPayroll(request);
    }

    /**
     * Downloads all payslips for a month/year as CSV (default) or PDF.
     * Response is a file attachment — use Postman "Send and Download" or browser.
     */
    @GetMapping("/download")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Download generated payroll as CSV or PDF file")
    public ResponseEntity<byte[]> downloadPayroll(
            @RequestParam Integer month,
            @RequestParam Integer year,
            @RequestParam(defaultValue = "csv") String format) {
        PayrollDownloadResult file = payrollService.downloadPayroll(month, year, format);
        return buildFileResponse(file);
    }

    @GetMapping("/payslips/download/me")
    @PreAuthorize("hasRole('EMPLOYEE')")
    @Operation(summary = "Download own payslip as PDF")
    public ResponseEntity<byte[]> downloadMyPayslip(
            @AuthenticationPrincipal User user,
            @RequestParam Integer month,
            @RequestParam Integer year) {
        Long employeeId = employeeRepository.findByUserId(user.getId())
                .orElseThrow(() -> new IllegalArgumentException("No employee profile linked to this user"))
                .getId();
        PayrollDownloadResult file = payrollService.downloadEmployeePayslip(employeeId, month, year);
        return buildFileResponse(file);
    }

    @GetMapping("/payslips")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Get payslips by month and year")
    public List<PayslipResponse> getPayslipsByPeriod(
            @RequestParam Integer month,
            @RequestParam Integer year) {
        return payrollService.getPayslipsByPeriod(month, year);
    }

    @GetMapping("/payslips/all")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Get all payslips")
    public List<PayslipResponse> getAllPayslips() {
        return payrollService.getAllPayslips();
    }

    @GetMapping("/payslips/me")
    @PreAuthorize("hasRole('EMPLOYEE')")
    @Operation(summary = "Get payslips for logged-in employee")
    public List<PayslipResponse> getMyPayslips(@AuthenticationPrincipal User user) {
        Long employeeId = employeeRepository.findByUserId(user.getId())
                .orElseThrow(() -> new IllegalArgumentException("No employee profile linked to this user"))
                .getId();
        return payrollService.getPayslipsForEmployee(employeeId);
    }

    private ResponseEntity<byte[]> buildFileResponse(PayrollDownloadResult file) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getFilename() + "\"")
                .contentType(MediaType.parseMediaType(file.getContentType()))
                .body(file.getContent());
    }
}

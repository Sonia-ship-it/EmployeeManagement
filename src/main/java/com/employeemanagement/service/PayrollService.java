package com.employeemanagement.service;

import com.employeemanagement.dto.MessageResponse;
import com.employeemanagement.dto.PayrollDownloadResult;
import com.employeemanagement.dto.PayrollProcessResponse;
import com.employeemanagement.dto.PayrollRunRequest;
import com.employeemanagement.dto.PayslipResponse;
import com.employeemanagement.model.Employee;
import com.employeemanagement.model.EmployeeStatus;
import com.employeemanagement.model.Message;
import com.employeemanagement.model.Payslip;
import com.employeemanagement.model.PayslipStatus;
import com.employeemanagement.repository.EmployeeRepository;
import com.employeemanagement.repository.MessageRepository;
import com.employeemanagement.repository.PayslipRepository;
import com.employeemanagement.validation.PayrollBusinessValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
/**
 * Orchestrates the full payroll run for a given month/year.
 *
 * <p>Processing steps per active employee:
 * <ol>
 *   <li>Validate eligibility (ACTIVE, no duplicate payslip)</li>
 *   <li>Calculate payslip amounts</li>
 *   <li>Validate calculated values</li>
 *   <li>Save payslip with status PAID</li>
 *   <li>Generate and save notification message</li>
 * </ol>
 * </p>
 */
public class PayrollService {

    private final EmployeeRepository employeeRepository;
    private final PayslipRepository payslipRepository;
    private final MessageRepository messageRepository;
    private final PayrollCalculationService calculationService;
    private final MessageService messageService;
    private final PayrollExportService exportService;
    private final PayrollBusinessValidator payrollValidator;

    /**
     * Main payroll trigger — called by POST /api/payroll/process.
     * Only ADMIN and MANAGER roles may invoke this (see SecurityConfig).
     */
    @Transactional
    public PayrollProcessResponse processPayroll(PayrollRunRequest request) {
        payrollValidator.validatePayrollRequest(request.getMonth(), request.getYear());
        payrollValidator.validateDeductionRates();

        List<Employee> activeEmployees = employeeRepository.findByStatus(EmployeeStatus.ACTIVE);
        if (activeEmployees.isEmpty()) {
            throw new IllegalArgumentException("No active employees found for payroll processing");
        }

        List<PayslipResponse> payslips = new ArrayList<>();
        List<MessageResponse> messages = new ArrayList<>();

        for (Employee employee : activeEmployees) {
            payrollValidator.validateEmployeeEligible(employee, request.getMonth(), request.getYear());

            Payslip payslip = calculationService.calculatePayslip(employee, request.getMonth(), request.getYear());
            payrollValidator.validatePayslipBeforeSave(payslip, employee);

            payslip.setStatus(PayslipStatus.PAID);
            payslip = payslipRepository.save(payslip);
            employee.getPayslips().add(payslip);

            Message message = messageService.createMessage(employee, payslip);
            message = messageRepository.save(message);
            employee.getMessages().add(message);

            payslips.add(toPayslipResponse(payslip));
            messages.add(toMessageResponse(message));

            log.info("Payroll processed for {} - net: {}, status: {}",
                    employee.getEmployeeCode(), payslip.getNetSalary(), payslip.getStatus());
        }

        return PayrollProcessResponse.builder()
                .month(request.getMonth())
                .year(request.getYear())
                .employeesProcessed(activeEmployees.size())
                .payslips(payslips)
                .messages(messages)
                .csvDownloadUrl("/api/payroll/download?month=" + request.getMonth()
                        + "&year=" + request.getYear() + "&format=csv")
                .pdfDownloadUrl("/api/payroll/download?month=" + request.getMonth()
                        + "&year=" + request.getYear() + "&format=pdf")
                .build();
    }

    public PayrollDownloadResult downloadPayroll(Integer month, Integer year, String format) {
        List<PayslipResponse> payslips = getPayslipsByPeriod(month, year);
        if (payslips.isEmpty()) {
            throw new IllegalArgumentException("No payroll found for " + month + "/" + year);
        }
        if ("pdf".equalsIgnoreCase(format)) {
            return exportService.exportPdf(payslips, month, year);
        }
        return exportService.exportCsv(payslips, month, year);
    }

    public PayrollDownloadResult downloadEmployeePayslip(Long employeeId, Integer month, Integer year) {
        Payslip payslip = payslipRepository.findByEmployeeIdAndMonthAndYear(employeeId, month, year)
                .orElseThrow(() -> new IllegalArgumentException(
                        "No payslip found for employee in " + month + "/" + year));
        PayslipResponse response = toPayslipResponse(payslip);
        PayrollDownloadResult result = exportService.exportSinglePayslipPdf(response);
        String filename = String.format("payslip_%s_%02d_%d.pdf",
                response.getEmployeeCode(), month, year);
        return PayrollDownloadResult.builder()
                .content(result.getContent())
                .filename(filename)
                .contentType(result.getContentType())
                .build();
    }

    public List<PayslipResponse> getAllPayslips() {
        return payslipRepository.findAll().stream().map(this::toPayslipResponse).toList();
    }

    public List<PayslipResponse> getPayslipsByPeriod(Integer month, Integer year) {
        return payslipRepository.findByMonthAndYear(month, year)
                .stream().map(this::toPayslipResponse).toList();
    }

    public List<PayslipResponse> getPayslipsForEmployee(Long employeeId) {
        return payslipRepository.findByEmployeeId(employeeId)
                .stream().map(this::toPayslipResponse).toList();
    }

    public List<MessageResponse> getAllMessages() {
        return messageRepository.findAll().stream().map(this::toMessageResponse).toList();
    }

    public List<MessageResponse> getMessagesForEmployee(Long employeeId) {
        return messageRepository.findByEmployeeId(employeeId)
                .stream().map(this::toMessageResponse).toList();
    }

    private PayslipResponse toPayslipResponse(Payslip payslip) {
        Employee employee = payslip.getEmployee();
        return PayslipResponse.builder()
                .id(payslip.getId())
                .employeeId(employee.getId())
                .employeeName(employee.getFullName())
                .employeeCode(employee.getEmployeeCode())
                .baseSalary(payslip.getBaseSalary())
                .houseAllowance(payslip.getHouseAllowance())
                .transportAllowance(payslip.getTransportAllowance())
                .grossSalary(payslip.getGrossSalary())
                .totalDeductions(payslip.getTotalDeductions())
                .tax(payslip.getTax())
                .pension(payslip.getPension())
                .medical(payslip.getMedical())
                .others(payslip.getOthers())
                .netSalary(payslip.getNetSalary())
                .status(payslip.getStatus())
                .month(payslip.getMonth())
                .year(payslip.getYear())
                .build();
    }

    private MessageResponse toMessageResponse(Message message) {
        Employee employee = message.getEmployee();
        return MessageResponse.builder()
                .id(message.getId())
                .employeeId(employee.getId())
                .employeeName(employee.getFullName())
                .text(message.getText())
                .dateTime(message.getDateTime())
                .build();
    }
}

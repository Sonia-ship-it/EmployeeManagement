package com.employeemanagement.service;

import com.employeemanagement.model.Employee;
import com.employeemanagement.model.Message;
import com.employeemanagement.model.Payslip;
import org.springframework.stereotype.Service;

import java.time.Month;
import java.time.format.TextStyle;
import java.util.Locale;

/**
 * Builds the "Dear ..." salary notification message required by the exam paper.
 *
 * <p>Called during payroll processing — the message is then saved to the {@code messages} table.</p>
 */
@Service
public class MessageService {

    /**
     * Template: Dear &lt;FIRSTNAME&gt; Your salary of &lt;MONTH YEAR&gt; from &lt;INSTITUTION&gt;
     * &lt;AMOUNT&gt; has been credited to your &lt;EMPLOYEE_ID&gt; account Successfully
     */
    public String buildPayrollMessage(Employee employee, Payslip payslip) {
        String monthYear = Month.of(payslip.getMonth())
                .getDisplayName(TextStyle.FULL, Locale.ENGLISH) + " " + payslip.getYear();
        String institution = employee.getInstitutionName() != null
                ? employee.getInstitutionName()
                : "Government";

        return "Dear " + employee.getFirstName()
                + " Your salary of " + monthYear
                + " from " + institution
                + " " + payslip.getNetSalary()
                + " has been credited to your " + employee.getEmployeeCode()
                + " account Successfully";
    }

    /** Creates a Message entity ready to persist (dateTime defaults to now). */
    public Message createMessage(Employee employee, Payslip payslip) {
        return Message.builder()
                .employee(employee)
                .text(buildPayrollMessage(employee, payslip))
                .build();
    }
}

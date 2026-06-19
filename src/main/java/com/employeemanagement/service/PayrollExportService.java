package com.employeemanagement.service;

import com.employeemanagement.dto.PayrollDownloadResult;
import com.employeemanagement.dto.PayslipResponse;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Exports processed payroll to downloadable CSV or PDF files.
 *
 * <p>Used by GET /api/payroll/download and employee payslip download endpoints.</p>
 */
@Service
@Slf4j
public class PayrollExportService {

    public PayrollDownloadResult exportCsv(List<PayslipResponse> payslips, int month, int year) {
        StringBuilder csv = new StringBuilder();
        csv.append("Employee Code,Employee Name,Base Salary,House,Transport,Gross,Total Deductions,Tax,Pension,Medical,Others,Net Salary,Status,Month,Year\n");

        for (PayslipResponse p : payslips) {
            csv.append(escape(p.getEmployeeCode())).append(',')
                    .append(escape(p.getEmployeeName())).append(',')
                    .append(p.getBaseSalary()).append(',')
                    .append(p.getHouseAllowance()).append(',')
                    .append(p.getTransportAllowance()).append(',')
                    .append(p.getGrossSalary()).append(',')
                    .append(p.getTotalDeductions()).append(',')
                    .append(p.getTax()).append(',')
                    .append(p.getPension()).append(',')
                    .append(p.getMedical()).append(',')
                    .append(p.getOthers()).append(',')
                    .append(p.getNetSalary()).append(',')
                    .append(p.getStatus()).append(',')
                    .append(p.getMonth()).append(',')
                    .append(p.getYear()).append('\n');
        }

        String filename = String.format("payroll_%02d_%d.csv", month, year);
        return PayrollDownloadResult.builder()
                .content(csv.toString().getBytes(StandardCharsets.UTF_8))
                .filename(filename)
                .contentType("text/csv")
                .build();
    }

    public PayrollDownloadResult exportPdf(List<PayslipResponse> payslips, int month, int year) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4.rotate(), 36, 36, 54, 36);
            PdfWriter.getInstance(document, out);
            document.open();

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
            Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9);
            Font cellFont = FontFactory.getFont(FontFactory.HELVETICA, 8);

            document.add(new Paragraph("Payroll Report — " + formatMonthYear(month, year), titleFont));
            document.add(new Paragraph(" "));
            document.add(new Paragraph("Employees: " + payslips.size()));
            document.add(new Paragraph(" "));

            PdfPTable table = new PdfPTable(13);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{2f, 3f, 2f, 2f, 2f, 2f, 2.5f, 2f, 2f, 2f, 2f, 2f, 1.5f});

            String[] headers = {
                    "Emp Code", "Name", "Base", "House", "Transport", "Gross",
                    "Deductions", "Tax", "Pension", "Medical", "Others", "Net", "Status"
            };
            for (String header : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(header, headerFont));
                cell.setBackgroundColor(new java.awt.Color(220, 220, 220));
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(cell);
            }

            for (PayslipResponse p : payslips) {
                addCell(table, p.getEmployeeCode(), cellFont);
                addCell(table, p.getEmployeeName(), cellFont);
                addCell(table, p.getBaseSalary().toPlainString(), cellFont);
                addCell(table, p.getHouseAllowance().toPlainString(), cellFont);
                addCell(table, p.getTransportAllowance().toPlainString(), cellFont);
                addCell(table, p.getGrossSalary().toPlainString(), cellFont);
                addCell(table, p.getTotalDeductions().toPlainString(), cellFont);
                addCell(table, p.getTax().toPlainString(), cellFont);
                addCell(table, p.getPension().toPlainString(), cellFont);
                addCell(table, p.getMedical().toPlainString(), cellFont);
                addCell(table, p.getOthers().toPlainString(), cellFont);
                addCell(table, p.getNetSalary().toPlainString(), cellFont);
                addCell(table, p.getStatus().name(), cellFont);
            }

            document.add(table);
            document.close();

            String filename = String.format("payroll_%02d_%d.pdf", month, year);
            return PayrollDownloadResult.builder()
                    .content(out.toByteArray())
                    .filename(filename)
                    .contentType("application/pdf")
                    .build();
        } catch (DocumentException ex) {
            log.error("Failed to generate payroll PDF", ex);
            throw new IllegalStateException("Failed to generate payroll PDF: " + ex.getMessage());
        } catch (Exception ex) {
            log.error("Failed to generate payroll PDF", ex);
            throw new IllegalStateException("Failed to generate payroll PDF");
        }
    }

    public PayrollDownloadResult exportSinglePayslipPdf(PayslipResponse payslip) {
        return exportPdf(List.of(payslip), payslip.getMonth(), payslip.getYear());
    }

    private void addCell(PdfPTable table, String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text != null ? text : "", font));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(cell);
    }

    private String escape(String value) {
        if (value == null) {
            return "";
        }
        if (value.contains(",") || value.contains("\"")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    private String formatMonthYear(int month, int year) {
        String[] months = {
                "", "January", "February", "March", "April", "May", "June",
                "July", "August", "September", "October", "November", "December"
        };
        if (month >= 1 && month <= 12) {
            return months[month] + " " + year;
        }
        return month + "/" + year;
    }
}

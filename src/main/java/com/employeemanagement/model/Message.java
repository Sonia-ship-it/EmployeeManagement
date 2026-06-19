package com.employeemanagement.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Salary notification message sent to an employee after payroll is processed.
 *
 * <p>Text is auto-generated in {@link com.employeemanagement.service.MessageService}
 * using the format: "Dear &lt;name&gt; Your salary of &lt;month/year&gt; from &lt;institution&gt; ..."</p>
 */
@Entity
@Table(name = "messages")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Many messages can belong to one employee (ManyToOne). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    /** Full notification text shown to the employee. */
    @Column(nullable = false, length = 500)
    private String text;

    /** When the message was created (set automatically). */
    @Column(name = "date_time", nullable = false)
    @Builder.Default
    private LocalDateTime dateTime = LocalDateTime.now();
}

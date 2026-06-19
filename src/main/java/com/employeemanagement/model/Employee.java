package com.employeemanagement.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.Check;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Core employee record — stores personal and employment information in one table.
 *
 * <p>Database rules: unique email & employee_code, base_salary must be &gt; 0.
 * Employee code format: EMP001, EMP002 (immutable after creation).</p>
 *
 * <p>Relationships:
 * <ul>
 *   <li>{@link #payslips} — one employee has many payslips (OneToMany)</li>
 *   <li>{@link #messages} — one employee has many salary notification messages</li>
 *   <li>{@link #department} — each employee belongs to one department</li>
 * </ul>
 * </p>
 */
@Entity
@Table(name = "employees")
@Check(constraints = "base_salary > 0")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Employee {

    // --- Personal information (validated at API + business level) ---

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(min = 2, max = 50)
    @Pattern(regexp = "^[A-Za-z ]+$")
    @Column(name = "first_name", nullable = false, length = 50)
    private String firstName;

    @NotBlank
    @Size(min = 2, max = 50)
    @Pattern(regexp = "^[A-Za-z ]+$")
    @Column(name = "last_name", nullable = false, length = 50)
    private String lastName;

    @NotBlank
    @Email
    @Column(nullable = false, unique = true)
    private String email;

    @NotBlank
    @Pattern(regexp = "^07[2389][0-9]{7}$", message = "Mobile must be a valid Rwanda number")
    @Column(nullable = false, length = 10)
    private String mobile;

    @NotBlank
    @Column(nullable = false, length = 50)
    private String district;

    @NotNull
    @Past(message = "Date of birth must be in the past")
    @Column(name = "date_of_birth", nullable = false)
    private LocalDate dateOfBirth;

    // --- Employment information ---

    /** Government employee ID, e.g. EMP001. Cannot be updated after insert. */
    @NotBlank
    @Pattern(regexp = "^EMP[0-9]{3}$", message = "Employee ID must match format EMP001")
    @Column(name = "employee_code", nullable = false, unique = true, length = 6, updatable = false)
    private String employeeCode;

    @NotBlank
    @Column(nullable = false)
    private String position;

    @NotNull
    @DecimalMin(value = "1", message = "Base salary must be greater than zero")
    @Column(name = "base_salary", nullable = false)
    private BigDecimal baseSalary;

    @NotNull
    @PastOrPresent(message = "Joining date cannot be in the future")
    @Column(name = "joining_date", nullable = false)
    private LocalDate joiningDate;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private EmployeeStatus status = EmployeeStatus.ACTIVE;

    @Column(name = "institution_name")
    private String institutionName;

    @NotNull
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;

    // --- Relationships ---

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Payslip> payslips = new ArrayList<>();

    @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Message> messages = new ArrayList<>();

    public String getFullName() {
        return firstName + " " + lastName;
    }
}

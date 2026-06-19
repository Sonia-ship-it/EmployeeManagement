package com.employeemanagement.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.Check;

import java.math.BigDecimal;

@Entity
@Table(name = "deductions")
@Check(constraints = "percentage >= 0 AND percentage <= 100")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Deduction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false, unique = true)
    private String name;

    @NotNull
    @DecimalMin("0")
    @DecimalMax("100")
    @Column(nullable = false)
    private BigDecimal percentage;
}

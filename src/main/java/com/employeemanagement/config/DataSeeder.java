package com.employeemanagement.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.employeemanagement.model.*;
import com.employeemanagement.repository.*;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Seeds the database with demo data on first startup (only when tables are empty).
 *
 * <p>Default login accounts:
 * <ul>
 *   <li>admin / admin123 (ADMIN)</li>
 *   <li>manager / manager123 (MANAGER)</li>
 *   <li>employee / employee123 (EMPLOYEE — linked to Mugabo Javis EMP001)</li>
 * </ul>
 * </p>
 */
@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {
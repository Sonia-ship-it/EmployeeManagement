package com.employeemanagement.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;

/**
 * Installs PostgreSQL helper functions after the app starts and tables exist.
 *
 * <p>Scripts use {@code ;;} as statement separator because PL/pgSQL functions contain semicolons.</p>
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DatabaseRoutineInitializer {
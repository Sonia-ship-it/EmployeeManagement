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

    private static final String STATEMENT_SEPARATOR = ";;";

    private final DataSource dataSource;

    @EventListener(ApplicationReadyEvent.class)
    public void installRoutines() {
        try (Connection connection = dataSource.getConnection()) {
            ScriptUtils.executeSqlScript(
                    connection,
                    new EncodedResource(new ClassPathResource("db/routines.sql")),
                    false,
                    false,
                    ScriptUtils.DEFAULT_COMMENT_PREFIX,
                    STATEMENT_SEPARATOR,
                    ScriptUtils.DEFAULT_BLOCK_COMMENT_START_DELIMITER,
                    ScriptUtils.DEFAULT_BLOCK_COMMENT_END_DELIMITER
            );
            log.info("PostgreSQL payroll routines installed successfully");
        } catch (Exception ex) {
            log.error("Could not install DB routines", ex);
        }
    }
}

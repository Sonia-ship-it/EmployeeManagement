package com.employeemanagement.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

@Component
@Slf4j
public class ApplicationShutdownListener {

    private final DataSource dataSource;

    public ApplicationShutdownListener(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @EventListener(ContextClosedEvent.class)
    public void onShutdown() {
        if (dataSource instanceof AutoCloseable closeable) {
            try {
                closeable.close();
                log.info("DataSource closed successfully on application shutdown");
            } catch (Exception ex) {
                log.warn("Error closing DataSource on shutdown: {}", ex.getMessage());
            }
        }
    }
}

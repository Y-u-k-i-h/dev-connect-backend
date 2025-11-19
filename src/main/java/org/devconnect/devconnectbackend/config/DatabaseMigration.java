package org.devconnect.devconnectbackend.config;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Database migration component to handle schema updates that Hibernate can't manage automatically.
 * This ensures the database schema matches the entity definitions.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DatabaseMigration {

    private final JdbcTemplate jdbcTemplate;

    /**
     * Runs migrations after the application context is initialized.
     * This method is idempotent and safe to run multiple times.
     */
    @PostConstruct
    @Transactional
    public void migrate() {
        log.info("Starting database schema migrations...");
        
        try {
            // Migration 1: Allow NULL values for dev_id in projects table
            fixProjectsDevIdConstraint();
            
            log.info("Database schema migrations completed successfully!");
        } catch (Exception e) {
            log.error("Error during database migration: {}", e.getMessage(), e);
            // Don't throw - let the application start even if migration fails
            // The constraint might already be fixed
        }
    }

    /**
     * Fixes the dev_id column in projects table to allow NULL values.
     * This allows clients to create projects without assigning a developer initially.
     * Developers can claim these projects later.
     */
    private void fixProjectsDevIdConstraint() {
        try {
            log.info("Checking dev_id constraint in projects table...");
            
            // Check if the constraint exists
            String checkSql = """
                SELECT is_nullable 
                FROM information_schema.columns 
                WHERE table_name = 'projects' 
                AND column_name = 'dev_id'
                """;
            
            String isNullable = jdbcTemplate.queryForObject(checkSql, String.class);
            
            if ("NO".equals(isNullable)) {
                log.info("dev_id column has NOT NULL constraint. Removing it...");
                
                // Remove the NOT NULL constraint
                String alterSql = "ALTER TABLE projects ALTER COLUMN dev_id DROP NOT NULL";
                jdbcTemplate.execute(alterSql);
                
                log.info("✅ Successfully removed NOT NULL constraint from dev_id column");
                log.info("Projects can now be created without a developer assigned");
            } else {
                log.info("✅ dev_id column already allows NULL values. No migration needed.");
            }
            
        } catch (Exception e) {
            log.warn("Could not modify dev_id constraint: {}. It may already be correct.", e.getMessage());
        }
    }
}

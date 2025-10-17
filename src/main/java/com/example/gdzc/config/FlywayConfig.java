package com.example.gdzc.config;

import org.flywaydb.core.Flyway;
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FlywayConfig {
    @Bean
    public FlywayMigrationStrategy flywayMigrationStrategy() {
        return flyway -> {
            // Attempt repair first to clear any failed migration state
            try {
                flyway.repair();
            } catch (Exception ignored) {
            }
            // Proceed with migration
            flyway.migrate();
        };
    }
}
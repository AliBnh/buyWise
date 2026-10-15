package com.buywise;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * BuyWise — Purchase Intelligence and Ownership Management Platform.
 * 
 * Main entry point for the Spring Boot application.
 * Features enabled:
 * - {@link EnableJpaAuditing}: Automatic auditing for entity creation and modification timestamps.
 * - {@link EnableScheduling}: Powers idempotent background tasks such as deadline reminder checks.
 * - {@link EnableAsync}: Enables non-blocking document extraction and AI processing jobs.
 * - {@link ConfigurationPropertiesScan}: Type-safe binding of configuration properties.
 */
@SpringBootApplication
@EnableJpaAuditing
@EnableScheduling
@EnableAsync
@ConfigurationPropertiesScan
public class BuyWiseApplication {

    public static void main(String[] args) {
        SpringApplication.run(BuyWiseApplication.class, args);
    }
}

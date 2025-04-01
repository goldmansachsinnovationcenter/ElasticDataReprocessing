package com.goldmansachs.elasticdatareprocessing;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main application class for Elastic Data Reprocessing application.
 */
@SpringBootApplication
public final class ElasticDataReprocessingApplication {

    /**
     * Private constructor to prevent instantiation of utility class.
     */
    private ElasticDataReprocessingApplication() {
    }

    /**
     * Main method to start the Spring Boot application.
     *
     * @param args command line arguments
     */
    public static void main(final String[] args) {
        SpringApplication.run(ElasticDataReprocessingApplication.class, args);
    }
}

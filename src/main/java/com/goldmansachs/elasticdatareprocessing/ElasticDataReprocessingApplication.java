package com.goldmansachs.elasticdatareprocessing;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main application class for Elastic Data Reprocessing application.
 */
@SpringBootApplication
public class ElasticDataReprocessingApplication {

    /**
     * Constructor for Spring Boot to use.
     */
    public ElasticDataReprocessingApplication() {
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

package com.demo.devstandards;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main entry point for the Development Standards Demo application.
 * Connects to AWS DocumentDB and demonstrates development standards compliance.
 */
@SpringBootApplication
public class DevstandardsDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(DevstandardsDemoApplication.class, args);
    }
}

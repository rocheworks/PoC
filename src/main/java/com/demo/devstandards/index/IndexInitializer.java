package com.demo.devstandards.index;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.stereotype.Component;

/**
 * Ensures that all required indexes for the WebUser collection are created
 * programmatically at application startup.
 *
 * This approach satisfies the development standard:
 * "Are indexes added or updated by code or have they been added to the pipeline"
 *
 * Each index created here must also be documented in docs/INDEX_DOCUMENTATION.md
 */
@Component
@Order(1)
public class IndexInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(IndexInitializer.class);
    private static final String COLLECTION_NAME = "WebUser";

    private final MongoTemplate mongoTemplate;

    public IndexInitializer(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public void run(String... args) {
        logger.info("Ensuring indexes for collection: {}", COLLECTION_NAME);

        try {
            // Index on email (unique)
            mongoTemplate.indexOps(COLLECTION_NAME)
                    .ensureIndex(new Index().on("email", Sort.Direction.ASC).unique().named("idx_email_unique"));
            logger.info("Ensured index: idx_email_unique on field 'email'");

            // Index on last_name
            mongoTemplate.indexOps(COLLECTION_NAME)
                    .ensureIndex(new Index().on("last_name", Sort.Direction.ASC).named("idx_last_name"));
            logger.info("Ensured index: idx_last_name on field 'last_name'");

            logger.info("All indexes for '{}' collection ensured successfully.", COLLECTION_NAME);
        } catch (Exception e) {
            logger.error("Failed to create indexes for collection '{}': {}", COLLECTION_NAME, e.getMessage(), e);
            throw new RuntimeException("Index initialization failed", e);
        }
    }
}

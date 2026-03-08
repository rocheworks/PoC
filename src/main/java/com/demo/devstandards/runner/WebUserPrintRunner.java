package com.demo.devstandards.runner;

import com.demo.devstandards.model.WebUser;
import com.demo.devstandards.repository.WebUserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * CommandLineRunner that prints the first 3 rows of the WebUser collection
 * to the console on application startup.
 */
@Component
public class WebUserPrintRunner implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(WebUserPrintRunner.class);
    private static final int MAX_ROWS_TO_PRINT = 3;

    private final WebUserRepository webUserRepository;

    public WebUserPrintRunner(WebUserRepository webUserRepository) {
        this.webUserRepository = webUserRepository;
    }

    @Override
    public void run(String... args) {
        logger.info("Fetching first {} rows from WebUser collection...", MAX_ROWS_TO_PRINT);

        try {
            List<WebUser> users = webUserRepository.findAll(PageRequest.of(0, MAX_ROWS_TO_PRINT)).getContent();

            if (users.isEmpty()) {
                logger.info("No records found in WebUser collection.");
            } else {
                logger.info("=== First {} WebUser records ===", users.size());
                for (int i = 0; i < users.size(); i++) {
                    WebUser user = users.get(i);
                    System.out.println("Row " + (i + 1) + ": " + user);
                }
                logger.info("=== End of WebUser records ===");
            }
        } catch (Exception e) {
            logger.error("Error fetching WebUser records: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to fetch WebUser records", e);
        }
    }
}

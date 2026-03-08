package com.demo.devstandards;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
    "spring.data.mongodb.uri=mongodb://localhost:27017/test",
    "docdb.tls.caFile=/tmp/dummy-ca.pem",
    "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration,org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration"
})
class DevstandardsDemoApplicationTests {

    @Test
    void contextLoads() {
        // Verifies that the Spring context loads without errors
        // (MongoDB connection excluded for unit testing)
    }
}

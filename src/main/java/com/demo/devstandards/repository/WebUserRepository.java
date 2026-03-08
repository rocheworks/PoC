package com.demo.devstandards.repository;

import com.demo.devstandards.model.WebUser;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

/**
 * Data access layer for the WebUser collection.
 * All database access for WebUser must go through this repository,
 * following the data access layer pattern (development standard).
 */
@Repository
public interface WebUserRepository extends MongoRepository<WebUser, String> {
}

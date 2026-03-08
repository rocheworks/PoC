# GitHub Copilot Instruction Digest - Development Standards

This file provides GitHub Copilot with project-specific coding instructions derived from the
**Development Standards and Checklists** document. These instructions are automatically loaded
by GitHub Copilot when working in this repository and guide the developer to produce
standards-compliant code.

---

## General Coding Standards

1. **Code Clarity and Comments**
   - Write code that is easy to understand. Add meaningful comments for complex logic.
   - Avoid cryptic variable names or abbreviations.

2. **Intention-Revealing Names**
   - All classes, methods, variables, and constants must have names that clearly reveal their intent.
   - Example: use `fetchActiveUsersByEmail()` instead of `getData()`.

3. **Single Responsibility Principle**
   - Each class or method/function should do one thing only.
   - If a method is doing more than one thing, refactor it into smaller methods.

4. **Code Structure**
   - Code must be well-structured: logical grouping, clear separation of concerns, consistent formatting.

5. **No Dead Code**
   - Remove unused variables, methods, imports, and commented-out code blocks before committing.

6. **Library Usage and Vulnerabilities**
   - Use common, well-maintained libraries in their latest stable versions.
   - Check all libraries for known vulnerabilities before adding them.

7. **Input Validation**
   - Validate all input values. Never trust external input.
   - Check for null, empty strings, out-of-range values, and invalid formats.

8. **No Hardcoded Values**
   - Never hardcode configuration values, credentials, URLs, or magic numbers.
   - Use application properties, environment variables, or constants.

9. **Resilience Against Missing Data**
   - Code must be resilient against unexpected or missing data.
   - Use Optional, null checks, and default values appropriately.

10. **Resource Management**
    - Always release resources (Streams, Connections, ResultSets, etc.) in all code paths.
    - Use try-with-resources for AutoCloseable resources.

---

## Error Handling Standards

11. **Comprehensive Error Handling**
    - Handle all edge cases and potential error scenarios.
    - Every catch block must either throw an exception or return a meaningful error — never swallow exceptions silently.

12. **Error Documentation**
    - Document error scenarios so that testers can verify them.
    - Include expected error codes and messages in Javadoc or comments.

13. **Functional Error Codes**
    - All functional errors (e.g., "password does not match") must be mapped to an appropriate error code and message.
    - Never return HTTP 500 for functional/business errors.

14. **Technical Error Codes**
    - All technical errors must be mapped to appropriate error codes.
    - Technical errors must not be internally mapped to functional error codes.

15. **Error Logging**
    - All errors must be logged in reporting logs for observability.

---

## Database Standards (CRITICAL)

16. **Data Access Layer Pattern**
    - All database access MUST go through a dedicated data access layer (Repository pattern).
    - Never write raw database queries in controllers or service classes.

17. **INDEX REQUIREMENT — New or Modified Fields**
    - **IMPORTANT**: When adding a new field to a MongoDB/DocumentDB document model, or when
      modifying an existing field used in queries, you MUST:
      - Add an `@Indexed` annotation on the field in the model class
      - Add programmatic index creation in `IndexInitializer.java` using `mongoTemplate.indexOps().ensureIndex()`
      - Update the index documentation in `docs/INDEX_DOCUMENTATION.md`
      - Update the index registry in `scripts/index-registry.json`
    - Example for adding a new field `middle_name`:
      ```java
      @Field("middle_name")
      @Indexed
      private String middleName;
      ```
      Then in `IndexInitializer.java`:
      ```java
      mongoTemplate.indexOps(COLLECTION_NAME)
          .ensureIndex(new Index().on("middle_name", Sort.Direction.ASC).named("idx_middle_name"));
      ```
      Then in `docs/INDEX_DOCUMENTATION.md`, add a row to the index table.
      Then in `scripts/index-registry.json`, add the field entry.

18. **Indexes in Pipeline**
    - Indexes must be created either by code (IndexInitializer) or added to the deployment pipeline.
    - Both approaches must be kept in sync.

19. **Index Documentation**
    - Every index MUST be documented in `docs/INDEX_DOCUMENTATION.md` with:
      - Index name, field(s), type, uniqueness, purpose, and when it was added.

20. **Write Conflict Handling**
    - Handle write conflicts properly when concurrent calls for the same data arrive.
    - Use optimistic locking (`@Version`) or retry logic where appropriate.

21. **Database Synchronization Latency**
    - Handle database synchronization latency issues properly (eventual consistency in DocumentDB).

22. **Performance Testing**
    - Test locally for performance and resource consumption before raising a PR.

23. **Connection Pooling**
    - Evaluate impact on connection pooling for any database changes.

---

## Secret and Key Handling

24. **Expired Key Handling**
    - Design and implement graceful handling of expired keys for customers.

25. **No Secrets in Logs**
    - Never log secrets, keys, tokens, or passwords anywhere.

26. **Secrets from Keystore**
    - All secrets must be taken from a keystore (AWS Secrets Manager, environment variables, etc.).
    - Never hardcode secrets in source code.

---

## Logging Standards

27. **Reporting Logs — Inbound Calls**
    - Add reporting logs for all inbound API calls.

28. **Reporting Logs — Outbound Calls**
    - Add reporting logs for all outbound calls (to databases, external APIs, etc.).

29. **Reporting Log Completeness**
    - Reporting logs must contain all required data fields.

30. **Transaction ID Propagation**
    - Propagate `transaction_id` across all service calls for traceability.

31. **KPI Logs**
    - Define, document, and implement KPI logs as required.

---

## Analytics and Accessibility

32. **Analytics Tags**
    - Include analytics tags where required.

33. **Accessibility**
    - Check accessibility for all frontend components.

---

## Developer Testing Standards

34. **Unit Tests**
    - Write unit tests (both positive and negative scenarios) covering all backend code.

35. **Long-Running Calls**
    - Handle long-running or blocking calls properly (use async, timeouts, circuit breakers).

36. **Resource Consumption Testing**
    - Test locally for resource consumption and load.

37. **Test Stubs**
    - Provide stubs used during development to testers for load testing.

---

## Other Standards

38. **Initialization Order**
    - Ensure initialization or first-setup calls complete before any subsequent backend calls are triggered.

---

## Quick Reference: Adding a New Field to a Collection

When you add a new field to any document model class (e.g., `WebUser.java`):

1. Add `@Field("field_name")` and `@Indexed` annotations
2. Add getter/setter methods
3. Update `toString()` method
4. Add index creation in `IndexInitializer.java`
5. Document the index in `docs/INDEX_DOCUMENTATION.md`
6. Register the index in `scripts/index-registry.json`
7. The PR verification workflow will check all of the above automatically

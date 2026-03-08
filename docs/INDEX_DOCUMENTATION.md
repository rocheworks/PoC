# Index Documentation - WebUser Collection

This document tracks all database indexes for the `WebUser` collection in DocumentDB.
As per Development Standards Checklist: **"Are indexes documented"**

## Collection: WebUser

| Index Name          | Field(s)     | Type     | Unique | Purpose                                 | Added In   |
|---------------------|-------------|----------|--------|-----------------------------------------|------------|
| `_id_`              | `_id`       | Default  | Yes    | Primary key (auto-created by MongoDB)   | Initial    |
| `idx_email_unique`  | `email`     | Ascending| Yes    | Fast lookup by email, enforce uniqueness| Initial    |
| `idx_last_name`     | `last_name` | Ascending| No     | Fast search/sort by last name           | Initial    |

## How to Add a New Index

When adding a new field to the WebUser collection, follow these steps:

1. **Add `@Indexed` annotation** on the field in `WebUser.java` model class
2. **Add programmatic index creation** in `IndexInitializer.java` using `mongoTemplate.indexOps()`
3. **Update this documentation** with the new index details in the table above
4. **Update the GitHub Actions index registry** in `scripts/index-registry.json`
5. **Raise PR** - The CI pipeline will verify that new fields have corresponding indexes

## Index Creation Strategy

Indexes are created in two complementary ways:
- **Annotation-based**: `@Indexed` on model fields (Spring Data auto-index)
- **Programmatic**: `IndexInitializer.java` ensures indexes at startup (pipeline-friendly)

Both methods must be kept in sync. The GitHub Actions workflow validates this on every PR.

## Changelog

| Date       | Change Description                              | Author      |
|------------|------------------------------------------------|-------------|
| 2026-03-08 | Initial index setup: email (unique), last_name | Dev Team    |

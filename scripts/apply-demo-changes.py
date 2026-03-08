#!/usr/bin/env python3
"""
apply-demo-changes.py
Applies demo changes to the project files for the mock CI/CD demonstration.
Usage: python3 apply-demo-changes.py [pass|fail]
"""

import json
import os
import sys

SCENARIO = sys.argv[1] if len(sys.argv) > 1 else "fail"
PROJECT_ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))

MODEL_FILE = os.path.join(PROJECT_ROOT, "src/main/java/com/demo/devstandards/model/WebUser.java")
INDEX_INIT_FILE = os.path.join(PROJECT_ROOT, "src/main/java/com/demo/devstandards/index/IndexInitializer.java")
INDEX_DOC_FILE = os.path.join(PROJECT_ROOT, "docs/INDEX_DOCUMENTATION.md")
INDEX_REGISTRY = os.path.join(PROJECT_ROOT, "scripts/index-registry.json")


def add_field_to_model(with_index: bool):
    """Add middle_name field to WebUser.java, optionally with @Indexed."""
    with open(MODEL_FILE, "r") as f:
        lines = f.readlines()

    new_lines = []
    for line in lines:
        # Insert the new field before @Field("last_name")
        if '@Field("last_name")' in line:
            new_lines.append('    @Field("middle_name")\n')
            if with_index:
                new_lines.append('    @Indexed\n')
            new_lines.append('    private String middleName;\n')
            new_lines.append('\n')
        new_lines.append(line)

    with open(MODEL_FILE, "w") as f:
        f.writelines(new_lines)


def add_index_to_initializer():
    """Add middle_name index creation to IndexInitializer.java."""
    with open(INDEX_INIT_FILE, "r") as f:
        content = f.read()

    insert_after = 'logger.info("Ensured index: idx_last_name on field \'last_name\'");'
    new_block = """

            // Index on middle_name
            mongoTemplate.indexOps(COLLECTION_NAME)
                    .ensureIndex(new Index().on("middle_name", Sort.Direction.ASC).named("idx_middle_name"));
            logger.info("Ensured index: idx_middle_name on field 'middle_name'");"""

    content = content.replace(insert_after, insert_after + new_block)

    with open(INDEX_INIT_FILE, "w") as f:
        f.write(content)


def add_to_index_documentation():
    """Add middle_name entry to INDEX_DOCUMENTATION.md."""
    with open(INDEX_DOC_FILE, "r") as f:
        content = f.read()

    target = '| `idx_last_name`     | `last_name` | Ascending| No     | Fast search/sort by last name           | Initial    |'
    new_row = '| `idx_middle_name`   | `middle_name` | Ascending| No     | Fast search by middle name              | Demo       |'
    content = content.replace(target, target + '\n' + new_row)

    with open(INDEX_DOC_FILE, "w") as f:
        f.write(content)


def add_to_index_registry():
    """Add middle_name entry to index-registry.json."""
    with open(INDEX_REGISTRY, "r") as f:
        data = json.load(f)

    data["collections"]["WebUser"]["fields"]["middle_name"] = {
        "index_name": "idx_middle_name",
        "type": "ascending",
        "unique": False
    }

    with open(INDEX_REGISTRY, "w") as f:
        json.dump(data, f, indent=2)
        f.write("\n")


if __name__ == "__main__":
    if SCENARIO == "pass":
        add_field_to_model(with_index=True)
        add_index_to_initializer()
        add_to_index_documentation()
        add_to_index_registry()
        print("  Applied PASS scenario: field + @Indexed + IndexInitializer + docs + registry")
    elif SCENARIO == "fail":
        add_field_to_model(with_index=False)
        print("  Applied FAIL scenario: field WITHOUT @Indexed, no IndexInitializer, no docs, no registry")
    else:
        print(f"Unknown scenario: {SCENARIO}. Use 'pass' or 'fail'.")
        sys.exit(1)

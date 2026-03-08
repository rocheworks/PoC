#!/bin/bash
# =============================================================================
# verify-indexes.sh
# =============================================================================
# This script verifies that new or modified fields in MongoDB/DocumentDB model
# classes have corresponding indexes defined in:
#   1. The model class (@Indexed annotation)
#   2. The IndexInitializer (programmatic index creation)
#   3. The index documentation (docs/INDEX_DOCUMENTATION.md)
#   4. The index registry (scripts/index-registry.json)
#
# Used by the GitHub Actions PR verification workflow.
# =============================================================================

set -euo pipefail

ERRORS=()
WARNINGS=()
PASS_COUNT=0

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

log_pass() {
    echo -e "${GREEN}[PASS]${NC} $1"
    PASS_COUNT=$((PASS_COUNT + 1))
}

log_fail() {
    echo -e "${RED}[FAIL]${NC} $1"
    ERRORS+=("$1")
}

log_warn() {
    echo -e "${YELLOW}[WARN]${NC} $1"
    WARNINGS+=("$1")
}

echo "============================================="
echo " Development Standards - Index Verification"
echo "============================================="
echo ""

# --------------------------------------------------------------------------
# Step 1: Detect new @Field annotations in model files from the PR diff
# --------------------------------------------------------------------------
echo "--- Step 1: Detecting new fields in model classes ---"

# Get the diff of model files. If GITHUB_BASE_REF is set, use it; otherwise compare HEAD~1
if [ -n "${GITHUB_BASE_REF:-}" ]; then
    BASE_REF="origin/${GITHUB_BASE_REF}"
    # Fetch the base branch for comparison
    git fetch origin "${GITHUB_BASE_REF}" --depth=1 2>/dev/null || true
else
    BASE_REF="HEAD~1"
fi

# Find all model Java files
MODEL_DIR="src/main/java/com/demo/devstandards/model"
if [ ! -d "$MODEL_DIR" ]; then
    echo "No model directory found at $MODEL_DIR. Skipping."
    exit 0
fi

# Get list of new @Field annotations added in this PR/commit
NEW_FIELDS=()
CHANGED_MODEL_FILES=$(git diff --name-only "$BASE_REF" -- "$MODEL_DIR" 2>/dev/null || echo "")

if [ -z "$CHANGED_MODEL_FILES" ]; then
    echo "No model files were changed in this PR."
    echo ""
    echo "============================================="
    echo " Result: No index verification needed"
    echo "============================================="
    exit 0
fi

echo "Changed model files:"
for f in $CHANGED_MODEL_FILES; do
    echo "  - $f"
done
echo ""

# Extract newly added @Field lines
for model_file in $CHANGED_MODEL_FILES; do
    if [ -f "$model_file" ]; then
        # Get added lines (lines starting with +, excluding +++ header)
        ADDED_LINES=$(git diff "$BASE_REF" -- "$model_file" | grep '^+' | grep -v '^+++' || true)

        # Look for @Field("...") annotations in added lines
        while IFS= read -r line; do
            # Extract field name from @Field("field_name")
            field_name=$(echo "$line" | grep -oP '@Field\("([^"]+)"\)' | grep -oP '"[^"]+"' | tr -d '"' || true)
            if [ -n "$field_name" ]; then
                NEW_FIELDS+=("$field_name")
                echo "  Detected new field: '$field_name' in $model_file"
            fi
        done <<< "$ADDED_LINES"

        # Also look for new private fields that might represent new DB fields (without @Field annotation)
        while IFS= read -r line; do
            # Match lines like: private String middleName;
            if echo "$line" | grep -qP '^\+\s+private\s+\w+\s+\w+;'; then
                var_name=$(echo "$line" | grep -oP 'private\s+\w+\s+(\w+);' | sed 's/private\s\+\w\+\s\+//' | tr -d ';' || true)
                if [ -n "$var_name" ] && [ "$var_name" != "id" ]; then
                    # Check if there's a corresponding @Field annotation already tracked
                    has_field_annotation=false
                    for existing in "${NEW_FIELDS[@]:-}"; do
                        if [ "$existing" = "$var_name" ]; then
                            has_field_annotation=true
                            break
                        fi
                    done
                    if [ "$has_field_annotation" = false ]; then
                        # Convert camelCase to snake_case for checking
                        snake_name=$(echo "$var_name" | sed 's/\([A-Z]\)/_\L\1/g' | sed 's/^_//')
                        # Check if we already have this from @Field
                        already_tracked=false
                        for existing in "${NEW_FIELDS[@]:-}"; do
                            if [ "$existing" = "$snake_name" ]; then
                                already_tracked=true
                                break
                            fi
                        done
                        if [ "$already_tracked" = false ]; then
                            NEW_FIELDS+=("$snake_name")
                            echo "  Detected new field (from variable): '$snake_name' in $model_file"
                        fi
                    fi
                fi
            fi
        done <<< "$ADDED_LINES"
    fi
done

echo ""

if [ ${#NEW_FIELDS[@]} -eq 0 ]; then
    echo "No new fields detected in model changes."
    echo ""
    echo "============================================="
    echo " Result: No new fields to verify"
    echo "============================================="
    exit 0
fi

echo "New fields to verify: ${NEW_FIELDS[*]}"
echo ""

# --------------------------------------------------------------------------
# Step 2: Verify @Indexed annotation exists for each new field
# --------------------------------------------------------------------------
echo "--- Step 2: Verifying @Indexed annotations ---"

for field_name in "${NEW_FIELDS[@]}"; do
    # Search in all model files for @Indexed near the field
    found_indexed=false
    for model_file in $CHANGED_MODEL_FILES; do
        if [ -f "$model_file" ]; then
            # Check if the field has @Indexed annotation before @Field("field_name")
            if grep -B3 "@Field(\"$field_name\")" "$model_file" | grep -q "@Indexed"; then
                found_indexed=true
                break
            fi
            # Check if @Indexed appears after @Field("field_name") (within 2 lines)
            if grep -A2 "@Field(\"$field_name\")" "$model_file" | grep -q "@Indexed"; then
                found_indexed=true
                break
            fi
            # Also check if @Indexed is on the same field declaration
            if grep -A2 "@Indexed" "$model_file" | grep -q "$field_name"; then
                found_indexed=true
                break
            fi
        fi
    done

    if [ "$found_indexed" = true ]; then
        log_pass "Field '$field_name' has @Indexed annotation in model class"
    else
        log_fail "Field '$field_name' is MISSING @Indexed annotation in model class. Add @Indexed to the field."
    fi
done
echo ""

# --------------------------------------------------------------------------
# Step 3: Verify IndexInitializer has the index creation
# --------------------------------------------------------------------------
echo "--- Step 3: Verifying programmatic index creation (IndexInitializer) ---"

INDEX_INIT_FILE="src/main/java/com/demo/devstandards/index/IndexInitializer.java"

if [ ! -f "$INDEX_INIT_FILE" ]; then
    log_fail "IndexInitializer.java not found at $INDEX_INIT_FILE"
else
    for field_name in "${NEW_FIELDS[@]}"; do
        if grep -q "\"$field_name\"" "$INDEX_INIT_FILE"; then
            log_pass "Field '$field_name' has programmatic index creation in IndexInitializer.java"
        else
            log_fail "Field '$field_name' is MISSING programmatic index creation in IndexInitializer.java. Add ensureIndex() for '$field_name'."
        fi
    done
fi
echo ""

# --------------------------------------------------------------------------
# Step 4: Verify index documentation
# --------------------------------------------------------------------------
echo "--- Step 4: Verifying index documentation ---"

INDEX_DOC_FILE="docs/INDEX_DOCUMENTATION.md"

if [ ! -f "$INDEX_DOC_FILE" ]; then
    log_fail "Index documentation not found at $INDEX_DOC_FILE"
else
    for field_name in "${NEW_FIELDS[@]}"; do
        if grep -q "$field_name" "$INDEX_DOC_FILE"; then
            log_pass "Field '$field_name' is documented in $INDEX_DOC_FILE"
        else
            log_fail "Field '$field_name' is MISSING from index documentation ($INDEX_DOC_FILE). Document the new index."
        fi
    done
fi
echo ""

# --------------------------------------------------------------------------
# Step 5: Verify index registry
# --------------------------------------------------------------------------
echo "--- Step 5: Verifying index registry ---"

INDEX_REGISTRY="scripts/index-registry.json"

if [ ! -f "$INDEX_REGISTRY" ]; then
    log_fail "Index registry not found at $INDEX_REGISTRY"
else
    for field_name in "${NEW_FIELDS[@]}"; do
        if grep -q "\"$field_name\"" "$INDEX_REGISTRY"; then
            log_pass "Field '$field_name' is registered in $INDEX_REGISTRY"
        else
            log_fail "Field '$field_name' is MISSING from index registry ($INDEX_REGISTRY). Add the field to the registry."
        fi
    done
fi
echo ""

# --------------------------------------------------------------------------
# Summary
# --------------------------------------------------------------------------
echo "============================================="
echo " INDEX VERIFICATION SUMMARY"
echo "============================================="
echo -e " ${GREEN}Passed:${NC}  $PASS_COUNT"
echo -e " ${RED}Failed:${NC}  ${#ERRORS[@]}"
echo -e " ${YELLOW}Warnings:${NC} ${#WARNINGS[@]}"
echo ""

if [ ${#ERRORS[@]} -gt 0 ]; then
    echo -e "${RED}ERRORS:${NC}"
    for err in "${ERRORS[@]}"; do
        echo "  - $err"
    done
    echo ""
    echo -e "${RED}============================================="
    echo " RESULT: FAILED - PR cannot be merged"
    echo "=============================================${NC}"
    echo ""
    echo "Please fix the above issues before merging."
    echo "Refer to docs/INDEX_DOCUMENTATION.md for guidance."
    exit 1
fi

echo -e "${GREEN}============================================="
echo " RESULT: PASSED - All index checks passed"
echo "=============================================${NC}"
exit 0

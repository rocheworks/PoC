#!/bin/bash
# =============================================================================
# demo-simulate-pr.sh
# =============================================================================
# This script simulates the PR verification process locally for demonstration.
# It creates a feature branch, makes changes (adding middle_name field),
# and runs the verification script to show how the CI/CD pipeline would work.
#
# Usage:
#   ./scripts/demo-simulate-pr.sh [pass|fail]
#   - pass: Demonstrates a compliant PR (all checks pass)
#   - fail: Demonstrates a non-compliant PR (missing index, checks fail)
# =============================================================================

set -euo pipefail

SCENARIO="${1:-fail}"
PROJECT_ROOT="$(cd "$(dirname "$0")/.." && pwd)"

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
CYAN='\033[0;36m'
NC='\033[0m'

echo -e "${CYAN}============================================="
echo " MOCK CI/CD PIPELINE DEMONSTRATION"
echo "=============================================${NC}"
echo ""
echo -e "Scenario: ${YELLOW}${SCENARIO}${NC}"
echo ""

cd "$PROJECT_ROOT"

# Step 1: Create feature branch
echo -e "${CYAN}[Pipeline Step 1] Creating feature branch...${NC}"
BRANCH_NAME="feature/add-middle-name-field"
git checkout -b "$BRANCH_NAME" 2>/dev/null || git checkout "$BRANCH_NAME" 2>/dev/null || true
echo "  Branch: $BRANCH_NAME"
echo ""

# Step 2: Make changes based on scenario using Python for reliable file modification
echo -e "${CYAN}[Pipeline Step 2] Developer makes changes...${NC}"

python3 "$PROJECT_ROOT/scripts/apply-demo-changes.py" "$SCENARIO"

# Step 3: Stage and commit
echo -e "${CYAN}[Pipeline Step 3] Committing changes...${NC}"
git add -A
git commit -m "feat: add middle_name field to WebUser" --allow-empty 2>/dev/null || true
echo "  Changes committed."
echo ""

# Step 4: Run verification (simulating CI/CD)
echo -e "${CYAN}[Pipeline Step 4] Running PR Verification (simulating GitHub Actions)...${NC}"
echo ""

chmod +x scripts/verify-indexes.sh

set +e
bash scripts/verify-indexes.sh
RESULT=$?
set -e

echo ""

# Step 5: Show result
if [ $RESULT -eq 0 ]; then
    echo -e "${GREEN}[Pipeline Step 5] PR APPROVED - Merge is allowed${NC}"
else
    echo -e "${RED}[Pipeline Step 5] PR BLOCKED - Developer must fix issues before merge${NC}"
    echo ""
    echo -e "${YELLOW}In GitHub Actions, the developer would see a PR comment with these errors"
    echo -e "and specific instructions on how to fix them.${NC}"
fi

echo ""
echo -e "${CYAN}============================================="
echo " END OF MOCK CI/CD DEMONSTRATION"
echo "=============================================${NC}"

# Cleanup - go back to original branch and delete feature branch
git checkout - 2>/dev/null || true
git branch -D "$BRANCH_NAME" 2>/dev/null || true

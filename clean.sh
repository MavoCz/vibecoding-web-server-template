#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"

GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

log()  { echo -e "${GREEN}[clean]${NC} $1"; }
warn() { echo -e "${YELLOW}[clean]${NC} $1"; }

cd "$SCRIPT_DIR"

# --- Backend ---
log "Cleaning Gradle build..."
cd "$SCRIPT_DIR/backend"
./gradlew clean -q 2>/dev/null || warn "Gradle clean failed (is Java installed?)"
cd "$SCRIPT_DIR"

# --- Node modules ---
log "Removing node_modules..."
rm -rf node_modules common/node_modules web/node_modules e2e/node_modules

# --- pnpm store (project-local) ---
if [ -d ".pnpm-store" ]; then
  log "Removing local pnpm store..."
  rm -rf .pnpm-store
fi

# --- Frontend build output ---
log "Removing frontend build artifacts..."
rm -rf web/dist
rm -rf backend/src/main/resources/static

# --- Playwright artifacts ---
log "Removing Playwright reports and results..."
rm -rf e2e/playwright-report e2e/test-results

# --- Docker Compose (optional) ---
if [ "${1:-}" = "--docker" ]; then
  log "Stopping Docker Compose and removing volumes..."
  docker compose -f backend/compose.yml down -v 2>/dev/null || true
fi

log "Done. Run 'pnpm install' to reinstall dependencies."

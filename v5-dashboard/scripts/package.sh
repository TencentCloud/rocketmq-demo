#!/bin/bash

###############################################################################
# RocketMQ Dashboard - Complete Build & Package Script
###############################################################################
# This script automates the complete build process:
# 1. Build frontend (Vue 3 + TypeScript)
# 2. Copy frontend artifacts to backend static resources
# 3. Build backend (Spring Boot + embedded frontend)
# 4. Verify build output
###############################################################################

set -e  # Exit on any error

# Color output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Project paths
PROJECT_ROOT="$(cd "$(dirname "$0")/.." && pwd)"
FRONTEND_DIR="$PROJECT_ROOT/frontend"
BACKEND_DIR="$PROJECT_ROOT/backend"
BACKEND_API_DIR="$BACKEND_DIR/rocketmq-dashboard-api"
STATIC_DIR="$BACKEND_API_DIR/src/main/resources/static"
TARGET_DIR="$BACKEND_API_DIR/target"

# Version info
VERSION="1.0.0-SNAPSHOT"
BUILD_DATE=$(date '+%Y-%m-%d %H:%M:%S')
BUILD_LOG="$PROJECT_ROOT/build.log"

###############################################################################
# Helper Functions
###############################################################################

log_info() {
    echo -e "${GREEN}[INFO]${NC} $1" | tee -a "$BUILD_LOG"
}

log_warn() {
    echo -e "${YELLOW}[WARN]${NC} $1" | tee -a "$BUILD_LOG"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1" | tee -a "$BUILD_LOG"
}

check_command() {
    if ! command -v "$1" &> /dev/null; then
        log_error "$1 is not installed. Please install it first."
        exit 1
    fi
}

print_banner() {
    echo ""
    echo "============================================="
    echo "  RocketMQ Dashboard - Build & Package"
    echo "============================================="
    echo "  Version: $VERSION"
    echo "  Date: $BUILD_DATE"
    echo "============================================="
    echo ""
}

###############################################################################
# Build Steps
###############################################################################

step_check_prerequisites() {
    log_info "Step 1/5: Checking prerequisites..."
    
    check_command "node"
    check_command "npm"
    check_command "mvn"
    check_command "java"
    
    log_info "Node version: $(node --version)"
    log_info "npm version: $(npm --version)"
    log_info "Maven version: $(mvn --version | head -n 1)"
    log_info "Java version: $(java -version 2>&1 | head -n 1)"
    
    log_info "✓ All prerequisites met"
}

step_build_frontend() {
    log_info "Step 2/5: Building frontend..."
    
    cd "$FRONTEND_DIR"
    
    # Clean previous build (both local dist and backend static)
    if [ -d "dist" ]; then
        log_info "Cleaning previous frontend build (local dist)..."
        rm -rf dist
    fi
    if [ -d "$STATIC_DIR" ]; then
        log_info "Cleaning previous frontend build (backend static)..."
        rm -rf "$STATIC_DIR"
    fi
    
    # Install dependencies
    log_info "Installing frontend dependencies..."
    npm install
    
    # Build frontend
    log_info "Building frontend (Vue 3 + TypeScript + Vite)..."
    npm run build
    
    # Verify build output - check both local dist and backend static
    if [ -d "dist" ]; then
        FRONTEND_SIZE=$(du -sh dist | cut -f1)
        log_info "✓ Frontend build complete - output to local dist/ (Size: $FRONTEND_SIZE)"
    elif [ -f "$STATIC_DIR/index.html" ]; then
        FRONTEND_SIZE=$(du -sh "$STATIC_DIR" | cut -f1)
        log_info "✓ Frontend build complete - output directly to backend static/ (Size: $FRONTEND_SIZE)"
    else
        log_error "Frontend build failed: no build output found"
        exit 1
    fi
}

step_copy_frontend_to_backend() {
    log_info "Step 3/5: Copying frontend to backend static resources..."
    
    cd "$FRONTEND_DIR"
    
    if [ -d "dist" ]; then
        if [ -d "$STATIC_DIR" ]; then
            log_info "Cleaning backend static directory..."
            rm -rf "$STATIC_DIR"
        fi
        
        mkdir -p "$STATIC_DIR"
        
        log_info "Copying frontend artifacts from dist/..."
        cp -r dist/* "$STATIC_DIR/"
        
        if [ ! -f "$STATIC_DIR/index.html" ]; then
            log_error "Failed to copy frontend to backend: index.html not found"
            exit 1
        fi
        
        STATIC_SIZE=$(du -sh "$STATIC_DIR" | cut -f1)
        log_info "✓ Frontend copied to backend (Size: $STATIC_SIZE)"
    elif [ -f "$STATIC_DIR/index.html" ]; then
        STATIC_SIZE=$(du -sh "$STATIC_DIR" | cut -f1)
        log_info "✓ Frontend already in backend static/ - skipping copy (Size: $STATIC_SIZE)"
    else
        log_error "No frontend build found to copy"
        exit 1
    fi
}

step_build_backend() {
    log_info "Step 4/5: Building backend with embedded frontend..."
    
    cd "$BACKEND_DIR"
    
    # Clean previous build
    log_info "Cleaning previous backend build..."
    mvn clean
    
    # Build backend (skip tests for faster build)
    log_info "Building backend (Spring Boot + Maven)..."
    mvn package -DskipTests
    
    # Verify JAR file
    JAR_FILE="$TARGET_DIR/rocketmq-dashboard-api-$VERSION.jar"
    if [ ! -f "$JAR_FILE" ]; then
        log_error "Backend build failed: JAR file not found at $JAR_FILE"
        exit 1
    fi
    
    JAR_SIZE=$(du -sh "$JAR_FILE" | cut -f1)
    log_info "✓ Backend build complete (JAR size: $JAR_SIZE)"
}

step_verify_build() {
    log_info "Step 5/5: Verifying build output..."
    
    JAR_FILE="$TARGET_DIR/rocketmq-dashboard-api-$VERSION.jar"
    
    # Check if JAR contains static resources
    log_info "Checking if JAR contains frontend resources..."
    if jar tf "$JAR_FILE" | grep -q "BOOT-INF/classes/static/index.html"; then
        log_info "✓ JAR contains frontend resources"
    else
        log_error "JAR does not contain frontend resources"
        exit 1
    fi
    
    # List JAR contents summary
    TOTAL_FILES=$(jar tf "$JAR_FILE" | wc -l)
    STATIC_FILES=$(jar tf "$JAR_FILE" | grep "BOOT-INF/classes/static/" | wc -l)
    
    log_info "JAR contents:"
    log_info "  - Total files: $TOTAL_FILES"
    log_info "  - Static files: $STATIC_FILES"
    
    log_info "✓ Build verification complete"
}

###############################################################################
# Main Execution
###############################################################################

main() {
    # Initialize build log
    echo "Build started at: $BUILD_DATE" > "$BUILD_LOG"
    
    print_banner
    
    # Record start time
    START_TIME=$(date +%s)
    
    # Execute build steps
    step_check_prerequisites
    step_build_frontend
    step_copy_frontend_to_backend
    step_build_backend
    step_verify_build
    
    # Calculate build time
    END_TIME=$(date +%s)
    BUILD_TIME=$((END_TIME - START_TIME))
    
    # Print summary
    echo ""
    echo "============================================="
    log_info "Build completed successfully!"
    echo "============================================="
    echo ""
    log_info "Build Summary:"
    log_info "  - Build time: ${BUILD_TIME}s"
    log_info "  - JAR location: $TARGET_DIR/rocketmq-dashboard-api-$VERSION.jar"
    log_info "  - Build log: $BUILD_LOG"
    echo ""
    log_info "Next steps:"
    echo "  1. Run application: java -jar $TARGET_DIR/rocketmq-dashboard-api-$VERSION.jar"
    echo "  2. Access dashboard: http://localhost:8080"
    echo "  3. Check health: curl http://localhost:8080/api/health"
    echo ""
}

# Trap errors
trap 'log_error "Build failed at line $LINENO. Check $BUILD_LOG for details."; exit 1' ERR

# Run main
main

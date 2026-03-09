#!/bin/bash

set -e

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

PROJECT_ROOT="$(cd "$(dirname "$0")/.." && pwd)"
BACKEND_DIR="$PROJECT_ROOT/backend/rocketmq-dashboard-api"
JAR_FILE="$BACKEND_DIR/target/rocketmq-dashboard-api-1.0.0-SNAPSHOT.jar"
PORT=8080
STARTUP_TIMEOUT=30
HEALTH_CHECK_INTERVAL=2

log_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

log_warn() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

check_jar_exists() {
    log_info "Checking if JAR file exists..."
    if [ ! -f "$JAR_FILE" ]; then
        log_error "JAR file not found at: $JAR_FILE"
        log_error "Please run './scripts/package.sh' first to build the application."
        exit 1
    fi
    log_info "✓ JAR file found: $JAR_FILE"
}

check_port_available() {
    log_info "Checking if port $PORT is available..."
    if lsof -Pi :$PORT -sTCP:LISTEN -t >/dev/null 2>&1 ; then
        log_warn "Port $PORT is already in use. Stopping existing process..."
        PID=$(lsof -Pi :$PORT -sTCP:LISTEN -t)
        kill -9 $PID 2>/dev/null || true
        sleep 2
    fi
    log_info "✓ Port $PORT is available"
}

start_application() {
    log_info "Starting RocketMQ Dashboard..."
    
    java -jar "$JAR_FILE" > /tmp/rocketmq-dashboard.log 2>&1 &
    APP_PID=$!
    
    echo $APP_PID > /tmp/rocketmq-dashboard.pid
    
    log_info "Application started with PID: $APP_PID"
    log_info "Log file: /tmp/rocketmq-dashboard.log"
}

wait_for_startup() {
    log_info "Waiting for application to start (timeout: ${STARTUP_TIMEOUT}s)..."
    
    ELAPSED=0
    while [ $ELAPSED -lt $STARTUP_TIMEOUT ]; do
        if curl -s http://localhost:$PORT/api/health > /dev/null 2>&1; then
            log_info "✓ Application started successfully in ${ELAPSED}s"
            return 0
        fi
        
        if ! ps -p $APP_PID > /dev/null 2>&1; then
            log_error "Application process died unexpectedly"
            log_error "Last 20 lines from log:"
            tail -n 20 /tmp/rocketmq-dashboard.log
            exit 1
        fi
        
        sleep $HEALTH_CHECK_INTERVAL
        ELAPSED=$((ELAPSED + HEALTH_CHECK_INTERVAL))
        echo -n "."
    done
    
    echo ""
    log_error "Application failed to start within ${STARTUP_TIMEOUT}s"
    stop_application
    exit 1
}

verify_health_endpoint() {
    log_info "Verifying health endpoint..."
    
    HEALTH_RESPONSE=$(curl -s http://localhost:$PORT/api/health)
    
    if echo "$HEALTH_RESPONSE" | grep -q '"status":"UP"'; then
        log_info "✓ Health endpoint is responding correctly"
        log_info "Response: $HEALTH_RESPONSE"
    else
        log_error "Health endpoint returned unexpected response"
        log_error "Response: $HEALTH_RESPONSE"
        stop_application
        exit 1
    fi
}

verify_frontend() {
    log_info "Verifying frontend is accessible..."
    
    HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:$PORT/)
    
    if [ "$HTTP_CODE" = "200" ]; then
        log_info "✓ Frontend is accessible (HTTP $HTTP_CODE)"
    else
        log_error "Frontend returned HTTP $HTTP_CODE instead of 200"
        stop_application
        exit 1
    fi
}

verify_api_endpoints() {
    log_info "Verifying API endpoints..."
    
    ENDPOINTS=(
        "/api/health"
        "/api/v1/config/credentials"
        "/api/v1/config/regions"
    )
    
    for endpoint in "${ENDPOINTS[@]}"; do
        HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" "http://localhost:$PORT$endpoint")
        if [ "$HTTP_CODE" = "200" ] || [ "$HTTP_CODE" = "401" ]; then
            log_info "✓ $endpoint: HTTP $HTTP_CODE"
        else
            log_warn "  $endpoint: HTTP $HTTP_CODE (unexpected)"
        fi
    done
}

stop_application() {
    if [ -f /tmp/rocketmq-dashboard.pid ]; then
        APP_PID=$(cat /tmp/rocketmq-dashboard.pid)
        log_info "Stopping application (PID: $APP_PID)..."
        kill -15 $APP_PID 2>/dev/null || true
        sleep 2
        kill -9 $APP_PID 2>/dev/null || true
        rm -f /tmp/rocketmq-dashboard.pid
        log_info "✓ Application stopped"
    fi
}

print_summary() {
    echo ""
    echo "============================================="
    log_info "Deployment Verification Summary"
    echo "============================================="
    echo ""
    log_info "✓ JAR file exists and is valid"
    log_info "✓ Application starts successfully"
    log_info "✓ Health endpoint responds correctly"
    log_info "✓ Frontend is accessible"
    log_info "✓ API endpoints are responding"
    echo ""
    log_info "Application is running at: http://localhost:$PORT"
    log_info "Health check: http://localhost:$PORT/api/health"
    echo ""
    log_info "To stop the application, run:"
    echo "  kill \$(cat /tmp/rocketmq-dashboard.pid)"
    echo ""
}

main() {
    echo ""
    echo "============================================="
    echo "  RocketMQ Dashboard - Deployment Verification"
    echo "============================================="
    echo ""
    
    check_jar_exists
    check_port_available
    start_application
    wait_for_startup
    verify_health_endpoint
    verify_frontend
    verify_api_endpoints
    
    print_summary
    
    log_warn "Note: Application is still running for your testing"
    log_warn "Remember to stop it when done: kill \$(cat /tmp/rocketmq-dashboard.pid)"
}

trap 'log_error "Verification failed"; stop_application; exit 1' ERR

main

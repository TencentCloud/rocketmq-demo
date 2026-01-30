# RocketMQ Dashboard Deployment Guide

Complete guide for deploying RocketMQ Dashboard in various environments.

## Table of Contents
- [Prerequisites](#prerequisites)
- [Quick Start Deployment](#quick-start-deployment)
- [Manual Deployment](#manual-deployment)
- [Configuration](#configuration)
- [Production Deployment](#production-deployment)
- [Docker Deployment](#docker-deployment)
- [Troubleshooting](#troubleshooting)
- [Upgrade Guide](#upgrade-guide)

---

## Prerequisites

### System Requirements
- **Java**: JDK 17 or higher
- **Memory**: Minimum 2GB RAM, recommended 4GB+
- **Disk Space**: 500MB for application + logs
- **Operating System**: Linux, macOS, or Windows

### Required Software
- **Node.js**: v16+ (for building frontend)
- **npm**: v7+ (comes with Node.js)
- **Maven**: 3.6+ (for building backend)
- **Git**: For cloning the repository

### Network Requirements
- Port 8080 must be available (default application port)
- Outbound access to Tencent Cloud APIs
- Inbound access from dashboard users

### Tencent Cloud Requirements
- Valid Tencent Cloud account
- SecretId and SecretKey for API access
- RocketMQ cluster already deployed in Tencent Cloud
- Appropriate IAM permissions for RocketMQ operations

### Verification Commands
```bash
# Check Java version
java -version  # Should show 17 or higher

# Check Node.js and npm
node -v  # Should show v16 or higher
npm -v   # Should show v7 or higher

# Check Maven
mvn -v   # Should show 3.6 or higher

# Check port availability
lsof -i :8080  # Should return empty (port available)
# Or on Linux:
netstat -tuln | grep 8080
```

---

## Quick Start Deployment

### Step 1: Clone Repository
```bash
git clone <repository-url>
cd rocketmq-dashboard5-tencent
```

### Step 2: Configure Credentials
Create or edit `backend/rocketmq-dashboard-api/src/main/resources/application.yml`:

```yaml
rocketmq:
  tencent:
    secretId: "YOUR_SECRET_ID"
    secretKey: "YOUR_SECRET_KEY"
    region: "ap-guangzhou"  # Or your region
```

**Security Note**: Never commit credentials to version control. Use environment variables in production.

### Step 3: Build Using Automated Script
```bash
chmod +x scripts/package.sh
./scripts/package.sh
```

The script will:
1. ✅ Check all prerequisites (Java, Node.js, Maven)
2. ✅ Build frontend with optimization
3. ✅ Copy frontend assets to backend static directory
4. ✅ Build backend with Maven
5. ✅ Verify JAR file contains embedded frontend
6. ✅ Display build summary with file sizes

**Expected Output**:
```
========================================
  RocketMQ Dashboard Package Script
========================================

[1/5] Checking prerequisites...
✓ node (v18.17.0) - OK
✓ npm (9.8.1) - OK
✓ mvn (3.9.4) - OK
✓ java (17.0.8) - OK

[2/5] Building frontend...
✓ Frontend built successfully
Frontend bundle size: 2.45 MB

[3/5] Copying frontend to backend...
✓ Frontend copied to backend static resources

[4/5] Building backend with Maven...
✓ Backend built successfully

[5/5] Verifying final package...
✓ JAR file exists: rocketmq-dashboard-api-1.0.0-SNAPSHOT.jar (45.2 MB)
✓ JAR contains embedded frontend

========================================
Build completed successfully in 45 seconds
Package: backend/rocketmq-dashboard-api/target/rocketmq-dashboard-api-1.0.0-SNAPSHOT.jar
========================================
```

### Step 4: Deploy and Verify
```bash
chmod +x scripts/deploy-verify.sh
./scripts/deploy-verify.sh
```

The script will:
1. ✅ Check JAR file exists
2. ✅ Verify port 8080 is available
3. ✅ Start the application
4. ✅ Wait for startup (max 30 seconds)
5. ✅ Verify health endpoint
6. ✅ Check frontend accessibility
7. ✅ Test API endpoints

**Expected Output**:
```
========================================
  RocketMQ Dashboard Deployment Verification
========================================

[1/7] Checking JAR file...
✓ JAR file exists

[2/7] Checking port availability...
✓ Port 8080 is available

[3/7] Starting application...
✓ Application started (PID: 12345)

[4/7] Waiting for application startup...
✓ Application started successfully

[5/7] Verifying health endpoint...
✓ Health check passed

[6/7] Verifying frontend...
✓ Frontend is accessible

[7/7] Verifying API endpoints...
✓ Config API working
✓ Cluster API working

========================================
Deployment verification completed successfully!
Application is running at: http://localhost:8080
PID: 12345
========================================
```

### Step 5: Access Dashboard
Open your browser and navigate to:
```
http://localhost:8080
```

Default login (if authentication is enabled):
- Username: `admin`
- Password: Check application logs or configuration

---

## Manual Deployment

If you prefer manual control over each step:

### Step 1: Build Frontend
```bash
cd frontend

# Install dependencies
npm install

# Build for production
npm run build
```

**Output**: Frontend assets will be generated in `backend/rocketmq-dashboard-api/src/main/resources/static/`

**Verify**:
```bash
ls -lh ../backend/rocketmq-dashboard-api/src/main/resources/static/
# Should show: index.html, assets/, etc.
```

### Step 2: Build Backend
```bash
cd ../backend

# Build without tests (faster)
mvn clean package -DskipTests

# Or build with tests (recommended)
mvn clean package
```

**Output**: JAR file at `backend/rocketmq-dashboard-api/target/rocketmq-dashboard-api-1.0.0-SNAPSHOT.jar`

**Verify**:
```bash
ls -lh rocketmq-dashboard-api/target/*.jar
# Should show JAR file ~40-50MB

# Check embedded frontend
jar tf rocketmq-dashboard-api/target/rocketmq-dashboard-api-1.0.0-SNAPSHOT.jar | grep "BOOT-INF/classes/static"
# Should show frontend files
```

### Step 3: Run Application
```bash
cd rocketmq-dashboard-api/target

# Basic startup
java -jar rocketmq-dashboard-api-1.0.0-SNAPSHOT.jar

# With custom port
java -jar -Dserver.port=9090 rocketmq-dashboard-api-1.0.0-SNAPSHOT.jar

# With custom configuration file
java -jar rocketmq-dashboard-api-1.0.0-SNAPSHOT.jar --spring.config.location=/path/to/application.yml

# With JVM options (recommended for production)
java -Xms1g -Xmx2g -XX:+UseG1GC \
  -jar rocketmq-dashboard-api-1.0.0-SNAPSHOT.jar
```

### Step 4: Verify Deployment

**Health Check**:
```bash
curl http://localhost:8080/api/health
# Expected: {"code":200,"message":"Success","data":"UP","timestamp":...}
```

**Frontend Check**:
```bash
curl -I http://localhost:8080
# Expected: HTTP/1.1 200, Content-Type: text/html
```

**API Check**:
```bash
curl http://localhost:8080/api/config
# Expected: {"code":200,"message":"Success","data":{...},"timestamp":...}
```

---

## Configuration

### Application Configuration

Edit `backend/rocketmq-dashboard-api/src/main/resources/application.yml`:

```yaml
server:
  port: 8080  # Change application port

rocketmq:
  tencent:
    secretId: "${TENCENT_SECRET_ID}"      # Use environment variable
    secretKey: "${TENCENT_SECRET_KEY}"    # Use environment variable
    region: "${TENCENT_REGION:ap-guangzhou}"  # Default to ap-guangzhou
    endpoint: "rocketmq.tencentcloudapi.com"

spring:
  application:
    name: rocketmq-dashboard

logging:
  level:
    root: INFO
    com.rocketmq.dashboard: DEBUG  # Change to INFO in production
  file:
    name: logs/rocketmq-dashboard.log
    max-size: 10MB
    max-history: 30
```

### Environment Variables

**Recommended for Production**:
```bash
# Set environment variables
export TENCENT_SECRET_ID="AKIDxxxxxxxxxxxxx"
export TENCENT_SECRET_KEY="xxxxxxxxxxxxxxxx"
export TENCENT_REGION="ap-guangzhou"
export SERVER_PORT="8080"

# Run application
java -jar rocketmq-dashboard-api-1.0.0-SNAPSHOT.jar
```

**Using .env file** (requires additional setup):
```bash
# Create .env file
cat > .env << EOF
TENCENT_SECRET_ID=AKIDxxxxxxxxxxxxx
TENCENT_SECRET_KEY=xxxxxxxxxxxxxxxx
TENCENT_REGION=ap-guangzhou
SERVER_PORT=8080
EOF

# Load and run (Linux/macOS)
set -a; source .env; set +a
java -jar rocketmq-dashboard-api-1.0.0-SNAPSHOT.jar
```

### Command-Line Configuration

Override any property at runtime:
```bash
java -jar rocketmq-dashboard-api-1.0.0-SNAPSHOT.jar \
  --server.port=9090 \
  --rocketmq.tencent.region=ap-beijing \
  --logging.level.com.rocketmq.dashboard=INFO
```

---

## Production Deployment

### Systemd Service Setup (Linux)

Create service file `/etc/systemd/system/rocketmq-dashboard.service`:

```ini
[Unit]
Description=RocketMQ Dashboard
After=network.target

[Service]
Type=simple
User=rocketmq
Group=rocketmq
WorkingDirectory=/opt/rocketmq-dashboard

# Environment variables
Environment="TENCENT_SECRET_ID=AKIDxxxxxxxxxxxxx"
Environment="TENCENT_SECRET_KEY=xxxxxxxxxxxxxxxx"
Environment="TENCENT_REGION=ap-guangzhou"

# JVM options
Environment="JAVA_OPTS=-Xms1g -Xmx2g -XX:+UseG1GC -XX:MaxGCPauseMillis=200"

# Start command
ExecStart=/usr/bin/java $JAVA_OPTS -jar /opt/rocketmq-dashboard/rocketmq-dashboard-api-1.0.0-SNAPSHOT.jar

# Restart policy
Restart=always
RestartSec=10

# Logging
StandardOutput=journal
StandardError=journal
SyslogIdentifier=rocketmq-dashboard

[Install]
WantedBy=multi-user.target
```

**Setup Steps**:
```bash
# Create user and directory
sudo useradd -r -s /bin/false rocketmq
sudo mkdir -p /opt/rocketmq-dashboard
sudo chown rocketmq:rocketmq /opt/rocketmq-dashboard

# Copy JAR file
sudo cp backend/rocketmq-dashboard-api/target/rocketmq-dashboard-api-1.0.0-SNAPSHOT.jar /opt/rocketmq-dashboard/

# Reload systemd
sudo systemctl daemon-reload

# Enable service (start on boot)
sudo systemctl enable rocketmq-dashboard

# Start service
sudo systemctl start rocketmq-dashboard

# Check status
sudo systemctl status rocketmq-dashboard

# View logs
sudo journalctl -u rocketmq-dashboard -f
```

**Service Management**:
```bash
# Start
sudo systemctl start rocketmq-dashboard

# Stop
sudo systemctl stop rocketmq-dashboard

# Restart
sudo systemctl restart rocketmq-dashboard

# Status
sudo systemctl status rocketmq-dashboard

# Enable on boot
sudo systemctl enable rocketmq-dashboard

# Disable on boot
sudo systemctl disable rocketmq-dashboard
```

### Nginx Reverse Proxy

**Configuration** (`/etc/nginx/sites-available/rocketmq-dashboard`):
```nginx
upstream rocketmq_dashboard {
    server localhost:8080;
    keepalive 32;
}

server {
    listen 80;
    server_name rocketmq-dashboard.example.com;

    # Redirect to HTTPS
    return 301 https://$server_name$request_uri;
}

server {
    listen 443 ssl http2;
    server_name rocketmq-dashboard.example.com;

    # SSL certificates
    ssl_certificate /etc/ssl/certs/rocketmq-dashboard.crt;
    ssl_certificate_key /etc/ssl/private/rocketmq-dashboard.key;
    ssl_protocols TLSv1.2 TLSv1.3;
    ssl_ciphers HIGH:!aNULL:!MD5;

    # Logging
    access_log /var/log/nginx/rocketmq-dashboard-access.log;
    error_log /var/log/nginx/rocketmq-dashboard-error.log;

    # Proxy settings
    location / {
        proxy_pass http://rocketmq_dashboard;
        proxy_http_version 1.1;
        
        # Headers
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        
        # Timeouts
        proxy_connect_timeout 60s;
        proxy_send_timeout 60s;
        proxy_read_timeout 60s;
        
        # Keep-alive
        proxy_set_header Connection "";
    }

    # Static files caching
    location ~* \.(js|css|png|jpg|jpeg|gif|ico|svg|woff|woff2|ttf|eot)$ {
        proxy_pass http://rocketmq_dashboard;
        expires 1y;
        add_header Cache-Control "public, immutable";
    }
}
```

**Enable Configuration**:
```bash
# Enable site
sudo ln -s /etc/nginx/sites-available/rocketmq-dashboard /etc/nginx/sites-enabled/

# Test configuration
sudo nginx -t

# Reload Nginx
sudo systemctl reload nginx
```

### HTTPS/SSL Setup

**Using Let's Encrypt (Certbot)**:
```bash
# Install Certbot
sudo apt-get install certbot python3-certbot-nginx

# Obtain certificate
sudo certbot --nginx -d rocketmq-dashboard.example.com

# Auto-renewal test
sudo certbot renew --dry-run
```

**Manual Certificate**:
```bash
# Generate self-signed certificate (development only)
sudo openssl req -x509 -nodes -days 365 -newkey rsa:2048 \
  -keyout /etc/ssl/private/rocketmq-dashboard.key \
  -out /etc/ssl/certs/rocketmq-dashboard.crt
```

### Log Management

**Application Logs** (defined in `application.yml`):
```bash
# Log location
/opt/rocketmq-dashboard/logs/rocketmq-dashboard.log

# View logs
tail -f /opt/rocketmq-dashboard/logs/rocketmq-dashboard.log

# Search for errors
grep ERROR /opt/rocketmq-dashboard/logs/rocketmq-dashboard.log

# Log rotation (logrotate)
sudo cat > /etc/logrotate.d/rocketmq-dashboard << EOF
/opt/rocketmq-dashboard/logs/*.log {
    daily
    rotate 30
    compress
    delaycompress
    missingok
    notifempty
    create 644 rocketmq rocketmq
}
EOF
```

**System Logs** (systemd journal):
```bash
# View all logs
sudo journalctl -u rocketmq-dashboard

# Follow logs (tail -f style)
sudo journalctl -u rocketmq-dashboard -f

# Today's logs
sudo journalctl -u rocketmq-dashboard --since today

# Last hour
sudo journalctl -u rocketmq-dashboard --since "1 hour ago"

# Export logs
sudo journalctl -u rocketmq-dashboard -o json > logs.json
```

### Monitoring Setup

**Health Check Script** (`/opt/rocketmq-dashboard/healthcheck.sh`):
```bash
#!/bin/bash

URL="http://localhost:8080/api/health"
EXPECTED_CODE=200

response=$(curl -s -o /dev/null -w "%{http_code}" "$URL")

if [ "$response" -eq "$EXPECTED_CODE" ]; then
    echo "OK: Health check passed ($response)"
    exit 0
else
    echo "CRITICAL: Health check failed ($response)"
    exit 2
fi
```

**Cron Job** (check every 5 minutes):
```bash
# Add to crontab
*/5 * * * * /opt/rocketmq-dashboard/healthcheck.sh >> /var/log/rocketmq-healthcheck.log 2>&1
```

---

## Docker Deployment

### Dockerfile

Create `Dockerfile` in project root:

```dockerfile
# Stage 1: Build frontend
FROM node:18-alpine AS frontend-builder
WORKDIR /app/frontend
COPY frontend/package*.json ./
RUN npm ci
COPY frontend/ ./
RUN npm run build

# Stage 2: Build backend
FROM maven:3.9-eclipse-temurin-17 AS backend-builder
WORKDIR /app
COPY backend/pom.xml ./
COPY backend/rocketmq-dashboard-api/pom.xml ./rocketmq-dashboard-api/
RUN mvn dependency:go-offline -B
COPY backend/ ./
COPY --from=frontend-builder /app/frontend/dist ./rocketmq-dashboard-api/src/main/resources/static/
RUN mvn clean package -DskipTests -B

# Stage 3: Runtime
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Create non-root user
RUN addgroup -S rocketmq && adduser -S rocketmq -G rocketmq

# Copy JAR
COPY --from=backend-builder /app/rocketmq-dashboard-api/target/rocketmq-dashboard-api-*.jar app.jar

# Change ownership
RUN chown -R rocketmq:rocketmq /app

# Switch to non-root user
USER rocketmq

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/api/health || exit 1

# Expose port
EXPOSE 8080

# JVM options
ENV JAVA_OPTS="-Xms512m -Xmx1g -XX:+UseG1GC"

# Run application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
```

### Docker Compose

Create `docker-compose.yml`:

```yaml
version: '3.8'

services:
  rocketmq-dashboard:
    build: .
    image: rocketmq-dashboard:latest
    container_name: rocketmq-dashboard
    ports:
      - "8080:8080"
    environment:
      - TENCENT_SECRET_ID=${TENCENT_SECRET_ID}
      - TENCENT_SECRET_KEY=${TENCENT_SECRET_KEY}
      - TENCENT_REGION=${TENCENT_REGION:-ap-guangzhou}
      - JAVA_OPTS=-Xms1g -Xmx2g
    volumes:
      - ./logs:/app/logs
    restart: unless-stopped
    healthcheck:
      test: ["CMD", "wget", "--no-verbose", "--tries=1", "--spider", "http://localhost:8080/api/health"]
      interval: 30s
      timeout: 10s
      retries: 3
      start_period: 60s
    logging:
      driver: "json-file"
      options:
        max-size: "10m"
        max-file: "3"
```

### Build and Run

```bash
# Build image
docker build -t rocketmq-dashboard:latest .

# Run with environment variables
docker run -d \
  --name rocketmq-dashboard \
  -p 8080:8080 \
  -e TENCENT_SECRET_ID="AKIDxxxxx" \
  -e TENCENT_SECRET_KEY="xxxxxxxx" \
  -e TENCENT_REGION="ap-guangzhou" \
  -v $(pwd)/logs:/app/logs \
  rocketmq-dashboard:latest

# Or use docker-compose
docker-compose up -d

# View logs
docker logs -f rocketmq-dashboard

# Check health
docker exec rocketmq-dashboard wget -O- http://localhost:8080/api/health

# Stop container
docker stop rocketmq-dashboard

# Remove container
docker rm rocketmq-dashboard
```

---

## Troubleshooting

### Common Issues

#### Issue: Port 8080 Already in Use
```
***************************
APPLICATION FAILED TO START
***************************
Description: Web server failed to start. Port 8080 was already in use.
```

**Solution**:
```bash
# Find process using port 8080
lsof -i :8080
# Or: netstat -tuln | grep 8080

# Kill the process
kill -9 <PID>

# Or use a different port
java -jar -Dserver.port=9090 rocketmq-dashboard-api-1.0.0-SNAPSHOT.jar
```

#### Issue: OutOfMemoryError
```
java.lang.OutOfMemoryError: Java heap space
```

**Solution**:
```bash
# Increase heap size
java -Xms2g -Xmx4g -jar rocketmq-dashboard-api-1.0.0-SNAPSHOT.jar
```

#### Issue: Frontend Returns 404
**Symptom**: Backend APIs work, but http://localhost:8080 returns 404.

**Solution**:
```bash
# Verify frontend files in JAR
jar tf rocketmq-dashboard-api-1.0.0-SNAPSHOT.jar | grep "BOOT-INF/classes/static"

# If empty, rebuild with frontend
cd frontend && npm run build
cd ../backend && mvn clean package -DskipTests
```

#### Issue: Tencent Cloud Authentication Failed
```
HTTP 401: Authentication failed
```

**Solution**:
1. Verify credentials:
```bash
# Check if environment variables are set
echo $TENCENT_SECRET_ID
echo $TENCENT_SECRET_KEY
```

2. Verify credentials are correct in Tencent Cloud Console

3. Check IAM permissions (must have RocketMQ read/write access)

4. Verify region is correct

#### Issue: Health Check Fails
```bash
curl http://localhost:8080/api/health
# Returns: Connection refused
```

**Solution**:
```bash
# Check if application is running
ps aux | grep rocketmq-dashboard

# Check logs
tail -f logs/rocketmq-dashboard.log

# Check if port is listening
netstat -tuln | grep 8080

# Try local health check
curl http://127.0.0.1:8080/api/health
```

#### Issue: API Returns 500 Internal Server Error
**Solution**:
1. Check application logs:
```bash
tail -f logs/rocketmq-dashboard.log | grep ERROR
```

2. Enable debug logging:
```bash
java -jar -Dlogging.level.com.rocketmq.dashboard=DEBUG rocketmq-dashboard-api-1.0.0-SNAPSHOT.jar
```

3. Verify Tencent Cloud connectivity:
```bash
curl -v https://rocketmq.tencentcloudapi.com
```

### Log Analysis

**Find startup errors**:
```bash
grep "ERROR" logs/rocketmq-dashboard.log | head -20
```

**Find recent exceptions**:
```bash
tail -100 logs/rocketmq-dashboard.log | grep -A 5 "Exception"
```

**Check startup time**:
```bash
grep "Started RocketMqDashboardApplication" logs/rocketmq-dashboard.log
```

### Performance Issues

#### High CPU Usage
```bash
# Check Java process
top -p $(pgrep -f rocketmq-dashboard)

# Get thread dump
jstack <PID> > thread_dump.txt

# Enable GC logging
java -Xlog:gc*:file=gc.log -jar rocketmq-dashboard-api-1.0.0-SNAPSHOT.jar
```

#### High Memory Usage
```bash
# Get heap dump
jmap -dump:format=b,file=heap_dump.hprof <PID>

# Analyze with tools like Eclipse MAT or VisualVM
```

### Getting Help

If issues persist:
1. Check application logs thoroughly
2. Review [API Documentation](API.md)
3. Review [Testing Guide](TESTING.md)
4. Check GitHub issues
5. Contact support with:
   - Log files
   - Configuration (sanitized)
   - Steps to reproduce
   - Environment details (OS, Java version, etc.)

---

## Upgrade Guide

### Backup Procedures

**Before Upgrading**:
```bash
# Stop application
sudo systemctl stop rocketmq-dashboard

# Backup current JAR
cp /opt/rocketmq-dashboard/rocketmq-dashboard-api-1.0.0-SNAPSHOT.jar \
   /opt/rocketmq-dashboard/backups/rocketmq-dashboard-api-1.0.0-SNAPSHOT.jar.backup

# Backup configuration
cp /opt/rocketmq-dashboard/application.yml \
   /opt/rocketmq-dashboard/backups/application.yml.backup

# Backup logs
tar -czf /opt/rocketmq-dashboard/backups/logs-$(date +%Y%m%d).tar.gz \
   /opt/rocketmq-dashboard/logs/
```

### Upgrade Steps

1. **Download New Version**:
```bash
cd /tmp
git clone <repository-url>
cd rocketmq-dashboard5-tencent
```

2. **Build New Version**:
```bash
./scripts/package.sh
```

3. **Stop Current Application**:
```bash
sudo systemctl stop rocketmq-dashboard
```

4. **Deploy New Version**:
```bash
sudo cp backend/rocketmq-dashboard-api/target/rocketmq-dashboard-api-1.0.0-SNAPSHOT.jar \
   /opt/rocketmq-dashboard/
```

5. **Restore Configuration** (if needed):
```bash
# Review configuration changes in new version
diff /opt/rocketmq-dashboard/backups/application.yml.backup \
     backend/rocketmq-dashboard-api/src/main/resources/application.yml

# Merge configuration as needed
```

6. **Start New Version**:
```bash
sudo systemctl start rocketmq-dashboard
```

7. **Verify Upgrade**:
```bash
# Check logs
sudo journalctl -u rocketmq-dashboard -f

# Check health
curl http://localhost:8080/api/health

# Test frontend
curl http://localhost:8080

# Test APIs
curl http://localhost:8080/api/config
```

### Rollback Procedures

If upgrade fails:

```bash
# Stop application
sudo systemctl stop rocketmq-dashboard

# Restore backup
cp /opt/rocketmq-dashboard/backups/rocketmq-dashboard-api-1.0.0-SNAPSHOT.jar.backup \
   /opt/rocketmq-dashboard/rocketmq-dashboard-api-1.0.0-SNAPSHOT.jar

# Restore configuration
cp /opt/rocketmq-dashboard/backups/application.yml.backup \
   /opt/rocketmq-dashboard/application.yml

# Start old version
sudo systemctl start rocketmq-dashboard

# Verify rollback
curl http://localhost:8080/api/health
```

### Zero-Downtime Upgrade

For production environments with load balancer:

1. Deploy new version on secondary server
2. Verify new version works correctly
3. Switch load balancer to new server
4. Monitor for issues
5. Upgrade original server
6. Balance traffic across both servers
7. Decommission old version after stability period

---

## Additional Resources

- [API Documentation](API.md) - Complete API reference
- [Testing Guide](TESTING.md) - How to run and write tests
- [README](../README.md) - Project overview and quick start

---

**Last Updated**: 2026-01-30  
**Version**: 1.0.0

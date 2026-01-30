# RocketMQ Dashboard

A modern, full-stack dashboard for monitoring and managing Apache RocketMQ clusters.

## Tech Stack

### Frontend
- **Vue 3** - Progressive JavaScript framework
- **TypeScript** - Type-safe development
- **Vite** - Fast build tool
- **TDesign Vue Next** - Enterprise-level UI component library
- **Vue Router** - Official routing solution
- **Pinia** - State management

### Backend
- **Spring Boot 3.2.1** - Application framework
- **Java 17** - Runtime environment
- **Maven** - Dependency management
- **Lombok** - Reduce boilerplate code

## Project Structure

```
rocketmq-dashboard5-tencent/
├── frontend/                # Frontend Vue 3 application
│   ├── src/
│   │   ├── components/     # Reusable components
│   │   ├── views/          # Page components
│   │   ├── router/         # Route configuration
│   │   ├── stores/         # Pinia state management
│   │   ├── App.vue         # Root component
│   │   └── main.ts         # Application entry
│   ├── package.json
│   └── vite.config.ts
│
├── backend/                 # Backend Spring Boot application
│   ├── pom.xml             # Parent POM
│   └── rocketmq-dashboard-api/
│       ├── src/
│       │   ├── main/
│       │   │   ├── java/
│       │   │   │   └── com/rocketmq/dashboard/
│       │   │   │       ├── DashboardApplication.java
│       │   │   │       └── controller/
│       │   │   └── resources/
│       │   │       ├── application.yml
│       │   │       └── static/    # Frontend build output
│       │   └── test/
│       └── pom.xml
│
├── build-frontend.sh        # Frontend build script
└── README.md
```

## Prerequisites

- **Node.js** >= 18.x
- **npm** >= 9.x
- **Java** >= 17
- **Maven** >= 3.8

## Quick Start

### 1. Clone the repository

```bash
git clone <repository-url>
cd rocketmq-dashboard5-tencent
```

### 2. Start Frontend (Development Mode)

```bash
cd frontend
npm install
npm run dev
```

Frontend will be available at: http://localhost:3000

### 3. Start Backend

```bash
cd backend
mvn spring-boot:run -pl rocketmq-dashboard-api
```

Backend API will be available at: http://localhost:8080

## Development Guide

### Frontend Development

#### Install Dependencies
```bash
cd frontend
npm install
```

#### Development Server
```bash
npm run dev
```

#### Build for Production
```bash
npm run build
```

The build output will be automatically placed in `backend/rocketmq-dashboard-api/src/main/resources/static/`

#### Type Checking
```bash
npm run build  # Includes type checking via vue-tsc
```

### Backend Development

#### Run Application
```bash
cd backend
mvn spring-boot:run -pl rocketmq-dashboard-api
```

#### Run Tests
```bash
mvn test
```

#### Package Application
```bash
mvn clean package
```

The packaged JAR will include the frontend build automatically.

### API Endpoints

#### Health Check
```
GET http://localhost:8080/api/health
```

Response:
```json
{
  "status": "UP",
  "application": "RocketMQ Dashboard",
  "version": "1.0.0"
}
```

## Building for Production

### Option 1: Build Frontend Only
```bash
./build-frontend.sh
```

### Option 2: Build Complete Application
```bash
cd frontend
npm run build

cd ../backend
mvn clean package
```

The final JAR file will be located at:
```
backend/rocketmq-dashboard-api/target/rocketmq-dashboard-api-1.0.0-SNAPSHOT.jar
```

### Run Production Build
```bash
java -jar backend/rocketmq-dashboard-api/target/rocketmq-dashboard-api-1.0.0-SNAPSHOT.jar
```

Access the application at: http://localhost:8080

## Configuration

### Frontend Configuration
Edit `frontend/vite.config.ts`:
- Development server port: `server.port`
- API proxy configuration: `server.proxy`
- Build output directory: `build.outDir`

### Backend Configuration
Edit `backend/rocketmq-dashboard-api/src/main/resources/application.yml`:
- Server port: `server.port`
- Application name: `spring.application.name`
- Logging levels: `logging.level`

## Troubleshooting

### Frontend Issues

**Port already in use:**
```bash
# Change port in vite.config.ts or use environment variable
PORT=3001 npm run dev
```

**Build fails:**
```bash
# Clean and reinstall dependencies
rm -rf node_modules package-lock.json
npm install
```

### Backend Issues

**Port 8080 already in use:**
```bash
# Change port in application.yml or use command line
mvn spring-boot:run -Dspring-boot.run.arguments=--server.port=8081
```

**Maven build fails:**
```bash
# Clean and rebuild
mvn clean install -U
```

## Contributing

1. Fork the repository
2. Create your feature branch: `git checkout -b feature/my-feature`
3. Commit your changes: `git commit -am 'Add some feature'`
4. Push to the branch: `git push origin feature/my-feature`
5. Submit a pull request

## License

[Add your license here]

## Contact

[Add contact information here]

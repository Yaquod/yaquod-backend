# Yaquod Backend

A Spring Boot backend application for managing vehicles with MQTT integration, authentication, and comprehensive security features.

## Table of Contents

- [Overview](#overview)
- [Features](#features)
- [Tech Stack](#tech-stack)
- [Prerequisites](#prerequisites)
- [Getting Started](#getting-started)
- [Project Structure](#project-structure)
- [Configuration](#configuration)
- [API Endpoints](#api-endpoints)
- [Security](#security)
- [Testing](#testing)
- [Docker Support](#docker-support)
- [Development](#development)
- [Contributing](#contributing)
- [License](#license)

## Overview

Yaquod Backend is a robust vehicle management system built with Spring Boot. It provides RESTful APIs for managing vehicles, users, and authentication, with real-time MQTT integration for location and status updates.

## Features

- **Vehicle Management**: Complete CRUD operations for vehicles
- **User Management**: User registration, authentication, and profile management
- **Role-Based Access Control**: Admin and user roles with different permissions
- **MQTT Integration**: Real-time vehicle location and status updates
- **Security**: JWT-based authentication and authorization
- **Validation**: Comprehensive input validation including VIN number validation
- **Database Support**: PostgreSQL for production, H2 for testing
- **Docker Ready**: Containerized deployment with Docker Compose

## Tech Stack

- **Framework**: Spring Boot 3.x
- **Language**: Java 17+
- **Build Tool**: Maven
- **Database**: PostgreSQL (production), H2 (testing)
- **Security**: Spring Security with JWT
- **Messaging**: MQTT
- **Testing**: JUnit 5, MockMvc, Testcontainers
- **Containerization**: Docker

## Prerequisites

- Java 17 or higher
- Maven 3.6+
- Docker and Docker Compose (optional)
- PostgreSQL (if running locally without Docker)

## Getting Started

### Clone the Repository

```bash
git clone https://github.com/Yaquod/yaquod-backend.git
cd yaquod-backend
```

### Build the Project

```bash
./mvnw clean install
```

### Run Locally

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

### Run with Docker

```bash
docker-compose up -d
```

The application will be available at `http://localhost:8080`

## Project Structure

```
yaquod-backend/
├── src/
│   ├── main/
│   │   ├── java/com/yaquodorg/yaquod/
│   │   │   ├── controller/      # REST controllers
│   │   │   ├── service/         # Business logic
│   │   │   ├── repository/      # Data access layer
│   │   │   ├── entity/          # JPA entities
│   │   │   ├── dtos/            # Data transfer objects
│   │   │   ├── security/        # Security configuration
│   │   │   └── config/          # Application configuration
│   │   └── resources/
│   │       ├── application.yml           # Main config
│   │       ├── application-dev.yml       # Dev profile
│   │       ├── application-test.yml      # Test profile
│   │       └── application-docker.yml    # Docker profile
│   └── test/
│       └── java/                # Test classes
├── docker-compose.yml           # Docker composition
├── Dockerfile                   # Container definition
├── pom.xml                     # Maven dependencies
└── README.md
```

## Configuration

### Application Profiles

- **dev**: Development environment with H2/PostgreSQL
- **test**: Testing environment with H2 in-memory database
- **docker**: Docker container environment

### Environment Variables

Key environment variables for Docker deployment:

```properties
SPRING_DATASOURCE_URL=jdbc:postgresql://db:5432/yaquod
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=your_password
JWT_SECRET=your_jwt_secret
MQTT_BROKER_URL=tcp://mqtt-broker:1883
```

## API Endpoints

### Authentication

- `POST /api/auth/register` - Register new user
- `POST /api/auth/login` - User login
- `POST /api/auth/refresh` - Refresh JWT token

### Vehicles (Admin Only)

- `GET /api/vehicles` - Get all vehicles
- `GET /api/vehicles/id/{id}` - Get vehicle by ID
- `GET /api/vehicles/vin/{vin}` - Get vehicle by VIN
- `POST /api/vehicles` - Create new vehicle
- `PATCH /api/vehicles` - Update vehicle
- `DELETE /api/vehicles/id/{id}` - Delete vehicle
- `PATCH /api/vehicles/vin/{vin}/location-update` - Trigger location update via MQTT
- `PATCH /api/vehicles/vin/{vin}/status-update` - Trigger status update via MQTT

### Users

- `GET /api/users` - Get all users (Admin)
- `GET /api/users/{id}` - Get user by ID
- `PATCH /api/users` - Update user profile
- `DELETE /api/users/{id}` - Delete user (Admin)

## Security

### Authentication Flow

1. User registers via `/api/auth/register`
2. User logs in via `/api/auth/login` and receives JWT token
3. Token must be included in `Authorization` header as `Bearer {token}`
4. Tokens expire after configured duration

### Role-Based Access

- **ADMIN**: Full access to all endpoints
- **USER**: Limited access to user-specific endpoints
- **DRIVER**: Special permissions for driver operations

## Testing

### Run All Tests

```bash
./mvnw test
```

### Test Categories

- **Unit Tests**: Service and repository layer tests
- **Integration Tests**: Full Spring context with security
- **Controller Tests**: MockMvc-based API testing

### Test Coverage

The project includes comprehensive tests for:
- Authentication flows
- Vehicle CRUD operations
- Security and authorization
- MQTT integration
- Input validation
- Concurrent operations
- Edge cases and error handling

## Docker Support

### Build Docker Image

```bash
docker build -t yaquod-backend .
```

### Docker Compose Services

- **app**: Spring Boot application
- **db**: PostgreSQL database
- **mqtt-broker**: MQTT broker (if configured)

### Docker Commands

```bash
# Start services
docker-compose up -d

# View logs
docker-compose logs -f app

# Stop services
docker-compose down

# Rebuild and restart
docker-compose up -d --build
```

## Development

### Code Style

- Follow Java naming conventions
- Use meaningful variable and method names
- Add JavaDoc comments for public APIs
- Keep methods focused and concise

### Branch Strategy

- `main`: Production-ready code
- `develop`: Integration branch
- `feature/*`: New features
- `bugfix/*`: Bug fixes

### Commit Messages

Follow conventional commits:
```
feat: add vehicle location tracking
fix: resolve JWT token validation issue
docs: update API documentation
test: add integration tests for vehicle controller
```

## Contributing

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'feat: add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request



For issues or questions, please open an issue on GitHub or contact the maintainers.
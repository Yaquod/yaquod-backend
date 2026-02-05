# Yaquod Backend

A Spring Boot backend application for the Yaquod project, providing vehicle tracking, trip management, and user authentication services with real-time MQTT integration and geospatial capabilities.

## Table of Contents

- [Features](#features)
- [Tech Stack](#tech-stack)
- [Prerequisites](#prerequisites)
- [Getting Started](#getting-started)
  - [Local Development](#local-development)
  - [Docker Deployment](#docker-deployment)
- [Configuration](#configuration)
- [API Endpoints](#api-endpoints)
- [Testing](#testing)
- [Project Structure](#project-structure)
- [License](#license)

## Features

- 🔐 **Authentication & Authorization**
  - JWT-based authentication with access and refresh tokens
  - OAuth2 integration (Google)
  - Email verification with OTP codes
  - Role-based access control (ADMIN, CLIENT)

- 🚗 **Vehicle Management**
  - CRUD operations for vehicles
  - Real-time location tracking via MQTT
  - Geospatial data support with PostGIS

- 🗺️ **Trip Management**
  - Create, read, update, and delete trips
  - Track trips by user or vehicle
  - Historical trip data

- 📧 **Email Notifications**
  - SMTP integration for verification codes
  - Firebase Cloud Messaging integration

- ⚡ **Real-time Communication**
  - MQTT broker integration for live location updates
  - Topic-based message handling

- 🛡️ **Security Features**
  - Rate limiting
  - Spring Security
  - Password encryption with BCrypt

## Tech Stack

- **Framework**: Spring Boot 3.5.6
- **Language**: Java 21
- **Database**: PostgreSQL with PostGIS extension
- **Authentication**: JWT, OAuth2
- **Real-time**: MQTT (Eclipse Paho)
- **Geospatial**: Hibernate Spatial, JTS
- **Testing**: JUnit 5, Spring Security Test, H2 with H2GIS
- **Build Tool**: Maven
- **Containerization**: Docker, Docker Compose

### Key Dependencies

- Spring Boot Starter (Web, Security, Data JPA, Mail, Actuator, OAuth2 Client)
- PostgreSQL & PostGIS
- JWT (JJWT 0.11.5)
- Eclipse Paho MQTT Client
- Firebase Admin SDK
- Lombok
- Hibernate Spatial
- Bean Validation

## Prerequisites

- **Java 21** or higher
- **Maven 3.9+**
- **Docker & Docker Compose** (for containerized deployment)
- **PostgreSQL 16** with PostGIS extension (for local development)
- **MQTT Broker** (e.g., Mosquitto) - optional for MQTT features

## Getting Started

### Local Development

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd yaquod-backend
   ```

2. **Configure the database**
   
   Create a PostgreSQL database with PostGIS:
   ```sql
   CREATE DATABASE yaquod;
   CREATE EXTENSION postgis;
   ```

3. **Configure application properties**
   
   Update `src/main/resources/application.yml` with your settings:
   - Database credentials
   - SMTP settings for email
   - OAuth2 credentials
   - MQTT broker URL
   - JWT secret key

4. **Build the project**
   ```bash
   ./mvnw clean install
   ```
   
   Or on Windows:
   ```bash
   mvnw.cmd clean install
   ```

5. **Run the application**
   ```bash
   ./mvnw spring-boot:run
   ```
   
   The application will start on `http://localhost:8000`

### Docker Deployment

The project includes Docker support for easy deployment with all dependencies.

1. **Start all services**
   ```bash
   docker-compose up -d
   ```

   This will start:
   - PostGIS database (PostgreSQL 16 with PostGIS 3.4) on port 5433
   - Spring Boot application on port 8000

2. **Stop all services**
   ```bash
   docker-compose down
   ```

3. **View logs**
   ```bash
   docker-compose logs -f springboot
   ```

**Note for Linux users**: Update the `MQTT_BROKER_URL` environment variable in `docker-compose.yml` to use your Linux IP address instead of `host.docker.internal`.

## Configuration

### Application Profiles

The application supports multiple profiles:

- **default** (`application.yml`): Local development
- **docker** (`application-docker.yml`): Docker environment
- **dev** (`application-dev.yml`): Development environment
- **test** (`application-test.yml`): Testing with H2 database

### Environment Variables

Key environment variables for Docker deployment:

```yaml
SPRING_PROFILES_ACTIVE: docker
SPRING_DATASOURCE_URL: jdbc:postgresql://postgis:5432/yaquod
SPRING_DATASOURCE_USERNAME: yaquod
SPRING_DATASOURCE_PASSWORD: yaquod
MQTT_BROKER_URL: tcp://host.docker.internal:1883
```

### MQTT Configuration

Configure MQTT broker settings in `application.yml`:

```yaml
mqtt:
  broker-url: tcp://localhost:1883
  client-id: spring-boot-clientF
  username: # optional
  password: # optional
  topics:
    - topic/order_update_location
    - topic/update_location
  qos: 1
```

## API Endpoints

All endpoints return responses in the following format:

```json
{
  "success": true,
  "message": "Operation successful",
  "data": { /* response data */ }
}
```

### Authentication (`/api/auth`)

#### Register Admin User
**POST** `/api/auth/admin/signup`

**Request Body:**
```json
{
  "email": "admin@example.com",
  "password": "SecurePass123",
  "firstName": "John",
  "lastName": "Doe",
  "phoneNumber": "+1234567890"
}
```

**Response (201 Created):**
```json
{
  "success": true,
  "message": "Success",
  "data": {
    "id": 1,
    "email": "admin@example.com",
    "firstName": "John",
    "lastName": "Doe",
    "phoneNumber": "+1234567890",
    "role": "ADMIN",
    "emailVerified": false
  }
}
```

#### Register Client User
**POST** `/api/auth/client/signup`

**Request Body:**
```json
{
  "email": "client@example.com",
  "password": "SecurePass123",
  "firstName": "Jane",
  "lastName": "Smith",
  "phoneNumber": "+1234567890"
}
```

**Response (201 Created):** Same format as admin signup with `"role": "CLIENT"`

#### Verify Email
**POST** `/api/auth/verify-code`

**Request Body:**
```json
{
  "email": "user@example.com",
  "code": 123456
}
```

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Success",
  "data": {
    "message": "Account Verified Successfully!"
  }
}
```

#### Regenerate OTP Code
**POST** `/api/auth/regenerate-code`

**Request Body:**
```json
{
  "email": "user@example.com"
}
```

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Success",
  "data": {
    "message": "OTP regenerated successfully. Check your email for the new OTP."
  }
}
```

#### Login
**POST** `/api/auth/login`

**Request Body:**
```json
{
  "email": "user@example.com",
  "password": "SecurePass123",
  "fcmToken": "firebase-token-here"
}
```

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Success",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "accessTokenExpiresIn": "2026-01-31T10:30:00.000+00:00",
    "refreshTokenExpiresIn": "2026-02-23T10:30:00.000+00:00",
    "user": {
      "id": 1,
      "email": "user@example.com",
      "firstName": "John",
      "lastName": "Doe",
      "role": "CLIENT"
    }
  }
}
```

#### Refresh Token
**GET** `/api/auth/token-refresh`

**Headers:**
```
Authorization: Bearer <refresh-token>
```

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Success",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "accessTokenExpiresIn": "2026-01-31T10:30:00.000+00:00",
    "refreshTokenExpiresIn": "2026-02-23T10:30:00.000+00:00"
  }
}
```

---

### Vehicles (`/api/vehicles`)

**Note:** All vehicle endpoints require ADMIN role authentication.

**Headers Required:**
```
Authorization: Bearer <access-token>
```

#### Create Vehicle
**POST** `/api/vehicles`

**Request Body:**
```json
{
  "vinNumber": "1HGBH41JXMN109186",
  "plateNo": "ABC-1234",
  "color": "Blue",
  "carCompany": "Honda",
  "model": "Civic 2023",
  "seats": 5
}
```

**Response (201 Created):**
```json
{
  "success": true,
  "message": "Success",
  "data": {
    "id": 1,
    "vinNumber": "1HGBH41JXMN109186",
    "plateNo": "ABC-1234",
    "color": "Blue",
    "carCompany": "Honda",
    "model": "Civic 2023",
    "seats": 5,
    "status": "IDLE",
    "lastUpdatedLong": 0.0,
    "lastUpdatedLat": 0.0
  }
}
```

#### Get All Vehicles
**GET** `/api/vehicles`

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Success",
  "data": [
    {
      "id": 1,
      "vinNumber": "1HGBH41JXMN109186",
      "plateNo": "ABC-1234",
      "color": "Blue",
      "carCompany": "Honda",
      "model": "Civic 2023",
      "seats": 5,
      "status": "IDLE"
    }
  ]
}
```

#### Get Vehicle by ID
**GET** `/api/vehicles/id/{vehicleId}`

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Success",
  "data": {
    "id": 1,
    "vinNumber": "1HGBH41JXMN109186",
    "plateNo": "ABC-1234",
    "color": "Blue",
    "carCompany": "Honda",
    "model": "Civic 2023",
    "seats": 5,
    "status": "IDLE"
  }
}
```

#### Get Vehicle by VIN
**GET** `/api/vehicles/vin/{vinNumber}`

**Response (200 OK):** Same format as Get Vehicle by ID

#### Update Vehicle
**PATCH** `/api/vehicles`

**Request Body:**
```json
{
  "vinNumber": "1HGBH41JXMN109186",
  "plateNo": "XYZ-5678",
  "color": "Red",
  "carCompany": "Honda",
  "model": "Civic 2023",
  "seats": 5
}
```

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Success",
  "data": {
    "id": 1,
    "vinNumber": "1HGBH41JXMN109186",
    "plateNo": "XYZ-5678",
    "color": "Red",
    "carCompany": "Honda",
    "model": "Civic 2023",
    "seats": 5,
    "status": "IDLE"
  }
}
```

#### Delete Vehicle
**DELETE** `/api/vehicles/id/{vehicleId}`

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Success",
  "data": {
    "message": "Vehicle deleted successfully!"
  }
}
```

#### Update Vehicle Location (via MQTT)
**PATCH** `/api/vehicles/vin/{vinNumber}/location-update`

Sends MQTT message to vehicle to request location update.

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Success",
  "data": {
    "message": "Order signal sent!"
  }
}
```

#### Update Vehicle Status (via MQTT)
**PATCH** `/api/vehicles/vin/{vinNumber}/status-update`

Sends MQTT message to vehicle to request status update.

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Success",
  "data": {
    "message": "Order signal sent!"
  }
}
```

---

### Trips (`/api/trips`)

**Headers Required:**
```
Authorization: Bearer <access-token>
```

#### Create Trip Request
**POST** `/api/trips/request`

**Request Body:**
```json
{
  "startLong": -73.935242,
  "startLat": 40.730610,
  "endLong": -73.989308,
  "endLat": 40.741895
}
```

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Success",
  "data": {
    "id": 1,
    "status": "PENDING",
    "createdAt": "2026-01-24T10:30:00.000+00:00",
    "estimatedTime": 15.5,
    "estimatedCost": 25.00
  }
}
```

#### Get Request Status
**GET** `/api/trips/request/status/{requestId}`

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Success",
  "data": {
    "id": 1,
    "status": "ACCEPTED",
    "createdAt": "2026-01-24T10:30:00.000+00:00",
    "estimatedTime": 15.5,
    "estimatedCost": 25.00
  }
}
```

#### Get Trip by Request ID
**GET** `/api/trips/by-request/{requestId}`

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Success",
  "data": {
    "id": 1,
    "status": "IN_PROGRESS",
    "startedAt": "2026-01-24T10:35:00.000+00:00",
    "endedAt": null,
    "request": {
      "id": 1,
      "status": "ACCEPTED"
    }
  }
}
```

#### Get Trip by ID
**GET** `/api/trips/{tripId}`

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Success",
  "data": {
    "id": 1,
    "status": "COMPLETED",
    "startedAt": "2026-01-24T10:35:00.000+00:00",
    "endedAt": "2026-01-24T10:50:00.000+00:00",
    "updatedAt": "2026-01-24T10:50:00.000+00:00"
  }
}
```

#### Delete Trip (Admin Only)
**DELETE** `/api/trips/{tripId}`

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Success",
  "data": {
    "message": "Trip deleted successfully"
  }
}
```

#### Get All Trips
**GET** `/api/trips`

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Success",
  "data": [
    {
      "id": 1,
      "status": "COMPLETED",
      "startedAt": "2026-01-24T10:35:00.000+00:00",
      "endedAt": "2026-01-24T10:50:00.000+00:00"
    },
    {
      "id": 2,
      "status": "IN_PROGRESS",
      "startedAt": "2026-01-24T11:00:00.000+00:00",
      "endedAt": null
    }
  ]
}
```

#### Get User's Trips
**GET** `/api/trips/user`

Returns trips for the authenticated user.

**Response (200 OK):** Same format as Get All Trips

#### Get Last N Trips
**GET** `/api/trips/last/{n}`

Returns the last N trips for the authenticated user.

**Example:** `/api/trips/last/5`

**Response (200 OK):** Same format as Get All Trips (limited to N items)

#### Get Trips by Vehicle VIN
**GET** `/api/trips/vehicle/{vinNumber}`

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Success",
  "data": [
    {
      "id": 1,
      "status": "COMPLETED",
      "startedAt": "2026-01-24T10:35:00.000+00:00",
      "endedAt": "2026-01-24T10:50:00.000+00:00"
    }
  ]
}
```

#### Decline Trip Request (Client Only)
**POST** `/api/trips/request/{requestId}/decline`

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Success",
  "data": {
    "message": "Trip request declined successfully"
  }
}
```

---

### Error Responses

All endpoints may return error responses in the following format:

**400 Bad Request:**
```json
{
  "success": false,
  "message": "Failed to create vehicle: VIN number already exists",
  "data": null
}
```

**401 Unauthorized:**
```json
{
  "success": false,
  "message": "Unauthorized",
  "data": null
}
```

**403 Forbidden:**
```json
{
  "success": false,
  "message": "Access Denied",
  "data": null
}
```

**500 Internal Server Error:**
```json
{
  "success": false,
  "message": "Internal Server Error: <error details>",
  "data": null
}
```

## Testing

The project includes comprehensive test coverage:

- Unit tests for services and repositories
- Integration tests for controllers with security
- Test coverage for MQTT integration
- Rate limiting tests

**Run all tests:**
```bash
./mvnw test
```

**Run specific test class:**
```bash
./mvnw test -Dtest=VehicleControllerTest
```

Tests use H2 in-memory database with H2GIS for geospatial testing.

## Project Structure

```
src/
├── main/
│   ├── java/com/yaquodorg/yaquod/
│   │   ├── config/          # Spring configuration
│   │   ├── controller/      # REST controllers
│   │   ├── dtos/            # Data transfer objects
│   │   ├── entity/          # JPA entities
│   │   ├── filter/          # Security filters (e.g., rate limiting)
│   │   ├── repository/      # JPA repositories
│   │   ├── response/        # API response models
│   │   ├── service/         # Business logic
│   │   ├── utils/           # Utility classes
│   │   └── YaquodApplication.java
│   └── resources/
│       ├── application.yml
│       ├── application-dev.yml
│       ├── application-docker.yml
│       └── application-test.yml
└── test/
    └── java/com/yaquodorg/yaquod/
        ├── controller/      # Controller tests
        ├── repository/      # Repository tests
        ├── service/         # Service tests
        └── filter/          # Filter tests
```

## License

This project is licensed under the terms specified in the [LICENSE](LICENSE) file.

---

**Note**: Make sure to update sensitive configuration values (JWT secret, OAuth2 credentials, SMTP credentials) before deploying to production.

# Architecture Documentation

## Overview

Yaquod Backend is a **Spring Boot 3.5.6** application built with **Java 21** that provides a comprehensive vehicle management and trip coordination system. The application follows a layered architecture pattern with clear separation of concerns.

## System Architecture

### High-Level Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                     Client Applications                      │
│              (Web, Mobile, IoT Devices)                      │
└───────────────────────────┬─────────────────────────────────┘
                            │
                            │ REST API / MQTT
                            │
┌───────────────────────────▼─────────────────────────────────┐
│                    API Gateway Layer                         │
│         (Security Filters, CORS, Rate Limiting)              │
└───────────────────────────┬─────────────────────────────────┘
                            │
┌───────────────────────────▼─────────────────────────────────┐
│                   Controller Layer                           │
│   ┌──────────────┬───────────────┬──────────────────┐       │
│   │Authentication│   Vehicle     │      Trip        │       │
│   │  Controller  │  Controller   │   Controller     │       │
│   └──────────────┴───────────────┴──────────────────┘       │
└───────────────────────────┬─────────────────────────────────┘
                            │
┌───────────────────────────▼─────────────────────────────────┐
│                    Service Layer                             │
│   ┌──────────────┬───────────────┬──────────────────┐       │
│   │   Auth       │   Vehicle     │      Trip        │       │
│   │  Service     │   Service     │   Service        │       │
│   ├──────────────┼───────────────┼──────────────────┤       │
│   │    JWT       │     MQTT      │     Request      │       │
│   │  Service     │   Service     │   Service        │       │
│   ├──────────────┼───────────────┼──────────────────┤       │
│   │    Mail      │     User      │                  │       │
│   │  Service     │   Service     │                  │       │
│   └──────────────┴───────────────┴──────────────────┘       │
└───────────────────────────┬─────────────────────────────────┘
                            │
┌───────────────────────────▼─────────────────────────────────┐
│                 Repository Layer (JPA)                       │
│   ┌──────────────┬───────────────┬──────────────────┐       │
│   │     User     │   Vehicle     │      Trip        │       │
│   │  Repository  │  Repository   │   Repository     │       │
│   │              │               │    Request       │       │
│   │              │               │   Repository     │       │
│   └──────────────┴───────────────┴──────────────────┘       │
└───────────────────────────┬─────────────────────────────────┘
                            │
┌───────────────────────────▼─────────────────────────────────┐
│              PostgreSQL Database (PostGIS)                   │
│         ┌────────────────────────────────────┐              │
│         │  Users │ Vehicles │ Trips │ Requests│             │
│         │  (with spatial/geolocation support) │             │
│         └────────────────────────────────────┘              │
└─────────────────────────────────────────────────────────────┘

External Systems:
┌─────────────┐  ┌─────────────┐  ┌─────────────┐
│    MQTT     │  │   Google    │  │    SMTP     │
│   Broker    │  │   OAuth2    │  │   Server    │
│  (Eclipse)  │  │             │  │  (Gmail)    │
└─────────────┘  └─────────────┘  └─────────────┘
```

## Layer Descriptions

### 1. Controller Layer
**Location**: `com.yaquodorg.yaquod.controller`

Handles HTTP requests and responses, orchestrating calls to services.

**Controllers**:
- **AuthenticationController**: User registration, login, verification, password reset
- **VehicleController**: Vehicle CRUD operations, location/status updates
- **TripController**: Trip requests, trip management, status tracking

**Responsibilities**:
- Request validation
- HTTP response formatting
- Exception handling
- Authorization checks (via `@PreAuthorize`)

### 2. Service Layer
**Location**: `com.yaquodorg.yaquod.service`

Contains business logic and coordinates between controllers and repositories.

**Services**:
- **AuthenticationService**: User authentication, JWT token management
- **VehicleService**: Vehicle management operations
- **TripService**: Trip lifecycle management
- **RequestService**: Trip request handling
- **JwtService**: JWT token generation and validation
- **MqttService**: MQTT messaging for real-time updates
- **MailSenderService**: Email notifications
- **UserService**: User management operations

**Responsibilities**:
- Business logic implementation
- Transaction management
- Inter-service communication
- Data transformation

### 3. Repository Layer
**Location**: `com.yaquodorg.yaquod.repository`

Data access layer using Spring Data JPA.

**Repositories**:
- **UserRepository**: User data persistence
- **VehicleRepository**: Vehicle data persistence
- **TripRepository**: Trip data persistence
- **RequestRepository**: Trip request data persistence

**Responsibilities**:
- Database operations (CRUD)
- Custom queries
- Spatial queries (PostGIS)

### 4. Entity Layer
**Location**: `com.yaquodorg.yaquod.entity`

JPA entities representing database tables.

**Entities**:
- **User**: User accounts with authentication details
- **Vehicle**: Vehicle information including location and status
- **Trip**: Trip records linking users, vehicles, and requests
- **Request**: Trip requests with start/end locations

**Enumerations**:
- **Role**: USER, ADMIN, CLIENT
- **VehicleStatus**: IDLE, IN_USE, MAINTENANCE
- **TripStatus**: PENDING, ACTIVE, COMPLETED, CANCELLED
- **RequestStatus**: PENDING, ACCEPTED, REJECTED, EXPIRED

### 5. Security Layer
**Location**: `com.yaquodorg.yaquod.config`, `com.yaquodorg.yaquod.filter`

Handles authentication and authorization.

**Components**:
- **SecurityConfig**: Main security configuration
- **JwtAuthenticationFilter**: JWT token validation filter
- **AuthenticationEntryPointFilter**: Unauthorized access handler
- **CustomAccessDeniedFilter**: Access denied handler
- **OAuth2LoginSuccessHandler**: Google OAuth2 integration

**Security Features**:
- JWT-based authentication
- Role-based access control (RBAC)
- OAuth2 integration (Google)
- CORS configuration
- Stateless session management

### 6. Configuration Layer
**Location**: `com.yaquodorg.yaquod.config`

Application configuration and bean definitions.

**Configurations**:
- **ApplicationConfig**: Core application beans
- **SecurityConfig**: Security settings
- **MqttConfig**: MQTT broker configuration

## Data Flow

### Authentication Flow
```
1. User Registration
   Client → AuthenticationController.signup()
         → AuthenticationService.signup()
         → UserRepository.save()
         → MailSenderService.sendVerificationEmail()

2. Email Verification
   Client → AuthenticationController.verifyCode()
         → AuthenticationService.verifyUser()
         → UserRepository.findByEmail()
         → UserRepository.save()

3. Login
   Client → AuthenticationController.login()
         → AuthenticationService.login()
         → UserRepository.findByEmail()
         → JwtService.generateToken()
         → Return JWT tokens
```

### Vehicle Management Flow
```
1. Create Vehicle (Admin)
   Client → VehicleController.createVehicle()
         → VehicleService.createVehicle()
         → VehicleRepository.save()

2. Update Vehicle Location (MQTT)
   IoT Device → MQTT Broker
             → MqttService.handleLocationUpdate()
             → VehicleService.updateLocation()
             → VehicleRepository.save()
```

### Trip Request Flow
```
1. Create Trip Request
   Client → TripController.createRequest()
         → RequestService.createRequest()
         → RequestRepository.save()
         → [Matching algorithm would run here]

2. Initialize Trip
   System → TripService.initTrip()
         → TripRepository.save()
         → VehicleService.updateStatus()
         → MqttService.publish() [notify vehicle]

3. Complete Trip
   Client → TripService.endTrip()
         → TripRepository.save()
         → VehicleService.updateStatus()
```

## Technology Stack

### Core Framework
- **Spring Boot 3.5.6**: Main application framework
- **Java 21**: Programming language
- **Maven**: Build and dependency management

### Data & Persistence
- **Spring Data JPA**: ORM and repository abstraction
- **PostgreSQL**: Production database
- **PostGIS**: Spatial database extension for geolocation
- **H2 Database**: In-memory database for testing
- **Hibernate Spatial**: Spatial data type support

### Security
- **Spring Security**: Authentication and authorization
- **JWT (JJWT 0.11.5)**: Token-based authentication
- **OAuth2 Client**: Google authentication integration
- **BCrypt**: Password hashing

### Messaging & Integration
- **Spring Integration MQTT**: MQTT protocol support
- **Eclipse Paho MQTT Client**: MQTT client library
- **Spring Mail**: Email service integration

### Spatial & Geolocation
- **JTS (LocationTech)**: Java Topology Suite for spatial operations
- **PostGIS**: PostgreSQL spatial extension

### Utilities
- **Lombok**: Boilerplate code reduction
- **Jakarta Validation**: Input validation
- **Caffeine**: In-memory caching

### Testing
- **JUnit 5**: Testing framework
- **Spring Security Test**: Security testing support
- **H2**: In-memory test database

### DevOps
- **Docker**: Containerization
- **Docker Compose**: Multi-container orchestration
- **Spring Actuator**: Application monitoring

## Design Patterns

### 1. Layered Architecture
Clear separation between Controller, Service, Repository layers.

### 2. Dependency Injection
Spring IoC container manages all dependencies.

### 3. Repository Pattern
Data access abstraction through Spring Data JPA repositories.

### 4. DTO Pattern
Data Transfer Objects for request/response handling.

### 5. Filter Chain Pattern
Security filters for authentication and authorization.

### 6. Builder Pattern
Entity creation using Lombok's `@Builder`.

### 7. Strategy Pattern
Different authentication strategies (JWT, OAuth2).

## Key Design Decisions

### 1. JWT for Stateless Authentication
- **Decision**: Use JWT tokens instead of session-based authentication
- **Rationale**: Enables horizontal scaling and microservices architecture
- **Trade-offs**: Token revocation complexity vs scalability benefits

### 2. PostGIS for Geospatial Data
- **Decision**: Use PostGIS extension for location data
- **Rationale**: Native spatial queries, efficient distance calculations
- **Trade-offs**: PostgreSQL dependency vs spatial query performance

### 3. MQTT for Real-time Updates
- **Decision**: MQTT protocol for vehicle location/status updates
- **Rationale**: Lightweight, pub-sub model, IoT-friendly
- **Trade-offs**: Additional broker dependency vs real-time capabilities

### 4. Role-Based Access Control
- **Decision**: Implement RBAC with Spring Security
- **Rationale**: Fine-grained access control, industry standard
- **Trade-offs**: Configuration complexity vs security flexibility

### 5. Multi-Profile Configuration
- **Decision**: Separate profiles for dev, test, docker environments
- **Rationale**: Environment-specific configurations
- **Trade-offs**: Configuration duplication vs deployment flexibility

## Scalability Considerations

### Horizontal Scaling
- Stateless JWT authentication enables load balancing
- No server-side session state
- Database connection pooling

### Performance Optimization
- Caffeine caching for frequently accessed data
- JPA lazy loading for relationships
- Spatial indexing in PostGIS

### Future Improvements
- Message queue for asynchronous processing
- Redis for distributed caching
- API rate limiting per user
- Database read replicas
- Microservices decomposition

## Security Architecture

### Authentication Layers
1. **JWT Filter**: Validates JWT tokens from Authorization header
2. **OAuth2 Integration**: Google authentication support
3. **Method Security**: `@PreAuthorize` annotations on endpoints
4. **Exception Handling**: Custom error responses for auth failures

### Authorization Model
- **PUBLIC**: `/api/auth/**` endpoints
- **CLIENT**: Regular users - trip requests, profile management
- **ADMIN**: Administrators - vehicle management, user management

### Security Best Practices
- Password hashing with BCrypt
- JWT token expiration
- CORS configuration
- HTTPS enforcement (deployment)
- Input validation
- SQL injection prevention (JPA)

## Monitoring & Observability

### Spring Actuator
- Health checks
- Application metrics
- Environment information

### Logging
- SLF4J with Logback
- Structured logging with `@Slf4j`
- Log levels per environment

## Deployment Architecture

### Docker Deployment
```
┌─────────────────────────────────────┐
│     Docker Compose Stack            │
│                                     │
│  ┌──────────────────────────────┐  │
│  │   Spring Boot Container      │  │
│  │   Port: 8000                 │  │
│  └──────────┬───────────────────┘  │
│             │                       │
│  ┌──────────▼───────────────────┐  │
│  │   PostGIS Container          │  │
│  │   Port: 5432                 │  │
│  └──────────────────────────────┘  │
│                                     │
│  External: MQTT Broker (1883)      │
└─────────────────────────────────────┘
```

### Environment Configuration
- **Development**: Local PostgreSQL, local MQTT
- **Docker**: Containerized PostgreSQL, host MQTT
- **Production**: Managed PostgreSQL, cloud MQTT broker

## Integration Points

### External Services
1. **SMTP Server**: Email verification and notifications
2. **Google OAuth2**: Social login integration
3. **MQTT Broker**: Real-time vehicle communication
4. **PostgreSQL**: Primary data store

### API Contracts
- RESTful APIs with JSON
- MQTT messages with JSON payloads
- Standard HTTP status codes
- Consistent error response format

## Error Handling Strategy

### Global Exception Handler
- **GlobalExceptionHandler**: Centralized exception handling
- Consistent error response format
- Proper HTTP status codes
- Detailed error messages

### Exception Types
- Validation errors → 400 Bad Request
- Authentication errors → 401 Unauthorized
- Authorization errors → 403 Forbidden
- Not found errors → 404 Not Found
- Server errors → 500 Internal Server Error


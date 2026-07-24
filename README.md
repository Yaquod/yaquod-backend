# Yaquod Backend

The backend service for the Yaquod ride-hailing/autonomous robo-taxi platform.

![System Design](images/system_design.svg)

## Table of Contents

- [Overview](#overview)
- [Architecture](#architecture)
- [Technologies](#technologies)
- [Features](#features)
  - [Authentication and Account Management](#authentication-and-account-management)
  - [Trip Management](#trip-management)
  - [Real-Time Communication](#real-time-communication)
  - [Payment Integration](#payment-integration)
  - [Admin Dashboard](#admin-dashboard)
  - [Push Notifications](#push-notifications)
- [Getting Started](#getting-started)
- [API Overview](#api-overview)
- [Project Structure](#project-structure)
- [Security](#security)
- [Deployment](#deployment)
- [License](#license)

## Overview

This is the backend service for the Yaquod ride-hailing/autonomous robo-taxi platform built with Spring Boot. It connects passengers directly with vehicles through a complete lifecycle of trip requests, vehicle matching, real-time tracking, payment processing, and post-trip rating.

The system supports multiple authentication methods including email and password, Google OAuth, and API-key-based vehicle authentication.

The system is designed for production deployment with PostgreSQL and PostGIS for spatial data handling, Redis for caching and timeout management, MQTT for real-time vehicle communication, and Firebase Cloud Messaging for push notifications to mobile clients. Payment processing is handled through Paymob with support for card tokenization, saved cards, and recurring charges.

## Architecture

The system follows a layered architecture with controllers exposing REST endpoints, services implementing business logic, repositories handling data access, and JPA entities modeling the domain. Spring Security secures all endpoints with JWT-based authentication and role-based authorization supporting three roles: CLIENT, ADMIN, and VEHICLE.

Communication between the server and vehicles occurs over MQTT, while clients interact with the server via standard REST API calls and Server-Sent Events for live location streaming. The admin dashboard aggregates data across all services to provide KPIs and operational visibility into users, vehicles, trips, requests, payments, and ratings.

The system design diagram above illustrates the major components and their interactions.

## Technologies

| Technology | Purpose |
|---|---|
| Spring Boot 3.5.6 / Java 21 | Application framework and runtime |
| PostgreSQL 16 + PostGIS 3.4 | Primary database with geospatial extensions |
| Hibernate Spatial / JTS | Spatial data mapping and operations |
| Redis 7 | Caching, session management, timeout events |
| MQTT (Eclipse Paho) | Real-time vehicle communication |
| Paymob API | Payment processing and card tokenization |
| Firebase Admin SDK | Cloud push notifications |
| JWT (jjwt 0.11.5) | Token-based authentication |
| Spring Security + OAuth2 | Authentication and authorization |
| SpringDoc OpenAPI 2.7 | API documentation |
| H2 / H2GIS | In-memory database for testing |

## Features

### Authentication and Account Management

Users can register with an email and password, after which a six-digit OTP is sent to their email for verification. The OTP must be submitted to activate the account before the user can log in. Password reset follows the same OTP-based verification flow. Google OAuth is supported through two paths: a browser-based OAuth2 login flow for web clients, and a Google ID token verification path for native mobile applications. Both paths create a new user if one does not already exist with the given email.

Vehicles authenticate using a unique API key and a hashed API secret generated during vehicle creation. These credentials are verified by a custom `VehicleAuthenticationProvider` that looks up the vehicle by API key and validates the secret using BCrypt. Upon successful authentication, the vehicle receives its own set of JWT access and refresh tokens scoped to the VEHICLE role.

JWT tokens are issued with configurable expiration times: seven days for access tokens and thirty days for refresh tokens, both for users and vehicles. The token type is determined by inspecting the roles claim, which allows the authentication filter to construct the appropriate `Authentication` object for each request.

### Trip Management

The trip lifecycle begins when a client creates a request specifying their pickup and destination locations. The system matches the request to the nearest available vehicle using PostGIS spatial queries with `ST_Distance` and `ST_DWithin`. The request can be accepted by a vehicle, declined, or left to time out. If the request times out without any vehicle responding, the system cancels it automatically.

Once a request is accepted, a trip is created and proceeds through a series of defined states: `INITIATED`, `VEHICLE_ON_WAY`, `VEHICLE_CLOSE`, `ARRIVED_AT_PICKUP`, `PASSENGER_ONBOARD`, `IN_PROGRESS`, `ARRIVED_AT_DESTINATION`, `COMPLETED`. The system also handles exceptional states including `PASSENGER_NO_SHOW`, `CANCELLED_BY_PASSENGER`, `CANCELLED_BY_SYSTEM`, `EMERGENCY`, `INCIDENT`, `VEHICLE_ISSUE`, and `REFUNDED`.

Each trip links a client, a vehicle, a request, and optionally a payment and a rating. After a trip is completed, the client can submit a rating from zero to five with an optional comment. Only one rating is permitted per trip, and the rating must belong to the user who took the trip.

### Real-Time Communication

The server communicates with vehicles over MQTT using an Eclipse Paho client integrated through Spring Integration. The MQTT gateway subscribes to eight topics covering location updates, status changes, ETA confirmations, vehicle arrivals, trip status transitions, and location streaming requests. Incoming MQTT messages are dispatched to the appropriate service methods based on topic and payload content.

For client-facing real-time updates, the server uses Server-Sent Events to stream vehicle locations during an active trip. A `ConcurrentHashMap` in the trip service maintains active subscriptions, and location broadcasts from vehicles are forwarded to the subscribed clients through their SSE connections.

Redis key expiry events are leveraged for timeout management. When a trip request is created, a Redis key is set with a configurable TTL. If the key expires before the vehicle responds, a `RedisExpiryListener` handles the event by cancelling the request and updating the relevant entities. A separate TTL handles the ETA response window where the passenger must confirm or cancel after receiving an estimated time and fare.

### Payment Integration

Payments are processed through Paymob, an Egyptian payment gateway. The integration supports card tokenization checkout for saving card details securely, one-time payments for single transactions, and both Customer Initiated Transactions (CIT) and Merchant Initiated Transactions (MIT) for charging saved cards.

When a user saves a card, the system creates a card tokenization checkout URL through Paymob's API. The card details are tokenized by Paymob and a token is stored locally with the masked PAN, card subtype, and cardholder name. Subsequent payments can use the saved card token directly without requiring the user to re-enter card details.

Paymob webhook callbacks are processed to update payment statuses from PENDING to PAID or FAILED. Idempotency is enforced using Redis to prevent duplicate webhook processing. The system tracks payments by order ID and transaction ID, and supports refunds through the REFUNDED status.

### Admin Dashboard

The admin dashboard aggregates statistics across the entire platform. The endpoint returns counts of users (broken down by role), vehicles (by status), trips (by status), requests (by status), payments (by status), total revenue, and average ratings. The dashboard service collects this data by calling the respective service methods for each metric.

Administrators have full CRUD access to users, vehicles, trips, requests, payments, and ratings. They can search users by email, update user roles, verify user accounts, update vehicle statuses, and delete any entity across the system.

### Push Notifications

Firebase Cloud Messaging is used to send push notifications to mobile clients. Each client's FCM token is stored on the user entity and updated on each login. The `FirebaseMessagingService` sends text-only notifications by token, which are triggered during trip lifecycle events to notify passengers of status changes.

## Getting Started

### Prerequisites

- Java 21
- Maven 3.9
- Docker and Docker Compose (for PostgreSQL with PostGIS, Redis, and an MQTT broker)
- An MQTT broker (such as Mosquitto) for vehicle communication

### Configuration

The application reads its configuration from `application.yml` and supports two additional profiles: `docker` and `test`. The Docker profile uses environment variable placeholders for all service connections, while the test profile uses an H2 in-memory database with H2GIS for spatial queries.

Key configuration properties include database credentials, Redis host and port, MQTT broker URL and topics, JWT secret and expiration times, Paymob API credentials, Google OAuth client IDs, and Firebase service account path.

### Running with Docker Compose

The repository includes a `docker-compose.yml` that starts three services: a PostGIS container, a Redis container with keyspace notifications enabled, and the Spring Boot application. The application is built from the Dockerfile using a multi-stage build that compiles the JAR with Maven in the first stage and runs it with a JRE in the second stage.

```bash
docker-compose up --build
```

### Running Locally

```bash
mvn spring-boot:run
```

For development, the test profile can be used to avoid external dependencies:

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=test
```

## API Overview

The API is organized into six main endpoint groups under the `/api` prefix:

| Group | Base Path | Access | Purpose |
|---|---|---|---|
| Authentication | `/api/auth` | Public (some endpoints) | Registration, login, OTP verification, token refresh, password reset |
| Vehicles | `/api/vehicles` | ADMIN / VEHICLE | Vehicle CRUD, credentials management, status and location updates |
| Trips | `/api/trips` | CLIENT / ADMIN / VEHICLE | Trip request lifecycle, SSE location streaming |
| Payments | `/api/payments` | Authenticated | Card management, payment processing, webhook handling |
| Ratings | `/api/ratings` | CLIENT / ADMIN | Trip ratings and reviews |
| Admins | `/api/admins` | ADMIN only | Dashboard statistics, system-wide management |

Full API documentation is available through Swagger UI when the application is running at `/swagger-ui.html`.

## Project Structure

```
com.yaquodorg.yaquod
├── YaquodApplication.java
├── config/          # Spring Security, Redis, MQTT, Swagger, data initializer
├── controller/      # REST controllers for auth, vehicles, trips, payments, ratings, admin
├── dtos/            # Data transfer objects organized by domain (auth, payment, trip, etc.)
├── entity/          # JPA entities: User, Vehicle, Trip, Request, Payment, Rating, SavedCard
├── exception/       # Custom exceptions and global exception handler
├── filter/          # JWT authentication filter, access denied, authentication entry point
├── repository/      # Spring Data JPA repositories with custom spatial queries
├── response/        # API response wrappers: ApiResponse, LoginResponse, RatingResponse
├── security/        # Vehicle authentication provider and token
├── service/         # Business logic organized by domain
│   ├── admin/       # Dashboard statistics
│   ├── auth/        # Authentication orchestration
│   ├── google/      # Google ID token verification
│   ├── jwt/         # JWT generation and validation
│   ├── mail/        # Email sending via SMTP
│   ├── messaging/   # Firebase Cloud Messaging
│   ├── mqtt/        # MQTT message handling and gateway
│   ├── payment/     # Paymob integration
│   ├── rating/      # Trip ratings
│   ├── redis/       # Redis key-value operations
│   ├── request/     # Trip request management
│   ├── trip/        # Trip lifecycle and SSE streaming
│   ├── user/        # User management
│   └── vehicle/     # Vehicle management and spatial queries
└── utils/           # Global exception handler, Redis expiry listener, VIN validation
```

## Security

Authentication supports three distinct flows. Users can authenticate with email and password through Spring Security's `DaoAuthenticationProvider` with BCrypt password hashing. Native mobile applications can authenticate with a Google ID token that is verified using Google's `IdTokenVerifier` with configurable audience for web, Android, and iOS clients. Vehicles authenticate using an API key and secret validated by a custom `VehicleAuthenticationProvider`.

All authenticated requests must include a JWT in the `Authorization` header as a Bearer token. A `JwtAuthenticationFilter` extracts and validates the token on every request, distinguishing between user tokens and vehicle tokens by inspecting the roles claim. User tokens require the account to have email verification enabled. Vehicle tokens carry the vehicle ID and the admin ID who created the vehicle.

Authorization is enforced at the controller level using `@PreAuthorize` annotations. The ADMIN role has access to `/api/admins/**` and most management endpoints. The CLIENT role can create trips, manage their own profile and payment methods, and submit ratings. The VEHICLE role can update its status and location and manage its assigned trips. Public endpoints include authentication routes and the Paymob webhook callback.

## Deployment

The application is containerized with a multi-stage Docker build. The first stage compiles the application with Maven and the second stage runs the JAR with an Eclipse Temurin JRE 21 runtime. The container exposes port 8000.

The Docker Compose setup includes three services: PostGIS for spatial data persistence, Redis for caching and timeout management, and the Spring Boot application. The application container depends on the PostGIS service being healthy and the Redis service being available. Environment variables for production deployment are configured through the Docker profile with placeholders for sensitive values such as database credentials, OAuth client secrets, MQTT broker URLs, and Paymob API keys.

## License

This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for details.


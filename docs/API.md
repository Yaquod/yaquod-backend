# API Documentation

## Base URL
- **Development**: `http://localhost:8000`
- **Docker**: `http://localhost:8000`
- **Production**: `https://api.yaquod.com` (configure as needed)

## Authentication

Most endpoints require JWT authentication. Include the token in the Authorization header:

```
Authorization: Bearer <your_jwt_token>
```

## Response Format

All API responses follow a consistent structure:

### Success Response
```json
{
  "success": true,
  "message": "Operation successful",
  "data": { ... }
}
```

### Error Response
```json
{
  "success": false,
  "message": "Error description",
  "data": null
}
```

---

## Authentication Endpoints

### 1. Admin Signup
**POST** `/api/auth/admin/signup`

Register a new admin user.

**Request Body:**
```json
{
  "email": "admin@example.com",
  "password": "SecurePass123!",
  "firstName": "John",
  "lastName": "Doe",
  "phoneNumber": "+1234567890",
  "dob": "1990-01-15"
}
```

**Response:** `201 Created`
```json
{
  "success": true,
  "message": "Admin registered successfully",
  "data": {
    "id": 1,
    "email": "admin@example.com",
    "firstName": "John",
    "lastName": "Doe",
    "role": "ADMIN",
    "isEmailVerified": false,
    "join_date": "2025-12-13T10:00:00Z"
  }
}
```

---

### 2. Client Signup
**POST** `/api/auth/client/signup`

Register a new client user.

**Request Body:**
```json
{
  "email": "client@example.com",
  "password": "SecurePass123!",
  "firstName": "Jane",
  "lastName": "Smith",
  "phoneNumber": "+1234567890",
  "dob": "1995-05-20"
}
```

**Response:** `201 Created`
```json
{
  "success": true,
  "message": "Client registered successfully",
  "data": {
    "id": 2,
    "email": "client@example.com",
    "firstName": "Jane",
    "lastName": "Smith",
    "role": "CLIENT",
    "isEmailVerified": false,
    "join_date": "2025-12-13T10:05:00Z"
  }
}
```

---

### 3. Verify Email Code
**POST** `/api/auth/verify-code`

Verify email with the OTP code sent to user's email.

**Request Body:**
```json
{
  "email": "client@example.com",
  "code": "123456"
}
```

**Response:** `200 OK`
```json
{
  "success": true,
  "message": "Account Verified Successfully!",
  "data": {
    "message": "Account Verified Successfully!"
  }
}
```

---

### 4. Regenerate OTP Code
**POST** `/api/auth/regenerate-code`

Request a new OTP code to be sent via email.

**Request Body:**
```json
{
  "email": "client@example.com"
}
```

**Response:** `200 OK`
```json
{
  "success": true,
  "message": "OTP regenerated successfully",
  "data": {
    "message": "OTP regenerated successfully. Check your email for the new OTP."
  }
}
```

---

### 5. Login
**POST** `/api/auth/login`

Authenticate user and receive JWT tokens.

**Request Body:**
```json
{
  "email": "client@example.com",
  "password": "SecurePass123!"
}
```

**Response:** `200 OK`
```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "expiresIn": 604800000
  }
}
```

---

### 6. Refresh Token
**GET** `/api/auth/token-refresh`

Refresh the access token using a refresh token.

**Headers:**
```
Authorization: Bearer <refresh_token>
```

**Response:** `200 OK`
```json
{
  "success": true,
  "message": "Token refreshed",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "expiresIn": 604800000
  }
}
```

---

### 7. Google OAuth2 Login
**POST** `/api/auth/google-login`

Authenticate using Google OAuth2.

**Request Body:**
```json
{
  "idToken": "google_id_token_here"
}
```

**Response:** `200 OK`
```json
{
  "success": true,
  "message": "Google login successful",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "expiresIn": 604800000
  }
}
```

---

### 8. Reset Password
**POST** `/api/auth/reset-password`

Reset user password using verification code.

**Request Body:**
```json
{
  "email": "client@example.com",
  "code": "123456",
  "newPassword": "NewSecurePass123!"
}
```

**Response:** `200 OK`
```json
{
  "success": true,
  "message": "Password reset successful",
  "data": null
}
```

---

## Vehicle Endpoints

All vehicle endpoints require `ADMIN` role.

### 1. Create Vehicle
**POST** `/api/vehicles`

**Auth Required:** Admin

**Request Body:**
```json
{
  "vinNumber": "1HGBH41JXMN109186",
  "plateNo": "ABC-1234",
  "color": "Blue",
  "carCompany": "Toyota",
  "model": "Camry",
  "seats": 5
}
```

**Response:** `201 Created`
```json
{
  "success": true,
  "message": "Vehicle created successfully",
  "data": {
    "id": 1,
    "vinNumber": "1HGBH41JXMN109186",
    "plateNo": "ABC-1234",
    "color": "Blue",
    "carCompany": "Toyota",
    "model": "Camry",
    "seats": 5,
    "status": "IDLE",
    "lastUpdatedStatusAt": null,
    "lastUpdatedLat": 0.0,
    "lastUpdatedLong": 0.0,
    "lastUpdatedLocationAt": null
  }
}
```

**Validation:**
- VIN number must be valid (17 characters)
- All fields are required

---

### 2. Get All Vehicles
**GET** `/api/vehicles`

**Auth Required:** Admin

**Response:** `200 OK`
```json
{
  "success": true,
  "message": "Vehicles retrieved successfully",
  "data": [
    {
      "id": 1,
      "vinNumber": "1HGBH41JXMN109186",
      "plateNo": "ABC-1234",
      "color": "Blue",
      "carCompany": "Toyota",
      "model": "Camry",
      "seats": 5,
      "status": "IDLE",
      "lastUpdatedLat": 40.7128,
      "lastUpdatedLong": -74.0060,
      "lastUpdatedLocationAt": "2025-12-13T10:30:00Z"
    }
  ]
}
```

---

### 3. Get Vehicle by ID
**GET** `/api/vehicles/id/{vehicleId}`

**Auth Required:** Admin

**Response:** `200 OK`
```json
{
  "success": true,
  "message": "Vehicle retrieved successfully",
  "data": {
    "id": 1,
    "vinNumber": "1HGBH41JXMN109186",
    "plateNo": "ABC-1234",
    "color": "Blue",
    "carCompany": "Toyota",
    "model": "Camry",
    "seats": 5,
    "status": "IDLE",
    "trips": []
  }
}
```

---

### 4. Get Vehicle by VIN
**GET** `/api/vehicles/vin/{vinNumber}`

**Auth Required:** Admin

**Response:** `200 OK`
```json
{
  "success": true,
  "message": "Vehicle retrieved successfully",
  "data": {
    "id": 1,
    "vinNumber": "1HGBH41JXMN109186",
    "plateNo": "ABC-1234",
    "carCompany": "Toyota",
    "model": "Camry",
    "seats": 5,
    "status": "IDLE"
  }
}
```

---

### 5. Update Vehicle
**PATCH** `/api/vehicles`

**Auth Required:** Admin

**Request Body:**
```json
{
  "vinNumber": "1HGBH41JXMN109186",
  "plateNo": "XYZ-5678",
  "color": "Red",
  "carCompany": "Toyota",
  "model": "Camry",
  "seats": 5
}
```

**Response:** `200 OK`
```json
{
  "success": true,
  "message": "Vehicle updated successfully",
  "data": {
    "id": 1,
    "vinNumber": "1HGBH41JXMN109186",
    "plateNo": "XYZ-5678",
    "color": "Red",
    "carCompany": "Toyota",
    "model": "Camry",
    "seats": 5,
    "status": "IDLE"
  }
}
```

---

### 6. Delete Vehicle
**DELETE** `/api/vehicles/id/{vehicleId}`

**Auth Required:** Admin

**Response:** `200 OK`
```json
{
  "success": true,
  "message": "Vehicle deleted successfully",
  "data": {
    "message": "Vehicle deleted successfully!"
  }
}
```

---

### 7. Delete Vehicle by VIN
**DELETE** `/api/vehicles/vin/{vinNumber}`

**Auth Required:** Admin

**Response:** `200 OK`
```json
{
  "success": true,
  "message": "Vehicle deleted successfully",
  "data": {
    "message": "Vehicle deleted successfully!"
  }
}
```

---

### 8. Update Vehicle Location via MQTT
**PATCH** `/api/vehicles/vin/{vinNumber}/location-update`

**Auth Required:** Admin

Triggers an MQTT message to request location update from vehicle.

**Request Body:**
```json
{
  "latitude": 40.7128,
  "longitude": -74.0060
}
```

**Response:** `200 OK`
```json
{
  "success": true,
  "message": "Location update message sent",
  "data": {
    "message": "Location update message sent successfully"
  }
}
```

---

### 9. Update Vehicle Status via MQTT
**PATCH** `/api/vehicles/vin/{vinNumber}/status-update`

**Auth Required:** Admin

Triggers an MQTT message to update vehicle status.

**Request Body:**
```json
{
  "status": "IN_USE"
}
```

**Valid Status Values:**
- `IDLE`
- `IN_USE`
- `MAINTENANCE`

**Response:** `200 OK`
```json
{
  "success": true,
  "message": "Status update message sent",
  "data": {
    "message": "Status update message sent successfully"
  }
}
```

---

### 10. Get Vehicle ETA and Status
**GET** `/api/vehicles/vin/{vinNumber}/eta-status`

**Auth Required:** Admin

**Response:** `200 OK`
```json
{
  "success": true,
  "message": "Vehicle ETA retrieved",
  "data": {
    "vinNumber": "1HGBH41JXMN109186",
    "status": "IN_USE",
    "estimatedArrival": "2025-12-13T11:30:00Z",
    "currentLat": 40.7128,
    "currentLong": -74.0060
  }
}
```

---

## Trip Endpoints

### 1. Create Trip Request
**POST** `/api/trips/request`

**Auth Required:** Authenticated User

Create a new trip request from current location to destination.

**Request Body:**
```json
{
  "startLat": 40.7128,
  "startLong": -74.0060,
  "endLat": 40.7589,
  "endLong": -73.9851
}
```

**Response:** `200 OK`
```json
{
  "success": true,
  "message": "Trip request created",
  "data": {
    "id": 1,
    "status": "PENDING",
    "createdAt": "2025-12-13T10:00:00Z",
    "user": {
      "id": 2,
      "email": "client@example.com",
      "firstName": "Jane",
      "lastName": "Smith"
    }
  }
}
```

---

### 2. Get Request Status
**GET** `/api/trips/request/status/{requestId}`

**Auth Required:** Authenticated User

**Response:** `200 OK`
```json
{
  "success": true,
  "message": "Request status retrieved",
  "data": {
    "id": 1,
    "status": "ACCEPTED",
    "createdAt": "2025-12-13T10:00:00Z",
    "acceptedAt": "2025-12-13T10:02:00Z"
  }
}
```

**Request Status Values:**
- `PENDING`: Waiting for assignment
- `ACCEPTED`: Vehicle assigned
- `REJECTED`: No vehicle available
- `EXPIRED`: Request timeout

---

### 3. Get Trip by Request ID
**GET** `/api/trips/by-request/{requestId}`

**Auth Required:** Authenticated User

**Response:** `200 OK`
```json
{
  "success": true,
  "message": "Trip retrieved",
  "data": {
    "id": 1,
    "status": "ACTIVE",
    "startedAt": "2025-12-13T10:05:00Z",
    "endedAt": null,
    "updatedAt": "2025-12-13T10:10:00Z",
    "request": {
      "id": 1,
      "status": "ACCEPTED"
    }
  }
}
```

**Trip Status Values:**
- `PENDING`: Trip created, not started
- `ACTIVE`: Trip in progress
- `COMPLETED`: Trip finished
- `CANCELLED`: Trip cancelled

---

### 4. Get Trip by ID
**GET** `/api/trips/{tripId}`

**Auth Required:** Authenticated User

**Response:** `200 OK`
```json
{
  "success": true,
  "message": "Trip retrieved",
  "data": {
    "id": 1,
    "status": "COMPLETED",
    "startedAt": "2025-12-13T10:05:00Z",
    "endedAt": "2025-12-13T10:45:00Z",
    "updatedAt": "2025-12-13T10:45:00Z"
  }
}
```

---

### 5. Initialize Trip (Admin)
**POST** `/api/trips/init`

**Auth Required:** Admin

Initialize a trip by assigning a vehicle to a request.

**Request Body:**
```json
{
  "requestId": 1,
  "vehicleId": 1
}
```

**Response:** `200 OK`
```json
{
  "success": true,
  "message": "Trip initialized",
  "data": {
    "id": 1,
    "status": "PENDING",
    "startedAt": null,
    "request": {
      "id": 1,
      "status": "ACCEPTED"
    }
  }
}
```

---

### 6. Get All Trips (Admin)
**GET** `/api/trips`

**Auth Required:** Admin

**Response:** `200 OK`
```json
{
  "success": true,
  "message": "Trips retrieved",
  "data": [
    {
      "id": 1,
      "status": "COMPLETED",
      "startedAt": "2025-12-13T10:05:00Z",
      "endedAt": "2025-12-13T10:45:00Z"
    },
    {
      "id": 2,
      "status": "ACTIVE",
      "startedAt": "2025-12-13T11:00:00Z",
      "endedAt": null
    }
  ]
}
```

---

### 7. Delete Trip (Admin)
**DELETE** `/api/trips/{tripId}`

**Auth Required:** Admin

**Response:** `200 OK`
```json
{
  "success": true,
  "message": "Trip deleted successfully",
  "data": {
    "message": "Trip deleted successfully"
  }
}
```

---

## Error Codes

### HTTP Status Codes

| Code | Description | Usage |
|------|-------------|-------|
| 200 | OK | Successful GET, PATCH, DELETE |
| 201 | Created | Successful POST (resource created) |
| 400 | Bad Request | Validation error, invalid input |
| 401 | Unauthorized | Missing or invalid JWT token |
| 403 | Forbidden | Insufficient permissions |
| 404 | Not Found | Resource doesn't exist |
| 500 | Internal Server Error | Server-side error |

### Common Error Responses

**Validation Error:**
```json
{
  "success": false,
  "message": "Validation failed: email must be valid",
  "data": null
}
```

**Authentication Error:**
```json
{
  "success": false,
  "message": "Invalid or expired token",
  "data": null
}
```

**Authorization Error:**
```json
{
  "success": false,
  "message": "Access denied: insufficient permissions",
  "data": null
}
```

**Not Found Error:**
```json
{
  "success": false,
  "message": "Vehicle not found",
  "data": null
}
```

---

## Rate Limiting

Currently not implemented. Future versions will include:
- 100 requests per minute per IP
- 1000 requests per hour per authenticated user
- Separate limits for admin users

---

## Pagination

Currently not implemented. Future versions will support:
```
GET /api/vehicles?page=0&size=20&sort=id,desc
```

---

## MQTT Topics

### Subscribed Topics
- `topic/order_update_location` - Vehicle location updates
- `topic/update_location` - General location updates

### Published Topics
- `topic/update_location/order` - Request vehicle location update
- `topic/update_status/order` - Request vehicle status update

### Message Format

**Location Update:**
```json
{
  "vinNumber": "1HGBH41JXMN109186",
  "latitude": 40.7128,
  "longitude": -74.0060,
  "timestamp": "2025-12-13T10:30:00Z"
}
```

**Status Update:**
```json
{
  "vinNumber": "1HGBH41JXMN109186",
  "status": "IN_USE",
  "timestamp": "2025-12-13T10:30:00Z"
}
```

---

## Testing

### Using cURL

**Login Example:**
```bash
curl -X POST http://localhost:8000/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "client@example.com",
    "password": "SecurePass123!"
  }'
```

**Get Vehicles with Auth:**
```bash
curl -X GET http://localhost:8000/api/vehicles \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### Using Postman

1. Import the API collection (if available)
2. Set environment variable `baseUrl` to `http://localhost:8000`
3. Set environment variable `token` after login
4. Use `{{baseUrl}}` and `{{token}}` in requests

---

## Versioning

Current API Version: **v1** (implicit)

Future versions will use URL versioning:
- `/api/v1/vehicles`
- `/api/v2/vehicles`

---




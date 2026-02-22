package com.yaquodorg.yaquod.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yaquodorg.yaquod.dtos.TripRequestDto;
import com.yaquodorg.yaquod.entity.*;
import com.yaquodorg.yaquod.repository.RequestRepository;
import com.yaquodorg.yaquod.repository.TripRepository;
import com.yaquodorg.yaquod.repository.UserRepository;
import com.yaquodorg.yaquod.repository.VehicleRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.sql.Timestamp;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for Trip endpoints
 * Tests full Spring context including security, JWT, and database
 * Uses real database (H2 or Testcontainers)
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("Trip Controller Integration Tests")
class TripControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private VehicleRepository vehicleRepository;

    @Autowired
    private RequestRepository requestRepository;

    @Autowired
    private TripRepository tripRepository;

    private GeometryFactory geometryFactory;
    private User testUser;
    private Vehicle testVehicle;
    private Request testRequest;
    private Trip testTrip;
    private TripRequestDto tripRequestDto;

    @BeforeEach
    void setUp() {
        geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);

        // Clean up
        tripRepository.deleteAll();
        requestRepository.deleteAll();
        vehicleRepository.deleteAll();
        userRepository.deleteAll();

        Date now = new Date(0);

        // Create test user
        testUser = User.builder()
                .email("test@example.com")
                .passwordHash("password123")
                .firstName("John")
                .lastName("Doe")
                .phoneNumber("+1234567890")
                .join_date(new Timestamp(now.getTime()))
                .role(Role.ADMIN)
                .code(111111)
                .isEmailVerified(true)
                .build();
        testUser = userRepository.save(testUser);

        // Create test vehicle with location
        Point vehicleLocation = geometryFactory.createPoint(new Coordinate(31.0, 30.0));
        testVehicle = Vehicle.builder()
                .vinNumber("VIN123456789")
                .model("Camry")
                .status(VehicleStatus.IDLE)
                .lastUpdatedLocation(vehicleLocation)
                .build();
        testVehicle = vehicleRepository.save(testVehicle);

        // Create test request
        Point startPoint = geometryFactory.createPoint(new Coordinate(31.0, 30.0));
        Point endPoint = geometryFactory.createPoint(new Coordinate(31.5, 30.5));
        testRequest = Request.builder()
                .user(testUser)
                .startLocation(startPoint)
                .destinationLocation(endPoint)
                .status(RequestStatus.PENDING)
                .createdAt(new Timestamp(System.currentTimeMillis()))
                .build();
        testRequest = requestRepository.save(testRequest);

        // Create test trip
        testTrip = Trip.builder()
                .request(testRequest)
                .vehicle(testVehicle)
                .user(testUser)
                .status(TripStatus.INITIATED)
                .startedAt(new Timestamp(System.currentTimeMillis()))
                .build();
        testTrip = tripRepository.save(testTrip);

        // Create trip request DTO
        tripRequestDto = TripRequestDto.builder()
                .startLong(31.0)
                .startLat(30.0)
                .endLong(31.5)
                .endLat(30.5)
                .build();
    }

    private Request createRequest(User user) {
        Request request = Request.builder()
                .user(user)
                .status(RequestStatus.PENDING)
                .createdAt(new Timestamp(System.currentTimeMillis()))
                .build();
        entityManager.persist(request);
        return request;
    }

    // TODO: Should be uncommented after handled correctly in the refactoring phase
    // @Test
    // @DisplayName("shouldCreateRequestWithAuthentication")
    // @WithMockUser(username = "test@example.com")
    // void shouldCreateRequestWithAuthentication() throws Exception {
    // // Act & Assert
    // mockMvc.perform(post("/api/trips/request")
    // .contentType(MediaType.APPLICATION_JSON)
    // .content(objectMapper.writeValueAsString(tripRequestDto)))
    // .andExpect(status().isOk())
    // .andExpect(jsonPath("$.success").value(true))
    // .andExpect(jsonPath("$.data.status").value("PENDING"))
    // .andExpect(jsonPath("$.data.user").exists());
    // }

    @Test
    @DisplayName("shouldGetRequestStatusById")
    @WithMockUser
    void shouldGetRequestStatusById() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/trips/request/status/" + testRequest.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(testRequest.getId()))
                .andExpect(jsonPath("$.data.status").value("PENDING"));
    }

    @Test
    @DisplayName("shouldReturn400WhenRequestNotFound")
    @WithMockUser
    void shouldReturn400WhenRequestNotFound() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/trips/request/status/999999"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(containsString("Failed to check Request status")));
    }

    @Test
    @DisplayName("shouldGetTripByRequestId")
    @WithMockUser
    void shouldGetTripByRequestId() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/trips/by-request/" + testRequest.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(testTrip.getId()))
                .andExpect(jsonPath("$.data.status").value("INITIATED"));
    }

    @Test
    @DisplayName("shouldReturn400WhenTripNotFoundByRequestId")
    @WithMockUser
    void shouldReturn400WhenTripNotFoundByRequestId() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/trips/by-request/999999"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(containsString("Failed to get Trip by requestId")));
    }

    @Test
    @DisplayName("shouldGetTripById")
    @WithMockUser
    void shouldGetTripById() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/trips/" + testTrip.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(testTrip.getId()))
                .andExpect(jsonPath("$.data.status").value("INITIATED"));
    }

    @Test
    @DisplayName("shouldReturn400WhenTripNotFoundById")
    @WithMockUser
    void shouldReturn400WhenTripNotFoundById() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/trips/999999"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(containsString("Failed to get Trip by id")));
    }

    @Test
    @DisplayName("shouldDeleteTripByIdWithAdminRole")
    @WithMockUser(roles = "ADMIN")
    void shouldDeleteTripByIdWithAdminRole() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/api/trips/" + testTrip.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.message").value("Trip deleted successfully"));

        // Verify deletion
        mockMvc.perform(get("/api/trips/" + testTrip.getId()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("shouldReturn403WhenDeletingWithoutAdminRole")
    @WithMockUser(roles = "USER")
    void shouldReturn403WhenDeletingWithoutAdminRole() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/api/trips/" + testTrip.getId()))
                .andExpect(status().isForbidden());

        // Verify trip still exists
        mockMvc.perform(get("/api/trips/" + testTrip.getId()))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("shouldGetAllTrips")
    @WithMockUser
    void shouldGetAllTrips() throws Exception {
        // Arrange - Create additional trip
        Trip trip2 = Trip.builder()
                .request(createRequest(testUser))
                .vehicle(testVehicle)
                .user(testUser)
                .status(TripStatus.COMPLETED)
                .startedAt(new Timestamp(System.currentTimeMillis()))
                .build();
        tripRepository.save(trip2);

        // Act & Assert
        mockMvc.perform(get("/api/trips"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(greaterThanOrEqualTo(2)));
    }

    // TODO: Should be uncommented after handled correctly in the refactoring phase
    // @Test
    // @DisplayName("shouldGetTripsByUserId")
    // @WithMockUser(username = "test@example.com")
    // void shouldGetTripsByUserId() throws Exception {
    // // Act & Assert
    // mockMvc.perform(get("/api/trips/user"))
    // .andExpect(status().isOk())
    // .andExpect(jsonPath("$.success").value(true))
    // .andExpect(jsonPath("$.data").isArray())
    // .andExpect(jsonPath("$.data.length()").value(greaterThanOrEqualTo(1)))
    // .andExpect(jsonPath("$.data[0].user.id").value(testUser.getId()));
    // }

    // TODO: Should be uncommented after handled correctly in the refactoring phase
    // @Test
    // @DisplayName("shouldGetLastNTripsForUser")
    // @WithMockUser(username = "test@example.com")
    // void shouldGetLastNTripsForUser() throws Exception {
    // // Arrange - Create multiple trips
    // for (int i = 0; i < 3; i++) {
    // Trip trip = Trip.builder()
    // .request(createRequest(testUser))
    // .vehicle(testVehicle)
    // .user(testUser)
    // .status(TripStatus.COMPLETED)
    // .startedAt(new Timestamp(System.currentTimeMillis() - (i * 60000)))
    // .build();
    // tripRepository.save(trip);
    // }
    //
    // // Act & Assert
    // mockMvc.perform(get("/api/trips/last/2"))
    // .andExpect(status().isOk())
    // .andExpect(jsonPath("$.success").value(true))
    // .andExpect(jsonPath("$.data").isArray())
    // .andExpect(jsonPath("$.data.length()").value(2));
    // }

    @Test
    @DisplayName("shouldGetTripsByVinNumber")
    @WithMockUser
    void shouldGetTripsByVinNumber() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/trips/vehicle/" + testVehicle.getVinNumber()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(greaterThanOrEqualTo(1)));
    }

    @Test
    @DisplayName("shouldReturn400WhenVehicleNotFoundForTrips")
    @WithMockUser
    void shouldReturn400WhenVehicleNotFoundForTrips() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/trips/vehicle/INVALID_VIN"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(containsString("Failed to get Trips by VIN number")));
    }

    @Test
    @DisplayName("shouldReturn401WhenNotAuthenticated")
    void shouldReturn401WhenNotAuthenticated() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/trips/request")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(tripRequestDto)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("shouldHandleInvalidJsonPayload")
    @WithMockUser
    void shouldHandleInvalidJsonPayload() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/trips/request")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{invalid json"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("shouldReturnEmptyArrayWhenNoTripsForVehicle")
    @WithMockUser
    void shouldReturnEmptyArrayWhenNoTripsForVehicle() throws Exception {
        // Arrange - Create vehicle without trips
        Point location = geometryFactory.createPoint(new Coordinate(32.0, 31.0));
        Vehicle newVehicle = Vehicle.builder()
                .vinNumber("VIN987654321")
                .model("Accord")
                .status(VehicleStatus.IDLE)
                .lastUpdatedLocation(location)
                .build();
        newVehicle = vehicleRepository.save(newVehicle);

        // Act & Assert
        mockMvc.perform(get("/api/trips/vehicle/" + newVehicle.getVinNumber()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(0));
    }

    @Test
    @DisplayName("shouldValidateTripRequestFields")
    @WithMockUser
    void shouldValidateTripRequestFields() throws Exception {
        // Arrange
        TripRequestDto invalidDto = TripRequestDto.builder().build(); // Missing required fields

        // Act & Assert
        mockMvc.perform(post("/api/trips/request")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isForbidden());
    }
}

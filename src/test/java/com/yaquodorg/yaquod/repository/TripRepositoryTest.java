package com.yaquodorg.yaquod.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import com.yaquodorg.yaquod.entity.Request;
import com.yaquodorg.yaquod.entity.RequestStatus;
import com.yaquodorg.yaquod.entity.Role;
import com.yaquodorg.yaquod.entity.Trip;
import com.yaquodorg.yaquod.entity.TripStatus;
import com.yaquodorg.yaquod.entity.User;
import com.yaquodorg.yaquod.entity.Vehicle;
import com.yaquodorg.yaquod.entity.VehicleStatus;

/**
 * NOTE: ALL THOSE TESTS ARE AI-GENERATED AND REVIEWED MANUALLY
 * <p>
 * Unit tests for TripRepository
 * Uses real database (H2 in-memory or Testcontainers)
 * Tests JPA queries and database interactions
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@DisplayName("TripRepository Unit Tests")
class TripRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private TripRepository tripRepository;

    private User testUser;
    private Vehicle testVehicle;
    private Request testRequest;
    private Trip testTrip;

    @BeforeEach
    void setUp() {

        Date now = new Date(0);

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
        entityManager.persist(testUser);

        testVehicle = Vehicle.builder()
                .vinNumber("VIN123456789")
                .model("Camry")
                .status(VehicleStatus.IDLE)
                .build();
        entityManager.persist(testVehicle);

        testRequest = Request.builder()
                .user(testUser)
                .status(RequestStatus.PENDING)
                .createdAt(new Timestamp(System.currentTimeMillis()))
                .build();
        entityManager.persist(testRequest);

        testTrip = Trip.builder()
                .request(testRequest)
                .vehicle(testVehicle)
                .user(testUser)
                .status(TripStatus.INITIATED)
                .startedAt(new Timestamp(System.currentTimeMillis()))
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

    @Test
    @DisplayName("shouldSaveTrip")
    void shouldSaveTrip() {
        // Act
        Trip savedTrip = tripRepository.save(testTrip);
        entityManager.flush();

        // Assert
        assertNotNull(savedTrip.getId());
        assertEquals(testUser.getId(), savedTrip.getUser().getId());
        assertEquals(testVehicle.getId(), savedTrip.getVehicle().getId());
        assertEquals(testRequest.getId(), savedTrip.getRequest().getId());
        assertEquals(TripStatus.INITIATED, savedTrip.getStatus());
    }

    @Test
    @DisplayName("shouldFindTripById")
    void shouldFindTripById() {
        // Arrange
        Trip savedTrip = entityManager.persist(testTrip);
        entityManager.flush();

        // Act
        Optional<Trip> result = tripRepository.findById(savedTrip.getId());

        // Assert
        assertTrue(result.isPresent());
        assertEquals(savedTrip.getId(), result.get().getId());
    }

    @Test
    @DisplayName("shouldReturnEmptyWhenTripNotFound")
    void shouldReturnEmptyWhenTripNotFound() {
        // Act
        Optional<Trip> result = tripRepository.findById(999L);

        // Assert
        assertFalse(result.isPresent());
    }

    @Test
    @DisplayName("shouldFindTripByRequestId")
    void shouldFindTripByRequestId() {
        // Arrange
        entityManager.persist(testTrip);
        entityManager.flush();

        // Act
        Trip result = tripRepository.findByRequestId(testRequest.getId());

        // Assert
        assertNotNull(result);
        assertEquals(testRequest.getId(), result.getRequest().getId());
    }

    @Test
    @DisplayName("shouldReturnNullWhenTripNotFoundByRequestId")
    void shouldReturnNullWhenTripNotFoundByRequestId() {
        // Act
        Trip result = tripRepository.findByRequestId(999L);

        // Assert
        assertNull(result);
    }

    @Test
    @DisplayName("shouldFindTripsByUserId")
    void shouldFindTripsByUserId() {
        // Arrange
        entityManager.persist(testTrip);

        Trip trip2 = Trip.builder()
                .request(createRequest(testUser))
                .vehicle(testVehicle)
                .user(testUser)
                .status(TripStatus.COMPLETED)
                .startedAt(new Timestamp(System.currentTimeMillis()))
                .build();
        entityManager.persist(trip2);
        entityManager.flush();

        // Act
        List<Trip> result = tripRepository.findByUserId(testUser.getId());

        // Assert
        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(t -> t.getUser().getId().equals(testUser.getId())));
    }

    @Test
    @DisplayName("shouldReturnEmptyListWhenNoTripsForUser")
    void shouldReturnEmptyListWhenNoTripsForUser() {
        // Arrange
        Date now = new Date(0);

        User anotherUser = User.builder()
                .email("another@example.com")
                .passwordHash("password123")
                .firstName("Jane")
                .lastName("Smith")
                .phoneNumber("+9876543210")
                .join_date(new Timestamp(now.getTime()))
                .role(Role.ADMIN)
                .code(111111)
                .isEmailVerified(true)
                .build();
        entityManager.persist(anotherUser);
        entityManager.flush();

        // Act
        List<Trip> result = tripRepository.findByUserId(anotherUser.getId());

        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("shouldFindTripsByUserIdOrderedByStartedAtDesc")
    void shouldFindTripsByUserIdOrderedByStartedAtDesc() {
        // Arrange
        Trip trip1 = Trip.builder()
                .request(createRequest(testUser))
                .vehicle(testVehicle)
                .user(testUser)
                .status(TripStatus.INITIATED)
                .startedAt(new Timestamp(System.currentTimeMillis() - 3600000)) // 1 hour ago
                .build();
        entityManager.persist(trip1);

        Trip trip2 = Trip.builder()
                .request(testRequest)
                .vehicle(testVehicle)
                .user(testUser)
                .status(TripStatus.COMPLETED)
                .startedAt(new Timestamp(System.currentTimeMillis() - 1800000)) // 30 min ago
                .build();
        entityManager.persist(trip2);

        Trip trip3 = Trip.builder()
                .request(createRequest(testUser))
                .vehicle(testVehicle)
                .user(testUser)
                .status(TripStatus.IN_PROGRESS)
                .startedAt(new Timestamp(System.currentTimeMillis())) // now
                .build();
        entityManager.persist(trip3);
        entityManager.flush();

        // Act
        List<Trip> result = tripRepository.findByUserIdOrderByStartedAtDesc(
                testUser.getId(), PageRequest.of(0, 2));

        // Assert
        assertEquals(2, result.size());
        assertTrue(result.get(0).getStartedAt().after(result.get(1).getStartedAt()));
        assertEquals(trip3.getId(), result.get(0).getId());
        assertEquals(trip2.getId(), result.get(1).getId());
    }

    @Test
    @DisplayName("shouldLimitResultsWithPageable")
    void shouldLimitResultsWithPageable() {
        // Arrange
        for (int i = 0; i < 5; i++) {
            Trip trip = Trip.builder()
                    .request(createRequest(testUser))
                    .vehicle(testVehicle)
                    .user(testUser)
                    .status(TripStatus.COMPLETED)
                    .startedAt(new Timestamp(System.currentTimeMillis() - (i * 1000)))
                    .build();
            entityManager.persist(trip);
        }
        entityManager.flush();

        // Act
        List<Trip> result = tripRepository.findByUserIdOrderByStartedAtDesc(
                testUser.getId(), PageRequest.of(0, 3));

        // Assert
        assertEquals(3, result.size());
    }

    @Test
    @DisplayName("shouldFindTripsByVehicleVinNumber")
    void shouldFindTripsByVehicleVinNumber() {
        // Arrange
        entityManager.persist(testTrip);

        Trip trip2 = Trip.builder()
                .request(createRequest(testUser))
                .vehicle(testVehicle)
                .user(testUser)
                .status(TripStatus.COMPLETED)
                .startedAt(new Timestamp(System.currentTimeMillis()))
                .build();
        entityManager.persist(trip2);
        entityManager.flush();

        // Act
        List<Trip> result = tripRepository.findByVehicleVinNumber(testVehicle.getVinNumber());

        // Assert
        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(t -> t.getVehicle().getVinNumber().equals(testVehicle.getVinNumber())));
    }

    @Test
    @DisplayName("shouldReturnEmptyListWhenNoTripsForVehicle")
    void shouldReturnEmptyListWhenNoTripsForVehicle() {
        // Arrange
        Vehicle anotherVehicle = Vehicle.builder()
                .vinNumber("VIN987654321")
                .model("Accord")
                .status(VehicleStatus.IDLE)
                .build();
        entityManager.persist(anotherVehicle);
        entityManager.flush();

        // Act
        List<Trip> result = tripRepository.findByVehicleVinNumber(anotherVehicle.getVinNumber());

        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("shouldFindAllTrips")
    void shouldFindAllTrips() {
        // Arrange
        entityManager.persist(testTrip);

        Trip trip2 = Trip.builder()
                .request(createRequest(testUser))
                .vehicle(testVehicle)
                .user(testUser)
                .status(TripStatus.COMPLETED)
                .startedAt(new Timestamp(System.currentTimeMillis()))
                .build();
        entityManager.persist(trip2);
        entityManager.flush();

        // Act
        List<Trip> result = tripRepository.findAll();

        // Assert
        assertEquals(2, result.size());
    }

    @Test
    @DisplayName("shouldDeleteTripById")
    void shouldDeleteTripById() {
        // Arrange
        Trip savedTrip = entityManager.persist(testTrip);
        entityManager.flush();
        Long tripId = savedTrip.getId();

        // Act
        tripRepository.deleteById(tripId);
        entityManager.flush();

        // Assert
        Trip deletedTrip = entityManager.find(Trip.class, tripId);
        assertNull(deletedTrip);
    }

    @Test
    @DisplayName("shouldUpdateTripStatus")
    void shouldUpdateTripStatus() {
        // Arrange
        Trip savedTrip = entityManager.persist(testTrip);
        entityManager.flush();
        entityManager.clear();

        // Act
        Trip tripToUpdate = tripRepository.findById(savedTrip.getId()).get();
        tripToUpdate.setStatus(TripStatus.COMPLETED);
        tripToUpdate.setEndedAt(new Timestamp(System.currentTimeMillis()));

        Trip updatedTrip = tripRepository.save(tripToUpdate);
        entityManager.flush();

        // Assert
        assertEquals(TripStatus.COMPLETED, updatedTrip.getStatus());
        assertNotNull(updatedTrip.getEndedAt());
    }

    @Test
    @DisplayName("shouldMaintainRelationships")
    void shouldMaintainRelationships() {
        // Act
        Trip savedTrip = tripRepository.save(testTrip);
        entityManager.flush();
        entityManager.clear();

        // Assert
        Trip foundTrip = tripRepository.findById(savedTrip.getId()).get();
        assertNotNull(foundTrip.getUser());
        assertNotNull(foundTrip.getVehicle());
        assertNotNull(foundTrip.getRequest());
        assertEquals(testUser.getId(), foundTrip.getUser().getId());
        assertEquals(testVehicle.getId(), foundTrip.getVehicle().getId());
        assertEquals(testRequest.getId(), foundTrip.getRequest().getId());
    }
}

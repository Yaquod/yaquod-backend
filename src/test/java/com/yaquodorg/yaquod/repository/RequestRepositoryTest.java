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
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import com.yaquodorg.yaquod.entity.Request;
import com.yaquodorg.yaquod.entity.RequestStatus;
import com.yaquodorg.yaquod.entity.Role;
import com.yaquodorg.yaquod.entity.User;

/**
 * NOTE: ALL THOSE TESTS ARE AI-GENERATED AND REVIEWED MANUALLY
 * <p>
 * Unit tests for RequestRepository
 * Uses real database (H2 in-memory or Testcontainers)
 * Tests JPA queries and database interactions
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@DisplayName("RequestRepository Unit Tests")
class RequestRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private RequestRepository requestRepository;

    private GeometryFactory geometryFactory;
    private User testUser;
    private Request testRequest;

    @BeforeEach
    void setUp() {
        geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);

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

        Point startPoint = geometryFactory.createPoint(new Coordinate(31.0, 30.0));
        Point endPoint = geometryFactory.createPoint(new Coordinate(31.5, 30.5));

        testRequest = Request.builder()
                .user(testUser)
                .startLocation(startPoint)
                .destinationLocation(endPoint)
                .status(RequestStatus.PENDING)
                .createdAt(new Timestamp(System.currentTimeMillis()))
                .build();
    }

    @Test
    @DisplayName("Should save request")
    void shouldSaveRequest() {
        // Act
        Request savedRequest = requestRepository.save(testRequest);
        entityManager.flush();

        // Assert
        assertNotNull(savedRequest.getId());
        assertEquals(testRequest.getUser().getId(), savedRequest.getUser().getId());
        assertEquals(RequestStatus.PENDING, savedRequest.getStatus());
    }

    @Test
    @DisplayName("Should save request with geometry points")
    void shouldSaveRequestWithGeo() {
        // Act
        Request savedRequest = requestRepository.save(testRequest);
        entityManager.flush();
        entityManager.clear();

        // Assert
        Request foundRequest = entityManager.find(Request.class, savedRequest.getId());
        assertNotNull(foundRequest.getStartLocation());
        assertNotNull(foundRequest.getDestinationLocation());
        assertEquals(31.0, foundRequest.getStartLocation().getX());
        assertEquals(30.0, foundRequest.getStartLocation().getY());
        assertEquals(31.5, foundRequest.getDestinationLocation().getX());
        assertEquals(30.5, foundRequest.getDestinationLocation().getY());
    }

    @Test
    @DisplayName("Should find and return request")
    void shouldFindAllRequests() {
        // Arrange
        Request savedRequest = entityManager.persist(testRequest);
        entityManager.flush();

        // Act
        Optional<Request> result = requestRepository.findById(savedRequest.getId());

        // Assert
        assertTrue(result.isPresent());
        assertEquals(savedRequest.getId(), result.get().getId());
        assertEquals(testUser.getId(), result.get().getUser().getId());
    }

    @Test
    @DisplayName("Should return empty list when there's no request")
    void findById_ShouldReturnEmpty_WhenNotExists() {
        // Act
        Optional<Request> result = requestRepository.findById(999L);

        // Assert
        assertFalse(result.isPresent());
    }

    @Test
    @DisplayName("Should return all requests")
    void shouldFindAllRequestsWithNewOne() {
        // Arrange
        entityManager.persist(testRequest);

        Request request2 = Request.builder()
                .user(testUser)
                .startLocation(geometryFactory.createPoint(new Coordinate(32.0, 31.0)))
                .destinationLocation(geometryFactory.createPoint(new Coordinate(32.5, 31.5)))
                .status(RequestStatus.COMPLETED)
                .createdAt(new Timestamp(System.currentTimeMillis()))
                .build();
        entityManager.persist(request2);
        entityManager.flush();

        // Act
        List<Request> requests = requestRepository.findAll();

        // Assert
        assertEquals(2, requests.size());
    }

    @Test
    @DisplayName("Should return empty list when no requests")
    void findAllWithEmptyRequests() {
        // Act
        List<Request> requests = requestRepository.findAll();

        // Assert
        assertTrue(requests.isEmpty());
    }

    @Test
    @DisplayName("Should delete request")
    void shouldDeleteById() {
        // Arrange
        Request savedRequest = entityManager.persist(testRequest);
        entityManager.flush();
        Long requestId = savedRequest.getId();

        // Act
        requestRepository.deleteById(requestId);
        entityManager.flush();

        // Assert
        Request deletedRequest = entityManager.find(Request.class, requestId);
        assertNull(deletedRequest);
    }

    @Test
    @DisplayName("Should update request")
    void shouldUpdateRequest() {
        // Arrange
        Request savedRequest = entityManager.persist(testRequest);
        entityManager.flush();
        entityManager.clear();

        // Act
        Request requestToUpdate = requestRepository.findById(savedRequest.getId()).get();
        requestToUpdate.setStatus(RequestStatus.COMPLETED);
        requestToUpdate.setEstimatedTime(20.0);
        requestToUpdate.setEstimatedFare(75.5);

        Request updatedRequest = requestRepository.save(requestToUpdate);
        entityManager.flush();

        // Assert
        assertEquals(RequestStatus.COMPLETED, updatedRequest.getStatus());
        assertEquals(20.0, updatedRequest.getEstimatedTime());
        assertEquals(75.5, updatedRequest.getEstimatedFare());
    }

    @Test
    void save_ShouldMaintainRelationshipWithUser() {
        // Act
        Request savedRequest = requestRepository.save(testRequest);
        entityManager.flush();
        entityManager.clear();

        // Assert
        Request foundRequest = requestRepository.findById(savedRequest.getId()).get();
        assertNotNull(foundRequest.getUser());
        assertEquals(testUser.getId(), foundRequest.getUser().getId());
        assertEquals(testUser.getEmail(), foundRequest.getUser().getEmail());
    }

    @Test
    void save_ShouldPersistTimestamp() {
        // Act
        Request savedRequest = requestRepository.save(testRequest);
        entityManager.flush();

        // Assert
        assertNotNull(savedRequest.getCreatedAt());
        assertTrue(savedRequest.getCreatedAt().getTime() <= System.currentTimeMillis());
    }
}

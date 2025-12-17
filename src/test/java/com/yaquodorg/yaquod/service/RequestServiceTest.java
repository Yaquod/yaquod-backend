package com.yaquodorg.yaquod.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.yaquodorg.yaquod.entity.Request;
import com.yaquodorg.yaquod.entity.RequestStatus;
import com.yaquodorg.yaquod.entity.User;
import com.yaquodorg.yaquod.repository.RequestRepository;
import com.yaquodorg.yaquod.service.request.RequestServiceImpl;
import com.yaquodorg.yaquod.service.trip.TripService;
import com.yaquodorg.yaquod.service.user.UserService;

/**
 * NOTE: ALL THOSE TESTS ARE AI-GENERATED AND REVIEWED MANUALLY
 *
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("RequestService Unit Tests")
class RequestServiceTest {

    @Mock
    private RequestRepository requestRepository;

    @Mock
    private UserService userService;

    @Mock
    private TripService tripService;

    @InjectMocks
    private RequestServiceImpl requestService;

    private GeometryFactory geometryFactory;
    private User testUser;
    private Request testRequest;
    private Point startPoint;
    private Point endPoint;

    @BeforeEach
    void setUp() {
        geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);

        testUser = User.builder()
                .id(1L)
                .email("test@example.com")
                .build();

        startPoint = geometryFactory.createPoint(new Coordinate(31.0, 30.0));
        endPoint = geometryFactory.createPoint(new Coordinate(31.5, 30.5));

        testRequest = Request.builder()
                .id(1L)
                .user(testUser)
                .startLocation(startPoint)
                .destinationLocation(endPoint)
                .status(RequestStatus.PENDING)
                .createdAt(new Timestamp(System.currentTimeMillis()))
                .build();
    }

    @Test
    @DisplayName("Should create and return request")
    void createRequest() {
        // Arrange
        double startLong = 31.0;
        double startLat = 30.0;
        double endLong = 31.5;
        double endLat = 30.5;

        when(userService.getUserById(1L)).thenReturn(testUser);
        when(requestRepository.save(any(Request.class))).thenReturn(testRequest);

        // Act
        Request result = requestService.createRequest(1L, startLong, startLat, endLong, endLat);

        // Assert
        assertNotNull(result);
        assertEquals(testUser, result.getUser());
        assertEquals(RequestStatus.PENDING, result.getStatus());

        verify(userService).getUserById(1L);
        verify(requestRepository).save(any(Request.class));
        verify(tripService).createTrip(eq(testRequest), eq(startLong), eq(startLat), eq(endLong), eq(endLat));
    }

    @Test
    @DisplayName("Should set correct geometry points")
    void createRequestWithCorrectGeometry() {
        // Arrange
        double startLong = 31.0;
        double startLat = 30.0;
        double endLong = 31.5;
        double endLat = 30.5;

        when(userService.getUserById(1L)).thenReturn(testUser);
        when(requestRepository.save(any(Request.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ArgumentCaptor<Request> requestCaptor = ArgumentCaptor.forClass(Request.class);

        // Act
        requestService.createRequest(1L, startLong, startLat, endLong, endLat);

        // Assert
        verify(requestRepository).save(requestCaptor.capture());
        Request capturedRequest = requestCaptor.getValue();

        assertEquals(startLong, capturedRequest.getStartLocation().getX());
        assertEquals(startLat, capturedRequest.getStartLocation().getY());
        assertEquals(endLong, capturedRequest.getDestinationLocation().getX());
        assertEquals(endLat, capturedRequest.getDestinationLocation().getY());
    }

    @Test
    @DisplayName("Shoud throw exception with null user")
    void createRequestWithNullUser() {
        // Arrange
        when(userService.getUserById(999L)).thenThrow(new RuntimeException("User not found"));

        // Act & Assert
        assertThrows(RuntimeException.class,
                () -> requestService.createRequest(999L, 31.0, 30.0, 31.5, 30.5));

        verify(requestRepository, never()).save(any(Request.class));
        verify(tripService, never()).createTrip(any(), anyDouble(), anyDouble(), anyDouble(), anyDouble());
    }

    @Test
    @DisplayName("Should return all requests")
    void getRequests() {
        // Arrange
        Request request2 = Request.builder().id(2L).build();
        List<Request> expectedRequests = Arrays.asList(testRequest, request2);
        when(requestRepository.findAll()).thenReturn(expectedRequests);

        // Act
        List<Request> result = requestService.getRequests();

        // Assert
        assertEquals(2, result.size());
        assertEquals(expectedRequests, result);
        verify(requestRepository).findAll();
    }

    @Test
    @DisplayName("Should return empty requests list")
    void getRequestsWithEmptyList() {
        // Arrange
        when(requestRepository.findAll()).thenReturn(Arrays.asList());

        // Act
        List<Request> result = requestService.getRequests();

        // Assert
        assertTrue(result.isEmpty());
        verify(requestRepository).findAll();
    }

    @Test
    @DisplayName("Should return all user requests")
    void getUserRequests() {
        // Arrange
        List<Request> userRequests = Arrays.asList(testRequest);
        testUser.setRequests(userRequests);
        when(userService.getUserById(1L)).thenReturn(testUser);

        // Act
        List<Request> result = requestService.getUserRequests(1L);

        // Assert
        assertEquals(1, result.size());
        assertEquals(userRequests, result);
        verify(userService).getUserById(1L);
    }

    @Test
    @DisplayName("Should throw exception for null user")
    void getUserRequestsWithNullUser() {
        // Arrange
        when(userService.getUserById(999L)).thenThrow(new RuntimeException("User not found"));

        // Act & Assert
        assertThrows(RuntimeException.class,
                () -> requestService.getUserRequests(999L));
    }

    @Test
    @DisplayName("Should return request")
    void getRequest() {
        // Arrange
        when(requestRepository.findById(1L)).thenReturn(Optional.of(testRequest));

        // Act
        Request result = requestService.getRequest(1L);

        // Assert
        assertNotNull(result);
        assertEquals(testRequest.getId(), result.getId());
        verify(requestRepository).findById(1L);
    }

    @Test
    @DisplayName("Should throw exception when request not found")
    void getRequestWithNull() {
        // Arrange
        when(requestRepository.findById(999L)).thenReturn(Optional.empty());

        // Act
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> requestService.getRequest(999L));

        // Assert
        assertEquals("Request not found!", exception.getMessage());
    }

    @Test
    @DisplayName("Should update request fields")
    void updateRequest() {
        // Arrange
        RequestStatus newStatus = RequestStatus.COMPLETED;
        double estimatedTime = 15.5;
        double estimatedFare = 50.0;

        when(requestRepository.findById(1L)).thenReturn(Optional.of(testRequest));

        // Act
        requestService.updateRequest(1L, newStatus, estimatedTime, estimatedFare);

        // Assert
        assertEquals(newStatus, testRequest.getStatus());
        assertEquals(estimatedTime, testRequest.getEstimatedTime());
        assertEquals(estimatedFare, testRequest.getEstimatedFare());
        verify(requestRepository).findById(1L);
    }

    @Test
    @DisplayName("Should throw exception for null request")
    void updateRequestWithNull() {
        // Arrange
        when(requestRepository.findById(999L)).thenReturn(Optional.empty());

        // Act
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> requestService.updateRequest(999L, RequestStatus.COMPLETED, 15.5, 50.0));

        // Assert
        assertEquals("Request not found!", exception.getMessage());
    }

    @Test
    @DisplayName("Should delete request")
    void deleteRequest() {
        // Arrange
        doNothing().when(requestRepository).deleteById(1L);

        // Act
        requestService.deleteRequest(1L);

        // Assert
        verify(requestRepository).deleteById(1L);
    }

    @Test
    @DisplayName("Should not throw exception when request not found")
    void deleteRequestWithNull() {
        // Arrange
        doNothing().when(requestRepository).deleteById(999L);

        // Act
        assertDoesNotThrow(() -> requestService.deleteRequest(999L));

        // Assert
        verify(requestRepository).deleteById(999L);
    }
}

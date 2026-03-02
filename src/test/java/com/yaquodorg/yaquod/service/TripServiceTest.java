package com.yaquodorg.yaquod.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.yaquodorg.yaquod.dtos.InitTripDto;
import com.yaquodorg.yaquod.entity.*;
import com.yaquodorg.yaquod.repository.TripRepository;
import com.yaquodorg.yaquod.service.trip.TripServiceImpl;
import com.yaquodorg.yaquod.service.user.UserService;
import com.yaquodorg.yaquod.service.vehicle.VehicleService;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

/** NOTE: ALL THOSE TESTS ARE AI-GENERATED AND REVIEWED MANUALLY */
@ExtendWith(MockitoExtension.class)
@DisplayName("TripService Unit Tests")
class TripServiceImplTest {

    @Mock private TripRepository tripRepository;

    @Mock private VehicleService vehicleService;

    @Mock private UserService userService;

    @Mock private ApplicationEventPublisher eventPublisher;

    @InjectMocks private TripServiceImpl tripService;

    private User testUser;
    private Vehicle testVehicle;
    private Request testRequest;
    private Trip testTrip;

    @BeforeEach
    void setUp() {
        testUser = User.builder().id(1L).email("test@example.com").build();

        testVehicle =
                Vehicle.builder().id(1L).vinNumber("VIN123456").status(VehicleStatus.IDLE).build();

        testRequest = Request.builder().id(1L).user(testUser).build();

        testTrip =
                Trip.builder()
                        .id(1L)
                        .request(testRequest)
                        .vehicle(testVehicle)
                        .user(testUser)
                        .status(TripStatus.INITIATED)
                        .startedAt(new Timestamp(System.currentTimeMillis()))
                        .build();
    }

    @Test
    @DisplayName("shouldCreateTrip")
    void shouldCreateTrip() {
        // Arrange
        double startLong = 31.0;
        double startLat = 30.0;
        double endLong = 31.5;
        double endLat = 30.5;

        when(vehicleService.findKNearestVehicles(startLong, startLat, 1))
                .thenReturn(Collections.singletonList(testVehicle));
        when(tripRepository.save(any(Trip.class))).thenReturn(testTrip);

        // Act
        tripService.createTrip(testRequest, startLong, startLat, endLong, endLat);

        // Assert
        verify(vehicleService).findKNearestVehicles(startLong, startLat, 1);
        verify(vehicleService)
                .updateVehicleStatus(testVehicle.getVinNumber(), VehicleStatus.PROCESSING);
        verify(tripRepository).save(any(Trip.class));
        verify(eventPublisher).publishEvent(any(InitTripDto.class));
    }

    @Test
    @DisplayName("shouldSaveTripWithCorrectStatus")
    void shouldSaveTripWithCorrectStatus() {
        // Arrange
        double startLong = 31.0;
        double startLat = 30.0;
        double endLong = 31.5;
        double endLat = 30.5;

        when(vehicleService.findKNearestVehicles(anyDouble(), anyDouble(), anyInt()))
                .thenReturn(Collections.singletonList(testVehicle));
        when(tripRepository.save(any(Trip.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ArgumentCaptor<Trip> tripCaptor = ArgumentCaptor.forClass(Trip.class);

        // Act
        tripService.createTrip(testRequest, startLong, startLat, endLong, endLat);

        // Assert
        verify(tripRepository).save(tripCaptor.capture());
        Trip capturedTrip = tripCaptor.getValue();

        assertEquals(TripStatus.INITIATED, capturedTrip.getStatus());
        assertEquals(testRequest, capturedTrip.getRequest());
        assertEquals(testVehicle, capturedTrip.getVehicle());
        assertEquals(testUser, capturedTrip.getUser());
        assertNotNull(capturedTrip.getStartedAt());
    }

    @Test
    @DisplayName("shouldPublishInitTripDtoWithCorrectData")
    void shouldPublishInitTripDtoWithCorrectData() {
        // Arrange
        double startLong = 31.0;
        double startLat = 30.0;
        double endLong = 31.5;
        double endLat = 30.5;

        when(vehicleService.findKNearestVehicles(anyDouble(), anyDouble(), anyInt()))
                .thenReturn(Collections.singletonList(testVehicle));
        when(tripRepository.save(any(Trip.class))).thenReturn(testTrip);

        ArgumentCaptor<InitTripDto> dtoCaptor = ArgumentCaptor.forClass(InitTripDto.class);

        // Act
        tripService.createTrip(testRequest, startLong, startLat, endLong, endLat);

        // Assert
        verify(eventPublisher).publishEvent(dtoCaptor.capture());
        InitTripDto capturedDto = dtoCaptor.getValue();

        assertEquals(testVehicle.getVinNumber(), capturedDto.getVinNumber());
        assertEquals(testRequest.getId(), capturedDto.getRequestId());
        assertEquals(startLong, capturedDto.getStartLong());
        assertEquals(startLat, capturedDto.getStartLat());
        assertEquals(endLong, capturedDto.getEndLong());
        assertEquals(endLat, capturedDto.getEndLat());
    }

    @Test
    @DisplayName("shouldThrowExceptionWhenNoVehiclesAvailable")
    void shouldThrowExceptionWhenNoVehiclesAvailable() {
        // Arrange
        when(vehicleService.findKNearestVehicles(anyDouble(), anyDouble(), anyInt()))
                .thenReturn(Collections.emptyList());

        // Act & Assert
        RuntimeException exception =
                assertThrows(
                        RuntimeException.class,
                        () -> tripService.createTrip(testRequest, 31.0, 30.0, 31.5, 30.5));

        assertEquals("No vehicles available for the requested location", exception.getMessage());
        verify(vehicleService, never()).updateVehicleStatus(anyString(), any());
        verify(tripRepository, never()).save(any(Trip.class));
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    @DisplayName("shouldUpdateVehicleStatusToProcessing")
    void shouldUpdateVehicleStatusToProcessing() {
        // Arrange
        when(vehicleService.findKNearestVehicles(anyDouble(), anyDouble(), anyInt()))
                .thenReturn(Collections.singletonList(testVehicle));
        when(tripRepository.save(any(Trip.class))).thenReturn(testTrip);

        // Act
        tripService.createTrip(testRequest, 31.0, 30.0, 31.5, 30.5);

        // Assert
        verify(vehicleService)
                .updateVehicleStatus(testVehicle.getVinNumber(), VehicleStatus.PROCESSING);
    }

    @Test
    @DisplayName("shouldGetTripByRequestId")
    void shouldGetTripByRequestId() {
        // Arrange
        when(tripRepository.findByRequestId(1L)).thenReturn(testTrip);

        // Act
        Trip result = tripService.getTripByRequestId(1L);

        // Assert
        assertNotNull(result);
        assertEquals(testTrip.getId(), result.getId());
        verify(tripRepository).findByRequestId(1L);
    }

    @Test
    @DisplayName("shouldThrowExceptionWhenTripNotFoundByRequestId")
    void shouldThrowExceptionWhenTripNotFoundByRequestId() {
        // Arrange
        when(tripRepository.findByRequestId(999L)).thenReturn(null);

        // Act & Assert
        RuntimeException exception =
                assertThrows(RuntimeException.class, () -> tripService.getTripByRequestId(999L));

        assertEquals("Trip not found for requestId: 999", exception.getMessage());
    }

    @Test
    @DisplayName("shouldGetTripById")
    void shouldGetTripById() {
        // Arrange
        when(tripRepository.findById(1L)).thenReturn(Optional.of(testTrip));

        // Act
        Trip result = tripService.getTripById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(testTrip.getId(), result.getId());
        verify(tripRepository).findById(1L);
    }

    @Test
    @DisplayName("shouldThrowExceptionWhenTripNotFoundById")
    void shouldThrowExceptionWhenTripNotFoundById() {
        // Arrange
        when(tripRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception =
                assertThrows(RuntimeException.class, () -> tripService.getTripById(999L));

        assertEquals("Trip not found for id: 999", exception.getMessage());
    }

    @Test
    @DisplayName("shouldDeleteTripById")
    void shouldDeleteTripById() {
        // Arrange
        doNothing().when(tripRepository).deleteById(1L);

        // Act
        tripService.deleteTripById(1L);

        // Assert
        verify(tripRepository).deleteById(1L);
    }

    @Test
    @DisplayName("shouldGetAllTrips")
    void shouldGetAllTrips() {
        // Arrange
        Trip trip2 = Trip.builder().id(2L).build();
        List<Trip> expectedTrips = Arrays.asList(testTrip, trip2);
        when(tripRepository.findAll()).thenReturn(expectedTrips);

        // Act
        List<Trip> result = tripService.getAllTrips();

        // Assert
        assertEquals(2, result.size());
        assertEquals(expectedTrips, result);
        verify(tripRepository).findAll();
    }

    @Test
    @DisplayName("shouldGetTripsByUserId")
    void shouldGetTripsByUserId() {
        // Arrange
        List<Trip> userTrips = Collections.singletonList(testTrip);
        when(userService.getUserById(1L)).thenReturn(testUser);
        when(tripRepository.findByUserId(1L)).thenReturn(userTrips);

        // Act
        List<Trip> result = tripService.getTripsByUserId(1L);

        // Assert
        assertEquals(1, result.size());
        assertEquals(userTrips, result);
        verify(userService).getUserById(1L);
        verify(tripRepository).findByUserId(1L);
    }

    @Test
    @DisplayName("shouldThrowExceptionWhenUserNotFoundForTrips")
    void shouldThrowExceptionWhenUserNotFoundForTrips() {
        // Arrange
        when(userService.getUserById(999L)).thenReturn(null);

        // Act & Assert
        RuntimeException exception =
                assertThrows(RuntimeException.class, () -> tripService.getTripsByUserId(999L));

        assertEquals("User not found for id: 999", exception.getMessage());
        verify(tripRepository, never()).findByUserId(anyLong());
    }

    @Test
    @DisplayName("shouldGetUserLastNTrips")
    void shouldGetUserLastNTrips() {
        // Arrange
        int n = 5;
        List<Trip> lastTrips = Collections.singletonList(testTrip);
        when(tripRepository.findByUserIdOrderByStartedAtDesc(eq(1L), any(Pageable.class)))
                .thenReturn(lastTrips);

        // Act
        List<Trip> result = tripService.getUserLastNTrips(n, 1L);

        // Assert
        assertEquals(1, result.size());
        assertEquals(lastTrips, result);
        verify(tripRepository).findByUserIdOrderByStartedAtDesc(eq(1L), eq(PageRequest.of(0, n)));
    }

    @Test
    @DisplayName("shouldGetTripsByVinNumber")
    void shouldGetTripsByVinNumber() {
        // Arrange
        String vinNumber = "VIN123456";
        List<Trip> vehicleTrips = Collections.singletonList(testTrip);
        when(vehicleService.getVehicleByVinNumber(vinNumber)).thenReturn(Optional.of(testVehicle));
        when(tripRepository.findByVehicleVinNumber(vinNumber)).thenReturn(vehicleTrips);

        // Act
        List<Trip> result = tripService.getTripsByVinNumber(vinNumber);

        // Assert
        assertEquals(1, result.size());
        assertEquals(vehicleTrips, result);
        verify(vehicleService).getVehicleByVinNumber(vinNumber);
        verify(tripRepository).findByVehicleVinNumber(vinNumber);
    }

    @Test
    @DisplayName("shouldThrowExceptionWhenVehicleNotFoundForTrips")
    void shouldThrowExceptionWhenVehicleNotFoundForTrips() {
        // Arrange
        String vinNumber = "INVALID_VIN";
        when(vehicleService.getVehicleByVinNumber(vinNumber)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception =
                assertThrows(
                        RuntimeException.class, () -> tripService.getTripsByVinNumber(vinNumber));

        assertEquals("Vehicle not found for vin number: " + vinNumber, exception.getMessage());
        verify(tripRepository, never()).findByVehicleVinNumber(anyString());
    }

    @Test
    @DisplayName("shouldReturnEmptyListWhenNoTripsExist")
    void shouldReturnEmptyListWhenNoTripsExist() {
        // Arrange
        when(tripRepository.findAll()).thenReturn(Collections.emptyList());

        // Act
        List<Trip> result = tripService.getAllTrips();

        // Assert
        assertTrue(result.isEmpty());
        verify(tripRepository).findAll();
    }
}

package com.yaquodorg.yaquod.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yaquodorg.yaquod.dtos.trip.TripDto;
import com.yaquodorg.yaquod.dtos.trip.TripRequestDto;
import com.yaquodorg.yaquod.entity.Request;
import com.yaquodorg.yaquod.entity.RequestStatus;
import com.yaquodorg.yaquod.entity.Trip;
import com.yaquodorg.yaquod.entity.TripStatus;
import com.yaquodorg.yaquod.entity.User;
import com.yaquodorg.yaquod.entity.Vehicle;
import com.yaquodorg.yaquod.exception.ResourceNotFoundException;
import com.yaquodorg.yaquod.service.request.RequestService;
import com.yaquodorg.yaquod.service.trip.TripService;
import com.yaquodorg.yaquod.utils.GlobalExceptionHandler;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

/**
 * NOTE: ALL THOSE TESTS ARE AI-GENERATED AND REVIEWED MANUALLY
 *
 * <p>Unit tests for TripController Tests controller logic with mocked services Does NOT test
 * security
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("TripController Unit Tests")
class TripControllerTest {

    @Mock private RequestService requestService;

    @Mock private TripService tripService;

    @InjectMocks private TripController tripController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private User testUser;
    private Request testRequest;
    private Trip testTrip;
    private Vehicle testVehicle;
    private TripRequestDto tripRequestDto;

    @BeforeEach
    void setUp() {
        // Setup MockMvc
        mockMvc =
                MockMvcBuilders.standaloneSetup(tripController)
                        .setControllerAdvice(new GlobalExceptionHandler())
                        .build();
        objectMapper = new ObjectMapper();

        testUser = User.builder().id(1L).email("test@example.com").build();

        testVehicle = Vehicle.builder().id(1L).vinNumber("VIN123456").build();

        testRequest =
                Request.builder()
                        .id(1L)
                        .user(testUser)
                        .status(RequestStatus.PENDING)
                        .createdAt(new Timestamp(System.currentTimeMillis()))
                        .build();

        testTrip =
                Trip.builder()
                        .id(1L)
                        .request(testRequest)
                        .vehicle(testVehicle)
                        .user(testUser)
                        .status(TripStatus.INITIATED)
                        .startedAt(new Timestamp(System.currentTimeMillis()))
                        .build();

        tripRequestDto =
                TripRequestDto.builder()
                        .startLong(31.0)
                        .startLat(30.0)
                        .endLong(31.5)
                        .endLat(30.5)
                        .build();
    }

    @Test
    @DisplayName("shouldCreateRequest")
    @WithMockUser
    void shouldCreateRequest() throws Exception {
        // Arrange
        when(requestService.createRequest(
                        any(), anyDouble(), anyDouble(), anyDouble(), anyDouble()))
                .thenReturn(testRequest);

        // Act & Assert
        mockMvc.perform(
                        post("/api/trips/request")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(tripRequestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.status").value("PENDING"));

        verify(requestService).createRequest(any(), eq(31.0), eq(30.0), eq(31.5), eq(30.5));
    }

    @Test
    @DisplayName("shouldReturn500WhenCreateRequestFails")
    @WithMockUser
    void shouldReturn500WhenCreateRequestFails() throws Exception {
        // Arrange
        when(requestService.createRequest(
                        any(), anyDouble(), anyDouble(), anyDouble(), anyDouble()))
                .thenThrow(new RuntimeException("Service error"));

        // Act & Assert
        mockMvc.perform(
                        post("/api/trips/request")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(tripRequestDto)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Internal server error: Service error"));
    }

    @Test
    @DisplayName("shouldGetRequestStatus")
    @WithMockUser
    void shouldGetRequestStatus() throws Exception {
        // Arrange
        when(requestService.getRequest(1L)).thenReturn(testRequest);

        // Act & Assert
        mockMvc.perform(get("/api/trips/request/status/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.status").value("PENDING"));

        verify(requestService).getRequest(1L);
    }

    @Test
    @DisplayName("shouldReturn404WhenRequestNotFound")
    @WithMockUser
    void shouldReturn404WhenRequestNotFound() throws Exception {
        // Arrange
        when(requestService.getRequest(999L))
                .thenThrow(new ResourceNotFoundException("Request not found!"));

        // Act & Assert
        mockMvc.perform(get("/api/trips/request/status/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Request not found!"));
    }

    @Test
    @DisplayName("shouldGetTripByRequestId")
    @WithMockUser
    void shouldGetTripByRequestId() throws Exception {
        // Arrange
        when(tripService.getTripByRequestId(1L)).thenReturn(testTrip);

        // Act & Assert
        mockMvc.perform(get("/api/trips/by-request/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.status").value("INITIATED"));

        verify(tripService).getTripByRequestId(1L);
    }

    @Test
    @DisplayName("shouldReturn404WhenTripNotFoundByRequestId")
    @WithMockUser
    void shouldReturn404WhenTripNotFoundByRequestId() throws Exception {
        // Arrange
        when(tripService.getTripByRequestId(999L))
                .thenThrow(new ResourceNotFoundException("Trip not found for requestId: 999"));

        // Act & Assert
        mockMvc.perform(get("/api/trips/by-request/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Trip not found for requestId: 999"));
    }

    @Test
    @DisplayName("shouldGetTripById")
    @WithMockUser
    void shouldGetTripById() throws Exception {
        // Arrange
        when(tripService.getTripById(1L)).thenReturn(testTrip);

        // Act & Assert
        mockMvc.perform(get("/api/trips/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.status").value("INITIATED"));

        verify(tripService).getTripById(1L);
    }

    @Test
    @DisplayName("shouldReturn404WhenTripNotFoundById")
    @WithMockUser
    void shouldReturn404WhenTripNotFoundById() throws Exception {
        // Arrange
        when(tripService.getTripById(999L))
                .thenThrow(new ResourceNotFoundException("Trip not found for id: 999"));

        // Act & Assert
        mockMvc.perform(get("/api/trips/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Trip not found for id: 999"));
    }

    @Test
    @DisplayName("shouldDeleteTripByIdWithAdminRole")
    @WithMockUser(roles = "ADMIN")
    void shouldDeleteTripByIdWithAdminRole() throws Exception {
        // Arrange
        doNothing().when(tripService).deleteTripById(1L);

        // Act & Assert
        mockMvc.perform(delete("/api/trips/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.message").value("Trip deleted successfully"));

        verify(tripService).deleteTripById(1L);
    }

    @Test
    @DisplayName("shouldReturn500WhenDeleteFails")
    @WithMockUser(roles = "ADMIN")
    void shouldReturn500WhenDeleteFails() throws Exception {
        // Arrange
        doThrow(new RuntimeException("Delete failed")).when(tripService).deleteTripById(1L);

        // Act & Assert
        mockMvc.perform(delete("/api/trips/1"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Internal server error: Delete failed"));
    }

    @Test
    @DisplayName("shouldGetAllTrips")
    @WithMockUser
    void shouldGetAllTrips() throws Exception {
        // Arrange
        Trip trip2 = Trip.builder().id(2L).status(TripStatus.COMPLETED).build();
        List<Trip> trips = Arrays.asList(testTrip, trip2);
        when(tripService.getAllTrips()).thenReturn(trips);

        // Act & Assert
        mockMvc.perform(get("/api/trips"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].id").value(1))
                .andExpect(jsonPath("$.data[1].id").value(2));

        verify(tripService).getAllTrips();
    }

    @Test
    @DisplayName("shouldReturnEmptyListWhenNoTrips")
    @WithMockUser
    void shouldReturnEmptyListWhenNoTrips() throws Exception {
        // Arrange
        when(tripService.getAllTrips()).thenReturn(Collections.emptyList());

        // Act & Assert
        mockMvc.perform(get("/api/trips"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(0));
    }

    @Test
    @DisplayName("shouldReturn500WhenGetAllTripsFails")
    @WithMockUser
    void shouldReturn500WhenGetAllTripsFails() throws Exception {
        // Arrange
        when(tripService.getAllTrips()).thenThrow(new RuntimeException("Database error"));

        // Act & Assert
        mockMvc.perform(get("/api/trips"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Internal server error: Database error"));
    }

    @Test
    @DisplayName("shouldGetTripsByUserId")
    @WithMockUser
    void shouldGetTripsByUserId() throws Exception {
        // Arrange
        List<Trip> userTrips = Collections.singletonList(testTrip);
        when(tripService.getTripsByUserId(any())).thenReturn(userTrips);

        // Act & Assert
        mockMvc.perform(get("/api/trips/user"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].id").value(1));

        verify(tripService).getTripsByUserId(any());
    }

    @Test
    @DisplayName("shouldReturn404WhenGetTripsByUserIdFails")
    @WithMockUser
    void shouldReturn404WhenGetTripsByUserIdFails() throws Exception {
        // Arrange
        when(tripService.getTripsByUserId(any()))
                .thenThrow(new ResourceNotFoundException("User not found"));

        // Act & Assert
        mockMvc.perform(get("/api/trips/user"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("User not found"));
    }

    @Test
    @DisplayName("shouldGetPaginatedTrips")
    @WithMockUser
    void shouldGetPaginatedTrips() throws Exception {
        // Arrange
        TripDto tripDto = TripDto.builder().id(1L).status(TripStatus.COMPLETED).build();

        Page<TripDto> tripPage =
                new PageImpl<>(Collections.singletonList(tripDto), PageRequest.of(0, 10), 1);

        when(tripService.getUserTripsPaginated(any(), any())).thenReturn(tripPage);

        // Act & Assert
        mockMvc.perform(get("/api/trips/last").param("page", "0").param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content.length()").value(1))
                .andExpect(jsonPath("$.data.content[0].id").value(1))
                .andExpect(jsonPath("$.data.totalElements").value(1));

        verify(tripService).getUserTripsPaginated(any(), any());
    }

    @Test
    @DisplayName("shouldReturn500WhenGetPaginatedTripsFails")
    @WithMockUser
    void shouldReturn500WhenGetPaginatedTripsFails() throws Exception {
        // Arrange
        when(tripService.getUserTripsPaginated(any(), any()))
                .thenThrow(new RuntimeException("Query error"));

        // Act & Assert
        mockMvc.perform(get("/api/trips/last").param("page", "0").param("size", "10"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Internal server error: Query error"));
    }

    @Test
    @DisplayName("shouldGetTripsByVinNumber")
    @WithMockUser
    void shouldGetTripsByVinNumber() throws Exception {
        // Arrange
        List<Trip> vehicleTrips = Collections.singletonList(testTrip);
        when(tripService.getTripsByVinNumber("VIN123456")).thenReturn(vehicleTrips);

        // Act & Assert
        mockMvc.perform(get("/api/trips/vehicle/VIN123456"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].id").value(1));

        verify(tripService).getTripsByVinNumber("VIN123456");
    }

    @Test
    @DisplayName("shouldReturn404WhenGetTripsByVinNumberFails")
    @WithMockUser
    void shouldReturn404WhenGetTripsByVinNumberFails() throws Exception {
        // Arrange
        when(tripService.getTripsByVinNumber("INVALID_VIN"))
                .thenThrow(new ResourceNotFoundException("Vehicle not found"));

        // Act & Assert
        mockMvc.perform(get("/api/trips/vehicle/INVALID_VIN"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Vehicle not found"));
    }

    @Test
    @DisplayName("shouldDeclineRequestSuccessfully")
    @WithMockUser
    void shouldDeclineRequestSuccessfully() throws Exception {
        // Arrange
        doNothing().when(requestService).declineRequestById(eq(1L), any());

        // Act & Assert
        mockMvc.perform(post("/api/trips/request/1/decline"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.message").value("Request declined successfully"));

        verify(requestService).declineRequestById(eq(1L), any());
    }

    @Test
    @DisplayName("shouldReturn500WhenDeclineRequestFails")
    @WithMockUser
    void shouldReturn500WhenDeclineRequestFails() throws Exception {
        // Arrange
        doThrow(new RuntimeException("Cannot decline"))
                .when(requestService)
                .declineRequestById(eq(1L), any());

        // Act & Assert
        mockMvc.perform(post("/api/trips/request/1/decline"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Internal server error: Cannot decline"));
    }

    @Test
    @DisplayName("shouldReturnForbiddenWhenDeclineRequestAccessDenied")
    @WithMockUser
    void shouldReturnForbiddenWhenDeclineRequestAccessDenied() throws Exception {
        // Arrange
        doThrow(new AccessDeniedException("Unauthorized to decline this request"))
                .when(requestService)
                .declineRequestById(eq(1L), any());

        // Act & Assert
        mockMvc.perform(post("/api/trips/request/1/decline"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Unauthorized to decline this request"));
    }

    @Test
    @DisplayName("shouldAcceptRequestSuccessfully")
    @WithMockUser
    void shouldAcceptRequestSuccessfully() throws Exception {
        // Arrange
        Request acceptedRequest =
                Request.builder()
                        .id(1L)
                        .user(testUser)
                        .status(RequestStatus.ACCEPTED)
                        .createdAt(new Timestamp(System.currentTimeMillis()))
                        .build();
        when(requestService.acceptRequestById(eq(1L), any())).thenReturn(acceptedRequest);

        // Act & Assert
        mockMvc.perform(post("/api/trips/request/1/accept"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.status").value("ACCEPTED"));

        verify(requestService).acceptRequestById(eq(1L), any());
    }

    @Test
    @DisplayName("shouldReturn500WhenAcceptRequestFails")
    @WithMockUser
    void shouldReturn500WhenAcceptRequestFails() throws Exception {
        // Arrange
        when(requestService.acceptRequestById(eq(1L), any()))
                .thenThrow(new RuntimeException("Cannot accept"));

        // Act & Assert
        mockMvc.perform(post("/api/trips/request/1/accept"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Internal server error: Cannot accept"));
    }

    @Test
    @DisplayName("shouldReturnForbiddenWhenAcceptRequestAccessDenied")
    @WithMockUser
    void shouldReturnForbiddenWhenAcceptRequestAccessDenied() throws Exception {
        // Arrange
        when(requestService.acceptRequestById(eq(1L), any()))
                .thenThrow(new AccessDeniedException("Unauthorized to accept this request"));

        // Act & Assert
        mockMvc.perform(post("/api/trips/request/1/accept"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Unauthorized to accept this request"));
    }

    @Test
    @DisplayName("shouldStartTripSuccessfully")
    @WithMockUser
    void shouldStartTripSuccessfully() throws Exception {
        // Arrange
        doNothing().when(tripService).startTrip(1L);

        // Act & Assert
        mockMvc.perform(post("/api/trips/request/1/start"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.message").value("Trip started successfully!"));

        verify(tripService).startTrip(1L);
    }

    @Test
    @DisplayName("shouldReturn500WhenStartTripFails")
    @WithMockUser
    void shouldReturn500WhenStartTripFails() throws Exception {
        // Arrange
        doThrow(new RuntimeException("Trip not ready")).when(tripService).startTrip(1L);

        // Act & Assert
        mockMvc.perform(post("/api/trips/request/1/start"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Internal server error: Trip not ready"));
    }

    @Test
    @DisplayName("shouldEndTripSuccessfully")
    @WithMockUser
    void shouldEndTripSuccessfully() throws Exception {
        // Arrange
        doNothing().when(tripService).endTrip(1L);

        // Act & Assert
        mockMvc.perform(post("/api/trips/request/1/end"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.message").value("Trip ended successfully!"));

        verify(tripService).endTrip(1L);
    }

    @Test
    @DisplayName("shouldReturn500WhenEndTripFails")
    @WithMockUser
    void shouldReturn500WhenEndTripFails() throws Exception {
        // Arrange
        doThrow(new RuntimeException("Trip not in progress")).when(tripService).endTrip(1L);

        // Act & Assert
        mockMvc.perform(post("/api/trips/request/1/end"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(
                        jsonPath("$.message").value("Internal server error: Trip not in progress"));
    }
}

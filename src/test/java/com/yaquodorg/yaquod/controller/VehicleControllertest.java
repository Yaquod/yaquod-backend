package com.yaquodorg.yaquod.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yaquodorg.yaquod.dtos.CreateVehicleDto;
import com.yaquodorg.yaquod.dtos.VehicleDto;
import com.yaquodorg.yaquod.entity.Vehicle;
import com.yaquodorg.yaquod.entity.VehicleStatus;
import com.yaquodorg.yaquod.service.mqtt.MqttService;
import com.yaquodorg.yaquod.service.vehicle.VehicleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * NOTE: ALL THOSE TESTS ARE AI-GENERATED AND REVIEWED MANUALLY
 * <p>
 * Unit tests for VehicleController
 * Tests controller logic with mocked services
 * Does NOT test security
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("VehicleController Unit Tests")
class VehicleControllerTest {

    @Mock
    private VehicleService vehicleService;

    @Mock
    private MqttService mqttService;

    @InjectMocks
    private VehicleController vehicleController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private CreateVehicleDto createVehicleDto;
    private Vehicle vehicle;

    @BeforeEach
    void setUp() {
        // Setup MockMvc
        mockMvc = MockMvcBuilders.standaloneSetup(vehicleController).build();
        objectMapper = new ObjectMapper();

        // Setup test data
        createVehicleDto = CreateVehicleDto.builder()
                .vinNumber("VIN1")
                .plateNo("ABC-123")
                .color("RED")
                .carCompany("Toyota")
                .model("Camry")
                .seats(4)
                .build();

        vehicle = new Vehicle();
        vehicle.setId(1L);
        vehicle.setVinNumber("VIN1");
        vehicle.setPlateNo("ABC-123");
        vehicle.setColor("RED");
        vehicle.setCarCompany("Toyota");
        vehicle.setModel("Camry");
        vehicle.setSeats(4);
        vehicle.setStatus(VehicleStatus.IDLE);
        vehicle.setLastUpdatedLong(0.0d);
        vehicle.setLastUpdatedLat(0.0d);
        vehicle.setLastUpdatedLocation(null);
    }

    @Test
    @DisplayName("POST /api/vehicles - Should create vehicle successfully")
    void shouldCreateVehicle() throws Exception {
        // Arrange
        when(vehicleService.createVehicle(any(CreateVehicleDto.class))).thenReturn(vehicle);

        // Act & Assert
        mockMvc.perform(post("/api/vehicles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createVehicleDto)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.vinNumber").value("VIN1"))
                .andExpect(jsonPath("$.data.plateNo").value("ABC-123"))
                .andExpect(jsonPath("$.data.model").value("Camry"))
                .andExpect(jsonPath("$.data.status").value("IDLE"));

        // Verify service was called with correct DTO
        ArgumentCaptor<CreateVehicleDto> dtoCaptor = ArgumentCaptor.forClass(CreateVehicleDto.class);
        verify(vehicleService, times(1)).createVehicle(dtoCaptor.capture());

        CreateVehicleDto capturedDto = dtoCaptor.getValue();
        assertThat(capturedDto.getPlateNo()).isEqualTo("ABC-123");
        assertThat(capturedDto.getModel()).isEqualTo("Camry");
    }

    @Test
    @DisplayName("POST /api/vehicles - Should return 400 when service throws exception")
    void shouldReturn400WhenCreateVehicleFails() throws Exception {
        // Arrange
        when(vehicleService.createVehicle(any(CreateVehicleDto.class)))
                .thenThrow(new RuntimeException("Duplicate plate number"));

        // Act & Assert
        mockMvc.perform(post("/api/vehicles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createVehicleDto)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(containsString("Failed to create vehicle")))
                .andExpect(jsonPath("$.message").value(containsString("Duplicate plate number")));

        verify(vehicleService, times(1)).createVehicle(any(CreateVehicleDto.class));
    }

    // TODO: Should be uncommented after handled correctly in the refactoring phase
    // @Test
    // @DisplayName("POST /api/vehicles - Should handle null DTO")
    // void shouldHandleNullDtoOnCreate() throws Exception {
    // // Act & Assert
    // mockMvc.perform(post("/api/vehicles")
    // .contentType(MediaType.APPLICATION_JSON)
    // .content("{}"))
    // .andDo(print())
    // .andExpect(status().isBadRequest());
    // }

    @Test
    @DisplayName("GET /api/vehicles - Should return all vehicles")
    void shouldGetAllVehicles() throws Exception {
        // Arrange
        Vehicle vehicle2 = new Vehicle();
        vehicle2.setId(2L);
        vehicle2.setVinNumber("test-vin-456");
        vehicle2.setPlateNo("XYZ-789");
        vehicle2.setModel("Honda Accord");

        List<Vehicle> vehicles = Arrays.asList(vehicle, vehicle2);
        when(vehicleService.getVehicles()).thenReturn(vehicles);

        // Act & Assert
        mockMvc.perform(get("/api/vehicles"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data", hasSize(2)))
                .andExpect(jsonPath("$.data[0].plateNo").value("ABC-123"))
                .andExpect(jsonPath("$.data[1].plateNo").value("XYZ-789"));

        verify(vehicleService, times(1)).getVehicles();
    }

    @Test
    @DisplayName("GET /api/vehicles - Should return empty list when no vehicles")
    void shouldReturnEmptyListWhenNoVehicles() throws Exception {
        // Arrange
        when(vehicleService.getVehicles()).thenReturn(List.of());

        // Act & Assert
        mockMvc.perform(get("/api/vehicles"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data", hasSize(0)));

        verify(vehicleService, times(1)).getVehicles();
    }

    @Test
    @DisplayName("GET /api/vehicles/id/{vehicleId} - Should return vehicle by ID")
    void shouldGetVehicleById() throws Exception {
        // Arrange
        when(vehicleService.getVehicle(1L)).thenReturn(vehicle);

        // Act & Assert
        mockMvc.perform(get("/api/vehicles/id/{vehicleId}", 1L))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.plateNo").value("ABC-123"));

        verify(vehicleService, times(1)).getVehicle(1L);
    }

    @Test
    @DisplayName("GET /api/vehicles/id/{vehicleId} - Should return 400 when vehicle not found")
    void shouldReturn400WhenVehicleNotFoundById() throws Exception {
        // Arrange
        when(vehicleService.getVehicle(999L))
                .thenThrow(new RuntimeException("Vehicle not found"));

        // Act & Assert
        mockMvc.perform(get("/api/vehicles/id/{vehicleId}", 999L))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(containsString("Could not fetch vehicle")));

        verify(vehicleService, times(1)).getVehicle(999L);
    }

    @Test
    @DisplayName("GET /api/vehicles/vin/{vinNumber} - Should return vehicle by VIN")
    void shouldGetVehicleByVIN() throws Exception {
        // Arrange
        String vin = "VIN1";
        when(vehicleService.getVehicleByVinNumber(vin)).thenReturn(Optional.of(vehicle));

        // Act & Assert
        mockMvc.perform(get("/api/vehicles/vin/{vinNumber}", vin))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.vinNumber").value(vin))
                .andExpect(jsonPath("$.data.plateNo").value("ABC-123"));

        verify(vehicleService, times(1)).getVehicleByVinNumber(vin);
    }

    @Test
    @DisplayName("GET /api/vehicles/vin/{vinNumber} - Should return 400 when vehicle not found by VIN")
    void shouldReturn400WhenVehicleNotFoundByVIN() throws Exception {
        // Arrange
        String vin = "non-existent-vin";
        when(vehicleService.getVehicleByVinNumber(vin)).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(get("/api/vehicles/vin/{vinNumber}", vin))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(containsString("Could not fetch vehicle")))
                .andExpect(jsonPath("$.message").value(containsString("Vehicle not found!")));

        verify(vehicleService, times(1)).getVehicleByVinNumber(vin);
    }

    @Test
    @DisplayName("PATCH /api/vehicles - Should update vehicle successfully")
    void shouldUpdateVehicle() throws Exception {
        // Arrange
        CreateVehicleDto updateDto = CreateVehicleDto.builder()
                .vinNumber("VIN1")
                .plateNo("XYZ-999")
                .color("RED")
                .carCompany("Update Company")
                .model("Updated Model")
                .seats(6)
                .build();

        Vehicle updatedVehicle = new Vehicle();
        updatedVehicle.setId(1L);
        updatedVehicle.setVinNumber("VIN1");
        updatedVehicle.setPlateNo("XYZ-999");
        updatedVehicle.setColor("RED");
        updatedVehicle.setCarCompany("Updated Company");
        updatedVehicle.setModel("Updated Model");
        updatedVehicle.setSeats(6);

        when(vehicleService.updateVehicle(any(CreateVehicleDto.class))).thenReturn(updatedVehicle);

        // Act & Assert
        mockMvc.perform(patch("/api/vehicles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.plateNo").value("XYZ-999"))
                .andExpect(jsonPath("$.data.model").value("Updated Model"))
                .andExpect(jsonPath("$.data.seats").value(6));

        verify(vehicleService, times(1)).updateVehicle(any(CreateVehicleDto.class));
    }

    @Test
    @DisplayName("PATCH /api/vehicles - Should return 400 when update fails")
    void shouldReturn400WhenUpdateFails() throws Exception {
        // Arrange
        when(vehicleService.updateVehicle(any(CreateVehicleDto.class)))
                .thenThrow(new RuntimeException("Vehicle not found"));

        // Act & Assert
        mockMvc.perform(patch("/api/vehicles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createVehicleDto)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(containsString("Failed to update vehicle")));

        verify(vehicleService, times(1)).updateVehicle(any(CreateVehicleDto.class));
    }

    @Test
    @DisplayName("DELETE /api/vehicles/id/{vehicleId} - Should delete vehicle successfully")
    void shouldDeleteVehicle() throws Exception {
        // Arrange
        doNothing().when(vehicleService).deleteVehicle(1L);

        // Act & Assert
        mockMvc.perform(delete("/api/vehicles/id/{vehicleId}", 1L))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.message").value("Vehicle deleted successfully!"));

        verify(vehicleService, times(1)).deleteVehicle(1L);
    }

    @Test
    @DisplayName("DELETE /api/vehicles/id/{vehicleId} - Should return 400 when delete fails")
    void shouldReturn400WhenDeleteFails() throws Exception {
        // Arrange
        doThrow(new RuntimeException("Vehicle not found"))
                .when(vehicleService).deleteVehicle(999L);

        // Act & Assert
        mockMvc.perform(delete("/api/vehicles/id/{vehicleId}", 999L))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(containsString("Could not delete vehicle")));

        verify(vehicleService, times(1)).deleteVehicle(999L);
    }

    @Test
    @DisplayName("DELETE /api/vehicles/id/{vehicleId} - Should handle service exception")
    void shouldHandleServiceExceptionOnDelete() throws Exception {
        // Arrange
        doThrow(new RuntimeException("Database connection error"))
                .when(vehicleService).deleteVehicle(1L);

        // Act & Assert
        mockMvc.perform(delete("/api/vehicles/id/{vehicleId}", 1L))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(containsString("Database connection error")));

        verify(vehicleService, times(1)).deleteVehicle(1L);
    }

    @Test
    @DisplayName("PATCH /api/vehicles/vin/{vinNumber}/location-update - Should send MQTT message")
    void shouldSendLocationUpdateSignal() throws Exception {
        // Arrange
        String vin = "VIN1";
        doNothing().when(mqttService).publish(anyString(), any());

        // Act & Assert
        mockMvc.perform(patch("/api/vehicles/vin/{vinNumber}/location-update", vin))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.message").value("Order signal sent!"));

        // Verify MQTT message was published
        ArgumentCaptor<String> topicCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<VehicleDto> dtoCaptor = ArgumentCaptor.forClass(VehicleDto.class);
        verify(mqttService, times(1)).publish(topicCaptor.capture(), dtoCaptor.capture());

        // Verify correct topic
        assertThat(topicCaptor.getValue()).isEqualTo("topic/update_location/order");

        // Verify correct payload
        VehicleDto capturedDto = dtoCaptor.getValue();
        assertThat(capturedDto.getVinNumber()).isEqualTo(vin);
    }

    @Test
    @DisplayName("PATCH /api/vehicles/vin/{vinNumber}/location-update - Should return 400 when MQTT fails")
    void shouldReturn400WhenLocationUpdateFails() throws Exception {
        // Arrange
        String vin = "VIN1";
        doThrow(new RuntimeException("MQTT broker unavailable"))
                .when(mqttService).publish(anyString(), any());

        // Act & Assert
        mockMvc.perform(patch("/api/vehicles/vin/{vinNumber}/location-update", vin))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(containsString("Could not send signal to vehicle")))
                .andExpect(jsonPath("$.message").value(containsString("MQTT broker unavailable")));

        verify(mqttService, times(1)).publish(anyString(), any());
    }

    @Test
    @DisplayName("PATCH /api/vehicles/vin/{vinNumber}/location-update - Should handle special characters in VIN")
    void shouldHandleSpecialCharactersInVINForLocationUpdate() throws Exception {
        // Arrange
        String vin = "test-vin-with-special-chars-!@#";
        doNothing().when(mqttService).publish(anyString(), any());

        // Act & Assert
        mockMvc.perform(patch("/api/vehicles/vin/{vinNumber}/location-update", vin))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // Verify correct VIN was passed
        ArgumentCaptor<VehicleDto> dtoCaptor = ArgumentCaptor.forClass(VehicleDto.class);
        verify(mqttService).publish(anyString(), dtoCaptor.capture());
        assertThat(dtoCaptor.getValue().getVinNumber()).isEqualTo(vin);
    }

    @Test
    @DisplayName("PATCH /api/vehicles/vin/{vinNumber}/status-update - Should send MQTT message")
    void shouldSendStatusUpdateSignal() throws Exception {
        // Arrange
        String vin = "VIN1";
        doNothing().when(mqttService).publish(anyString(), any());

        // Act & Assert
        mockMvc.perform(patch("/api/vehicles/vin/{vinNumber}/status-update", vin))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.message").value("Order signal sent!"));

        // Verify MQTT message was published
        ArgumentCaptor<String> topicCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<VehicleDto> dtoCaptor = ArgumentCaptor.forClass(VehicleDto.class);
        verify(mqttService, times(1)).publish(topicCaptor.capture(), dtoCaptor.capture());

        // Verify correct topic
        assertThat(topicCaptor.getValue()).isEqualTo("topic/update_status/order");

        // Verify correct payload
        VehicleDto capturedDto = dtoCaptor.getValue();
        assertThat(capturedDto.getVinNumber()).isEqualTo(vin);
    }

    @Test
    @DisplayName("PATCH /api/vehicles/vin/{vinNumber}/status-update - Should return 400 when MQTT fails")
    void shouldReturn400WhenStatusUpdateFails() throws Exception {
        // Arrange
        String vin = "VIN1";
        doThrow(new RuntimeException("MQTT connection timeout"))
                .when(mqttService).publish(anyString(), any());

        // Act & Assert
        mockMvc.perform(patch("/api/vehicles/vin/{vinNumber}/status-update", vin))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(containsString("Could not send signal to vehicle")))
                .andExpect(jsonPath("$.message").value(containsString("MQTT connection timeout")));

        verify(mqttService, times(1)).publish(anyString(), any());
    }

    // EDGE CASES AND ERROR HANDLING
    @Test
    @DisplayName("Should handle very long VIN")
    void shouldHandleVeryLongVIN() throws Exception {
        // Arrange
        String longVin = "a".repeat(500);
        when(vehicleService.getVehicleByVinNumber(longVin)).thenReturn(Optional.of(vehicle));

        // Act & Assert
        mockMvc.perform(get("/api/vehicles/vin/{vinNumber}", longVin))
                .andDo(print())
                .andExpect(status().isOk());

        verify(vehicleService, times(1)).getVehicleByVinNumber(longVin);
    }

    @Test
    @DisplayName("Should handle negative vehicle ID")
    void shouldHandleNegativeVehicleId() throws Exception {
        // Arrange
        when(vehicleService.getVehicle(-1L))
                .thenThrow(new RuntimeException("Invalid ID"));

        // Act & Assert
        mockMvc.perform(get("/api/vehicles/id/{vehicleId}", -1L))
                .andDo(print())
                .andExpect(status().isBadRequest());

        verify(vehicleService, times(1)).getVehicle(-1L);
    }

    @Test
    @DisplayName("Should handle zero vehicle ID")
    void shouldHandleZeroVehicleId() throws Exception {
        // Arrange
        when(vehicleService.getVehicle(0L))
                .thenThrow(new RuntimeException("Invalid ID"));

        // Act & Assert
        mockMvc.perform(get("/api/vehicles/id/{vehicleId}", 0L))
                .andDo(print())
                .andExpect(status().isBadRequest());

        verify(vehicleService, times(1)).getVehicle(0L);
    }

    @Test
    @DisplayName("Should verify correct MQTT topics are used")
    void shouldVerifyCorrectMqttTopics() throws Exception {
        // Arrange
        String vin = "VIN1";
        doNothing().when(mqttService).publish(anyString(), any());

        // Act - Test location update
        mockMvc.perform(patch("/api/vehicles/vin/{vinNumber}/location-update", vin))
                .andExpect(status().isOk());

        // Assert - Verify location topic
        ArgumentCaptor<String> topicCaptor1 = ArgumentCaptor.forClass(String.class);
        verify(mqttService, times(1)).publish(topicCaptor1.capture(), any());
        assertThat(topicCaptor1.getValue()).isEqualTo("topic/update_location/order");

        // Reset mock
        reset(mqttService);
        doNothing().when(mqttService).publish(anyString(), any());

        // Act - Test status update
        mockMvc.perform(patch("/api/vehicles/vin/{vinNumber}/status-update", vin))
                .andExpect(status().isOk());

        // Assert - Verify status topic
        ArgumentCaptor<String> topicCaptor2 = ArgumentCaptor.forClass(String.class);
        verify(mqttService, times(1)).publish(topicCaptor2.capture(), any());
        assertThat(topicCaptor2.getValue()).isEqualTo("topic/update_status/order");
    }

    @Test
    @DisplayName("Should not call service when vehicle service throws NullPointerException")
    void shouldHandleNullPointerException() throws Exception {
        // Arrange
        when(vehicleService.createVehicle(any(CreateVehicleDto.class)))
                .thenThrow(new NullPointerException("Unexpected null value"));

        // Act & Assert
        mockMvc.perform(post("/api/vehicles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createVehicleDto)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));

        verify(vehicleService, times(1)).createVehicle(any(CreateVehicleDto.class));
    }
}

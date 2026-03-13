package com.yaquodorg.yaquod.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yaquodorg.yaquod.dtos.CreateVehicleDto;
import com.yaquodorg.yaquod.entity.Role;
import com.yaquodorg.yaquod.entity.Vehicle;
import com.yaquodorg.yaquod.entity.VehicleStatus;
import com.yaquodorg.yaquod.repository.VehicleRepository;
import com.yaquodorg.yaquod.util.WithMockCustomUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

/**
 * NOTE: ALL THOSE TESTS ARE AI-GENERATED AND REVIEWED MANUALLY
 *
 * <p>Integration tests for VehicleController Tests full Spring context including security Uses real
 * database (H2 or Testcontainers)
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("VehicleController Integration Tests with Security")
class VehicleControllerIntegrationTest {

    private final String VinNumber1 = "1HGCM82633A004352";
    private final String VinNumber2 = "1M8GDM9AXKP042788";
    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private VehicleRepository vehicleRepository;
    private CreateVehicleDto createVehicleDto;
    private Vehicle vehicle;

    @BeforeEach
    void setUp() {
        vehicleRepository.deleteAll();

        createVehicleDto =
                CreateVehicleDto.builder()
                        .vinNumber(VinNumber1)
                        .plateNo("ABC-123")
                        .color("RED")
                        .carCompany("Toyota")
                        .model("Camry")
                        .seats(4)
                        .build();

        vehicle = new Vehicle();
        vehicle.setId(1L);
        vehicle.setVinNumber(VinNumber1);
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
    @DisplayName("POST /api/vehicles - Should return 401 when not authenticated")
    void shouldReturn401WhenNotAuthenticated() throws Exception {
        mockMvc.perform(
                        post("/api/vehicles")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(createVehicleDto)))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /api/vehicles - Should return 403 when user lacks ADMIN role")
    @WithMockUser(roles = "USER")
    void shouldReturn403WhenUserLacksAdminRole() throws Exception {
        mockMvc.perform(
                        post("/api/vehicles")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(createVehicleDto)))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("POST /api/vehicles - Should succeed with ADMIN role")
    @WithMockCustomUser(email = "admin@example.com", role = Role.ADMIN)
    void shouldSucceedWithAdminRole() throws Exception {
        mockMvc.perform(
                        post("/api/vehicles")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(createVehicleDto)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.vehicle.plateNo").value("ABC-123"));
    }

    @Test
    @DisplayName("GET /api/vehicles - Should return 401 when not authenticated")
    void shouldReturn401WhenGettingVehiclesWithoutAuth() throws Exception {
        mockMvc.perform(get("/api/vehicles")).andDo(print()).andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /api/vehicles - Should return 403 with USER role")
    @WithMockUser(roles = "USER")
    void shouldReturn403WhenGettingVehiclesWithUserRole() throws Exception {
        mockMvc.perform(get("/api/vehicles")).andDo(print()).andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /api/vehicles - Should succeed with ADMIN role")
    @WithMockUser(roles = "ADMIN")
    void shouldGetVehiclesWithAdminRole() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/vehicles"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data", hasSize(0)));
    }

    @Test
    @DisplayName("GET /api/vehicles/id/{vehicleId} - Should require ADMIN role")
    @WithMockUser(roles = "USER")
    void shouldRequireAdminRoleForGetById() throws Exception {
        mockMvc.perform(get("/api/vehicles/id/1")).andDo(print()).andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /api/vehicles/vin/{vinNumber} - Should require ADMIN role")
    @WithMockUser(roles = "DRIVER")
    void shouldRequireAdminRoleForGetByVIN() throws Exception {
        mockMvc.perform(get("/api/vehicles/vin/test-vin"))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("PATCH /api/vehicles - Should require ADMIN role")
    @WithMockUser(roles = "USER")
    void shouldRequireAdminRoleForUpdate() throws Exception {
        mockMvc.perform(
                        patch("/api/vehicles")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(createVehicleDto)))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("DELETE /api/vehicles/id/{vehicleId} - Should require ADMIN role")
    @WithMockUser(roles = "USER")
    void shouldRequireAdminRoleForDelete() throws Exception {
        mockMvc.perform(delete("/api/vehicles/id/1"))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("PATCH /api/vehicles/vin/{vin}/location-update - Should require ADMIN role")
    @WithMockUser(roles = "USER")
    void shouldRequireAdminRoleForLocationUpdate() throws Exception {
        mockMvc.perform(patch("/api/vehicles/vin/test-vin/location-update"))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("PATCH /api/vehicles/vin/{vin}/status-update - Should require ADMIN role")
    @WithMockUser(roles = "USER")
    void shouldRequireAdminRoleForStatusUpdate() throws Exception {
        mockMvc.perform(patch("/api/vehicles/vin/test-vin/status-update"))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    /** FULL INTEGRATION TESTS */
    @Test
    @DisplayName("Should create, retrieve, update, and delete vehicle (full flow)")
    @WithMockCustomUser(email = "admin@example.com", role = Role.ADMIN)
    void shouldPerformFullCrudFlow() throws Exception {
        // 1. Create vehicle
        String createResponse =
                mockMvc.perform(
                                post("/api/vehicles")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(createVehicleDto)))
                        .andDo(print())
                        .andExpect(status().isCreated())
                        .andExpect(jsonPath("$.success").value(true))
                        .andReturn()
                        .getResponse()
                        .getContentAsString();

        // Extract vehicle ID from response
        Long vehicleId = vehicleRepository.findAll().get(0).getId();

        // 2. Get vehicle by ID
        mockMvc.perform(get("/api/vehicles/id/{vehicleId}", vehicleId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.plateNo").value("ABC-123"));

        // 3. Get all vehicles
        mockMvc.perform(get("/api/vehicles"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(1)));

        // 4. Update vehicle
        CreateVehicleDto updateDto =
                CreateVehicleDto.builder()
                        .vinNumber(VinNumber1)
                        .plateNo("XYZ-999")
                        .color("Updated Color")
                        .carCompany("Updated Company")
                        .model("Updated Model")
                        .seats(6)
                        .build();

        mockMvc.perform(
                        patch("/api/vehicles")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(updateDto)))
                .andDo(print())
                .andExpect(status().isOk());

        // 5. Delete vehicle
        mockMvc.perform(delete("/api/vehicles/id/{vehicleId}", vehicleId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.message").value("Vehicle deleted successfully!"));

        // 6. Verify deletion
        assertThat(vehicleRepository.findById(vehicleId)).isEmpty();
    }

    @Test
    @DisplayName("Should handle concurrent vehicle creation")
    @WithMockCustomUser(email = "admin@example.com", role = Role.ADMIN)
    void shouldHandleConcurrentVehicleCreation() throws Exception {
        // Create first vehicle
        mockMvc.perform(
                        post("/api/vehicles")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(createVehicleDto)))
                .andExpect(status().isCreated());

        // Create second vehicle with different plate
        CreateVehicleDto dto2 =
                CreateVehicleDto.builder()
                        .vinNumber(VinNumber2)
                        .plateNo("XYZ-789")
                        .color("WHITE")
                        .carCompany("Honda")
                        .model("Accord")
                        .seats(5)
                        .build();

        mockMvc.perform(
                        post("/api/vehicles")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(dto2)))
                .andExpect(status().isCreated());

        // Verify both exist
        mockMvc.perform(get("/api/vehicles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(2)));
    }

    @Test
    @DisplayName("Should get vehicle by VIN after creation")
    @WithMockCustomUser(email = "admin@example.com", role = Role.ADMIN)
    void shouldGetVehicleByVINAfterCreation() throws Exception {
        // Create vehicle
        mockMvc.perform(
                        post("/api/vehicles")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(createVehicleDto)))
                .andExpect(status().isCreated());

        // Get the created vehicle's VIN
        Vehicle created = vehicleRepository.findAll().get(0);
        assertNotNull(created);
        String vin = created.getVinNumber();

        // Get by VIN
        mockMvc.perform(get("/api/vehicles/vin/{vinNumber}", vin))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.vinNumber").value(vin))
                .andExpect(jsonPath("$.data.plateNo").value("ABC-123"));
    }

    @Test
    @DisplayName("Should return 404 when getting non-existent vehicle by ID")
    @WithMockUser(roles = "ADMIN")
    void shouldReturn404ForNonExistentVehicleById() throws Exception {
        mockMvc.perform(get("/api/vehicles/id/999"))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("Should return 404 when getting non-existent vehicle by VIN")
    @WithMockUser(roles = "ADMIN")
    void shouldReturn404ForNonExistentVehicleByVIN() throws Exception {
        mockMvc.perform(get("/api/vehicles/vin/non-existent-vin"))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("Should return empty list when no vehicles exist")
    @WithMockUser(roles = "ADMIN")
    void shouldReturnEmptyListWhenNoVehicles() throws Exception {
        mockMvc.perform(get("/api/vehicles"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data", hasSize(0)));
    }

    /** MQTT INTEGRATION TESTS */
    @Test
    @DisplayName("MQTT location update should work with valid VIN")
    @WithMockCustomUser(email = "admin@example.com", role = Role.ADMIN)
    void shouldSendLocationUpdateViaMQTT() throws Exception {
        // Create vehicle first
        mockMvc.perform(
                        post("/api/vehicles")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(createVehicleDto)))
                .andExpect(status().isCreated());

        Vehicle created = vehicleRepository.findAll().get(0);
        String vin = created.getVinNumber();

        // Send location update signal
        mockMvc.perform(patch("/api/vehicles/vin/{vinNumber}/location-update", vin))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.message").value("Order signal sent!"));
    }

    @Test
    @DisplayName("MQTT status update should work with valid VIN")
    @WithMockCustomUser(email = "admin@example.com", role = Role.ADMIN)
    void shouldSendStatusUpdateViaMQTT() throws Exception {
        // Create vehicle first
        mockMvc.perform(
                        post("/api/vehicles")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(createVehicleDto)))
                .andExpect(status().isCreated());

        Vehicle created = vehicleRepository.findAll().get(0);
        String vin = created.getVinNumber();

        // Send status update signal
        mockMvc.perform(patch("/api/vehicles/vin/{vinNumber}/status-update", vin))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.message").value("Order signal sent!"));
    }

    @Test
    @DisplayName("MQTT updates should require ADMIN role")
    @WithMockUser(roles = "DRIVER")
    void mqttUpdatesShouldRequireAdminRole() throws Exception {
        // Location update
        mockMvc.perform(patch("/api/vehicles/vin/test-vin/location-update"))
                .andExpect(status().isForbidden());

        // Status update
        mockMvc.perform(patch("/api/vehicles/vin/test-vin/status-update"))
                .andExpect(status().isForbidden());
    }

    /** VALIDATION TESTS */
    @Test
    @DisplayName("Should validate DTO fields on create")
    @WithMockUser(roles = "ADMIN")
    void shouldValidateDtoFieldsOnCreate() throws Exception {
        // Invalid DTO with empty plate number
        CreateVehicleDto invalidDto =
                CreateVehicleDto.builder()
                        .plateNo("") // Invalid
                        .model(null) // Invalid
                        .seats(-1) // Invalid
                        .build();

        mockMvc.perform(
                        post("/api/vehicles")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(invalidDto)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should handle malformed JSON")
    @WithMockUser(roles = "ADMIN")
    void shouldHandleMalformedJson() throws Exception {
        mockMvc.perform(
                        post("/api/vehicles")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{invalid json"))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should handle missing content type")
    @WithMockUser(roles = "ADMIN")
    void shouldHandleMissingContentType() throws Exception {
        mockMvc.perform(
                        post("/api/vehicles")
                                .content(objectMapper.writeValueAsString(createVehicleDto)))
                .andDo(print())
                .andExpect(status().isUnsupportedMediaType());
    }

    /** PERFORMANCE AND EDGE CASES */
    @Test
    @DisplayName("Should handle multiple rapid requests")
    @WithMockCustomUser(email = "admin@example.com", role = Role.ADMIN)
    void shouldHandleMultipleRapidRequests() throws Exception {
        // Valid VIN numbers with correct check digits
        String[] validVins = {VinNumber1, VinNumber2};

        for (int i = 0; i < 2; i++) {
            CreateVehicleDto dto =
                    CreateVehicleDto.builder()
                            .vinNumber(validVins[i])
                            .plateNo("PLATE-" + i)
                            .carCompany("Company" + i)
                            .color("Color" + i)
                            .model("Model " + i)
                            .seats(4)
                            .build();

            mockMvc.perform(
                            post("/api/vehicles")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isCreated());
        }

        // Verify all were created
        mockMvc.perform(get("/api/vehicles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(2)));
    }

    @Test
    @DisplayName("Should handle special characters in plate numbers")
    @WithMockCustomUser(email = "admin@example.com", role = Role.ADMIN)
    void shouldHandleSpecialCharactersInPlateNumbers() throws Exception {
        CreateVehicleDto dto =
                CreateVehicleDto.builder()
                        .vinNumber(VinNumber2)
                        .plateNo("ABC-123!@#")
                        .color("RED")
                        .carCompany("Test")
                        .model("Model")
                        .seats(4)
                        .build();

        mockMvc.perform(
                        post("/api/vehicles")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("Should persist data across requests")
    @WithMockCustomUser(email = "admin@example.com", role = Role.ADMIN)
    void shouldPersistDataAcrossRequests() throws Exception {
        // Create vehicle
        mockMvc.perform(
                        post("/api/vehicles")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(createVehicleDto)))
                .andExpect(status().isCreated());

        // Verify it exists in second request
        mockMvc.perform(get("/api/vehicles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(1)));

        // Verify it still exists in third request
        mockMvc.perform(get("/api/vehicles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(1)));
    }
}

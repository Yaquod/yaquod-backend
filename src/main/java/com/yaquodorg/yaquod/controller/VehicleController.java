package com.yaquodorg.yaquod.controller;

import com.yaquodorg.yaquod.dtos.CreateVehicleDto;
import com.yaquodorg.yaquod.dtos.VehicleDto;
import com.yaquodorg.yaquod.entity.Vehicle;
import com.yaquodorg.yaquod.response.ApiResponse;
import com.yaquodorg.yaquod.response.MessageResponse;
import com.yaquodorg.yaquod.service.mqtt.MqttService;
import com.yaquodorg.yaquod.service.vehicle.VehicleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.yaquodorg.yaquod.response.ApiResponse.createFailureResponse;
import static com.yaquodorg.yaquod.response.ApiResponse.createSuccessResponse;
import static org.springframework.http.HttpStatus.CREATED;

@RestController
@RequestMapping("/api/vehicles")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Vehicles", description = "Vehicle management APIs")
public class VehicleController {

    private static final String TOPIC_ORDER_UPDATE_LOCATION = "topic/update_location/order";
    private static final String TOPIC_ORDER_UPDATE_STATUS = "topic/update_status/order";
    private final VehicleService vehicleService;
    private final MqttService mqttService;

    @Operation(summary = "Create a new vehicle", description = "Creates a new vehicle in the system. Requires ADMIN role.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Vehicle created successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid vehicle data or creation failed"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied - requires ADMIN role")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<ApiResponse<Vehicle>> createVehicle(@Valid @RequestBody CreateVehicleDto createVehicleDto) {
        try {
            Vehicle vehicle = vehicleService.createVehicle(createVehicleDto);
            return ResponseEntity.status(CREATED)
                    .body(createSuccessResponse(vehicle));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(createFailureResponse("Failed to create vehicle: " + e.getMessage()));
        }
    }

    @Operation(summary = "Get all vehicles", description = "Retrieves a list of all vehicles in the system. Requires ADMIN role.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Vehicles retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied - requires ADMIN role")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<ApiResponse<List<Vehicle>>> getVehicles() {
        return ResponseEntity.ok(createSuccessResponse(vehicleService.getVehicles()));
    }

    @Operation(summary = "Get vehicle by ID", description = "Retrieves a specific vehicle by its database ID. Requires ADMIN role.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Vehicle retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Vehicle not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied - requires ADMIN role")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/id/{vehicleId}")
    public ResponseEntity<ApiResponse<Vehicle>> getVehicle(
            @Parameter(description = "The unique database ID of the vehicle", required = true)
            @PathVariable Long vehicleId) {
        try {
            Vehicle vehicle = vehicleService.getVehicle(vehicleId);
            return ResponseEntity.ok(createSuccessResponse(vehicle));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(createFailureResponse("Could not fetch vehicle: " + e.getMessage()));
        }
    }

    @Operation(summary = "Get vehicle by VIN", description = "Retrieves a specific vehicle by its Vehicle Identification Number (VIN). Requires ADMIN role.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Vehicle retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Vehicle not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied - requires ADMIN role")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/vin/{vinNumber}")
    public ResponseEntity<ApiResponse<Vehicle>> getVehicle(
            @Parameter(description = "The Vehicle Identification Number (VIN)", required = true, example = "1HGBH41JXMN109186")
            @PathVariable String vinNumber) {
        try {
            Vehicle vehicle = vehicleService.getVehicleByVinNumber(vinNumber)
                    .orElseThrow(() -> new RuntimeException("Vehicle not found!"));
            return ResponseEntity.ok(createSuccessResponse(vehicle));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(createFailureResponse("Could not fetch vehicle: " + e.getMessage()));
        }
    }

    @Operation(summary = "Update a vehicle", description = "Updates an existing vehicle with the provided data. Requires ADMIN role.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Vehicle updated successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid vehicle data or update failed"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied - requires ADMIN role")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping
    public ResponseEntity<ApiResponse<Vehicle>> updateVehicle(@Valid @RequestBody CreateVehicleDto createVehicleDto) {
        try {
            Vehicle vehicle = vehicleService.updateVehicle(createVehicleDto);
            return ResponseEntity.ok(createSuccessResponse(vehicle));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(createFailureResponse("Failed to update vehicle: " + e.getMessage()));
        }
    }

    @Operation(summary = "Delete a vehicle", description = "Deletes a vehicle from the system by its database ID. Requires ADMIN role.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Vehicle deleted successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Vehicle not found or deletion failed"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied - requires ADMIN role")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/id/{vehicleId}")
    public ResponseEntity<ApiResponse<MessageResponse>> deleteVehicle(
            @Parameter(description = "The unique database ID of the vehicle to delete", required = true)
            @PathVariable Long vehicleId) {
        try {
            vehicleService.deleteVehicle(vehicleId);
            return ResponseEntity.ok(createSuccessResponse(new MessageResponse("Vehicle deleted successfully!")));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(createFailureResponse("Could not delete vehicle: " + e.getMessage()));

        }
    }

    @Operation(summary = "Request vehicle location update", description = "Sends an MQTT signal to request a location update from the vehicle. Requires ADMIN role.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Location update signal sent successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Failed to send signal to vehicle"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied - requires ADMIN role")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/vin/{vinNumber}/location-update")
    public ResponseEntity<ApiResponse<MessageResponse>> updateVehicleLocation(
            @Parameter(description = "The Vehicle Identification Number (VIN)", required = true, example = "1HGBH41JXMN109186")
            @PathVariable String vinNumber) {
        try {
            mqttService.publish(TOPIC_ORDER_UPDATE_LOCATION, new VehicleDto(vinNumber));

            return ResponseEntity.ok(createSuccessResponse(new MessageResponse("Order signal sent!")));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(createFailureResponse("Could not send signal to vehicle: " + e.getMessage()));

        }
    }

    @Operation(summary = "Request vehicle status update", description = "Sends an MQTT signal to request a status update from the vehicle. Requires ADMIN role.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Status update signal sent successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Failed to send signal to vehicle"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied - requires ADMIN role")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/vin/{vinNumber}/status-update")
    public ResponseEntity<ApiResponse<MessageResponse>> updateVehicleStatus(
            @Parameter(description = "The Vehicle Identification Number (VIN)", required = true, example = "1HGBH41JXMN109186")
            @PathVariable String vinNumber) {
        try {
            mqttService.publish(TOPIC_ORDER_UPDATE_STATUS, new VehicleDto(vinNumber));
            return ResponseEntity.ok(createSuccessResponse(new MessageResponse("Order signal sent!")));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(createFailureResponse("Could not send signal to vehicle: " + e.getMessage()));

        }
    }
}

package com.yaquodorg.yaquod.controller;

import static com.yaquodorg.yaquod.response.ApiResponse.createSuccessResponse;
import static org.springframework.http.HttpStatus.CREATED;

import com.yaquodorg.yaquod.dtos.vehicle.CreateVehicleDto;
import com.yaquodorg.yaquod.dtos.vehicle.VehicleDto;
import com.yaquodorg.yaquod.entity.User;
import com.yaquodorg.yaquod.entity.Vehicle;
import com.yaquodorg.yaquod.exception.ResourceNotFoundException;
import com.yaquodorg.yaquod.response.ApiResponse;
import com.yaquodorg.yaquod.response.CreateVehicleResponse;
import com.yaquodorg.yaquod.response.MessageResponse;
import com.yaquodorg.yaquod.service.mqtt.MqttService;
import com.yaquodorg.yaquod.service.vehicle.VehicleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/vehicles")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Vehicles", description = "Vehicle management APIs")
public class VehicleController {

    private static final String TOPIC_ORDER_UPDATE_LOCATION = "topic/vehicle/update/location/order";
    private static final String TOPIC_ORDER_UPDATE_STATUS = "topic/vehicle/update/status/order";
    private final VehicleService vehicleService;
    private final MqttService mqttService;

    @Operation(
            summary = "Create a new vehicle",
            description = "Creates a new vehicle in the system. Requires ADMIN role.")
    @ApiResponses(
            value = {
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "201",
                        description = "Vehicle created successfully"),
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "409",
                        description = "Vehicle already exists"),
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "403",
                        description = "Access denied - requires ADMIN role")
            })
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<ApiResponse<CreateVehicleResponse>> createVehicle(
            @Valid @RequestBody CreateVehicleDto createVehicleDto,
            @AuthenticationPrincipal User user) {
        CreateVehicleResponse response = vehicleService.createVehicle(createVehicleDto, user);
        return ResponseEntity.status(CREATED).body(createSuccessResponse(response));
    }

    @Operation(
            summary = "Get all vehicles",
            description = "Retrieves a list of all vehicles in the system. Requires ADMIN role.")
    @ApiResponses(
            value = {
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "200",
                        description = "Vehicles retrieved successfully"),
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "403",
                        description = "Access denied - requires ADMIN role")
            })
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<ApiResponse<List<Vehicle>>> getVehicles() {
        return ResponseEntity.ok(createSuccessResponse(vehicleService.getVehicles()));
    }

    @Operation(
            summary = "Get vehicle by ID",
            description = "Retrieves a specific vehicle by its database ID. Requires ADMIN role.")
    @ApiResponses(
            value = {
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "200",
                        description = "Vehicle retrieved successfully"),
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "404",
                        description = "Vehicle not found"),
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "403",
                        description = "Access denied - requires ADMIN role")
            })
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/id/{vehicleId}")
    public ResponseEntity<ApiResponse<Vehicle>> getVehicle(
            @Parameter(description = "The unique database ID of the vehicle", required = true)
                    @PathVariable
                    Long vehicleId) {
        Vehicle vehicle = vehicleService.getVehicle(vehicleId);
        return ResponseEntity.ok(createSuccessResponse(vehicle));
    }

    @Operation(
            summary = "Get vehicle by VIN",
            description =
                    "Retrieves a specific vehicle by its Vehicle Identification Number (VIN)."
                            + " Requires ADMIN role.")
    @ApiResponses(
            value = {
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "200",
                        description = "Vehicle retrieved successfully"),
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "404",
                        description = "Vehicle not found"),
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "403",
                        description = "Access denied - requires ADMIN role")
            })
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/vin/{vinNumber}")
    public ResponseEntity<ApiResponse<Vehicle>> getVehicle(
            @Parameter(
                            description = "The Vehicle Identification Number (VIN)",
                            required = true,
                            example = "1HGBH41JXMN109186")
                    @PathVariable
                    String vinNumber) {
        Vehicle vehicle =
                vehicleService
                        .getVehicleByVinNumber(vinNumber)
                        .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found!"));
        return ResponseEntity.ok(createSuccessResponse(vehicle));
    }

    @Operation(
            summary = "Update a vehicle",
            description =
                    "Updates an existing vehicle with the provided data. Requires ADMIN role.")
    @ApiResponses(
            value = {
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "200",
                        description = "Vehicle updated successfully"),
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "404",
                        description = "Vehicle not found"),
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "403",
                        description = "Access denied - requires ADMIN role")
            })
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping
    public ResponseEntity<ApiResponse<Vehicle>> updateVehicle(
            @Valid @RequestBody CreateVehicleDto createVehicleDto) {
        Vehicle vehicle = vehicleService.updateVehicle(createVehicleDto);
        return ResponseEntity.ok(createSuccessResponse(vehicle));
    }

    @Operation(
            summary = "Delete a vehicle",
            description =
                    "Deletes a vehicle from the system by its database ID. Requires ADMIN role.")
    @ApiResponses(
            value = {
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "200",
                        description = "Vehicle deleted successfully"),
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "404",
                        description = "Vehicle not found"),
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "403",
                        description = "Access denied - requires ADMIN role")
            })
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/id/{vehicleId}")
    public ResponseEntity<ApiResponse<MessageResponse>> deleteVehicle(
            @Parameter(
                            description = "The unique database ID of the vehicle to delete",
                            required = true)
                    @PathVariable
                    Long vehicleId) {
        vehicleService.deleteVehicle(vehicleId);
        return ResponseEntity.ok(
                createSuccessResponse(new MessageResponse("Vehicle deleted successfully!")));
    }

    @Operation(
            summary = "Request vehicle location update",
            description =
                    "Sends an MQTT signal to request a location update from the vehicle. Requires"
                            + " ADMIN role.")
    @ApiResponses(
            value = {
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "200",
                        description = "Location update signal sent successfully"),
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "403",
                        description = "Access denied - requires ADMIN role")
            })
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/vin/{vinNumber}/location-update")
    public ResponseEntity<ApiResponse<MessageResponse>> updateVehicleLocation(
            @Parameter(
                            description = "The Vehicle Identification Number (VIN)",
                            required = true,
                            example = "1HGBH41JXMN109186")
                    @PathVariable
                    String vinNumber) {
        mqttService.publish(TOPIC_ORDER_UPDATE_LOCATION, new VehicleDto(vinNumber));
        return ResponseEntity.ok(createSuccessResponse(new MessageResponse("Order signal sent!")));
    }

    @Operation(
            summary = "Request vehicle status update",
            description =
                    "Sends an MQTT signal to request a status update from the vehicle. Requires"
                            + " ADMIN role.")
    @ApiResponses(
            value = {
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "200",
                        description = "Status update signal sent successfully"),
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "403",
                        description = "Access denied - requires ADMIN role")
            })
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/vin/{vinNumber}/status-update")
    public ResponseEntity<ApiResponse<MessageResponse>> updateVehicleStatus(
            @Parameter(
                            description = "The Vehicle Identification Number (VIN)",
                            required = true,
                            example = "1HGBH41JXMN109186")
                    @PathVariable
                    String vinNumber) {
        mqttService.publish(TOPIC_ORDER_UPDATE_STATUS, new VehicleDto(vinNumber));
        return ResponseEntity.ok(createSuccessResponse(new MessageResponse("Order signal sent!")));
    }

    @Operation(summary = "Verify Vehicle", description = "Verify a vehicle exists or not.")
    @ApiResponses(
            value = {
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "200",
                        description = "Vehicles verified successfully"),
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "404",
                        description = "Vehicle not found")
            })
    @PreAuthorize("hasAnyRole('VEHICLE', 'ADMIN')")
    @GetMapping("/verify/{vinNumber}")
    public ResponseEntity<ApiResponse<MessageResponse>> verifyVehicle(
            @Parameter(description = "The vin number of the vehicle", required = true) @PathVariable
                    String vinNumber) {
        boolean isVehicleVerified = vehicleService.verifyVehicle(vinNumber);
        if (isVehicleVerified) {
            return ResponseEntity.ok(
                    createSuccessResponse(new MessageResponse("Vehicle Verified Successfully!")));
        } else {
            throw new ResourceNotFoundException("Vehicle Not Found!");
        }
    }
}

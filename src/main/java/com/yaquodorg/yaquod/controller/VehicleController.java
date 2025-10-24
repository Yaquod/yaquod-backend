package com.yaquodorg.yaquod.controller;

import com.yaquodorg.yaquod.dtos.CreateVehicleDto;
import com.yaquodorg.yaquod.dtos.VehicleDto;
import com.yaquodorg.yaquod.entity.Vehicle;
import com.yaquodorg.yaquod.response.ApiResponse;
import com.yaquodorg.yaquod.response.MessageResponse;
import com.yaquodorg.yaquod.service.mqtt.MqttService;
import com.yaquodorg.yaquod.service.vehicle.VehicleService;
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
public class VehicleController {

    private final VehicleService vehicleService;
    private final MqttService mqttService;
    private static final String TOPIC_ORDER_UPDATE_LOCATION = "topic/order_update_location";
    private static final String TOPIC_ORDER_UPDATE_STATUS = "topic/vehicle_update_status";

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<ApiResponse<Vehicle>> createVehicle(@RequestBody CreateVehicleDto createVehicleDto) {
        try {
            Vehicle vehicle = vehicleService.createVehicle(createVehicleDto);
            return ResponseEntity.status(CREATED)
                    .body(createSuccessResponse(vehicle));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(createFailureResponse("Failed to create vehicle: " + e.getMessage()));
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<ApiResponse<List<Vehicle>>> getVehicles() {
        return ResponseEntity.ok(createSuccessResponse(vehicleService.getVehicles()));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/id/{vehicleId}")
    public ResponseEntity<ApiResponse<Vehicle>> getVehicle(@PathVariable Long vehicleId) {
        try {
            Vehicle vehicle = vehicleService.getVehicle(vehicleId);
            return ResponseEntity.ok(createSuccessResponse(vehicle));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(createFailureResponse("Could not fetch vehicle: " + e.getMessage()));
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/uuid/{vehicleUUID}")
    public ResponseEntity<ApiResponse<Vehicle>> getVehicle(@PathVariable String vehicleUUID) {
        try {
            Vehicle vehicle = vehicleService.getVehicleByUUID(vehicleUUID)
                    .orElseThrow(() -> new RuntimeException("Vehicle not found!"));
            return ResponseEntity.ok(createSuccessResponse(vehicle));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(createFailureResponse("Could not fetch vehicle: " + e.getMessage()));
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping
    public ResponseEntity<ApiResponse<Vehicle>> updateVehicle(@RequestBody CreateVehicleDto createVehicleDto) {
        try {
            Vehicle vehicle = vehicleService.updateVehicle(createVehicleDto);
            return ResponseEntity.ok(createSuccessResponse(vehicle));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(createFailureResponse("Failed to update vehicle: " + e.getMessage()));
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/id/{vehicleId}")
    public ResponseEntity<ApiResponse<MessageResponse>> deleteVehicle(
            @PathVariable Long vehicleId) {
        try {
            vehicleService.deleteVehicle(vehicleId);
            return ResponseEntity.ok(createSuccessResponse(new MessageResponse("Vehicle deleted successfully!")));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(createFailureResponse("Could not delete vehicle: " + e.getMessage()));

        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/uuid/{vehicleUUID}/location-update")
    public ResponseEntity<ApiResponse<MessageResponse>> updateVehicleLocation(@PathVariable String vehicleUUID) {
        try {
            mqttService.publish(TOPIC_ORDER_UPDATE_LOCATION, new VehicleDto(vehicleUUID));
            return ResponseEntity.ok(createSuccessResponse(new MessageResponse("Order signal sent!")));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(createFailureResponse("Could not send signal to vehicle: " + e.getMessage()));

        }
    }


    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/uuid/{vehicleUUID}/status-update")
    public ResponseEntity<ApiResponse<MessageResponse>> updateVehicleStatus(@PathVariable String vehicleUUID) {
        try {
            mqttService.publish(TOPIC_ORDER_UPDATE_STATUS, new VehicleDto(vehicleUUID));
            return ResponseEntity.ok(createSuccessResponse(new MessageResponse("Order signal sent!")));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(createFailureResponse("Could not send signal to vehicle: " + e.getMessage()));

        }
    }


}

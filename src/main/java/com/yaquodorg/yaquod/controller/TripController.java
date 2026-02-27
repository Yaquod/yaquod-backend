package com.yaquodorg.yaquod.controller;

import static com.yaquodorg.yaquod.response.ApiResponse.createSuccessResponse;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.yaquodorg.yaquod.dtos.TripRequestDto;
import com.yaquodorg.yaquod.entity.Request;
import com.yaquodorg.yaquod.entity.Trip;
import com.yaquodorg.yaquod.entity.User;
import com.yaquodorg.yaquod.response.ApiResponse;
import com.yaquodorg.yaquod.response.MessageResponse;
import com.yaquodorg.yaquod.service.request.RequestService;
import com.yaquodorg.yaquod.service.trip.TripService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/trips")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Trips", description = "Trip management and request APIs")
public class TripController {

    private final RequestService requestService;
    private final TripService tripService;

    @Operation(summary = "Create a new trip request", description = "Creates a new trip request with start and end coordinates for the authenticated user")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Trip request created successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Failed to create trip request")
    })
    @PostMapping("/request")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<ApiResponse<Request>> createRequest(@RequestBody TripRequestDto tripRequestDto,
            @AuthenticationPrincipal User user) {
        try {
            Request request = requestService.createRequest(user.getId(),
                    tripRequestDto.getStartLong(),
                    tripRequestDto.getStartLat(),
                    tripRequestDto.getEndLong(),
                    tripRequestDto.getEndLat());

            return ResponseEntity
                    .ok(createSuccessResponse(request));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.createFailureResponse("Failed to create trip request: " + e.getMessage()));
        }
    }

    @Operation(summary = "Get request status", description = "Retrieves the status of a trip request by its ID")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Request status retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Failed to get request status")
    })
    @PreAuthorize("hasAnyRole('CLIENT', 'ADMIN')")
    @GetMapping("/request/status/{requestId}")
    public ResponseEntity<ApiResponse<Request>> getRequest(
            @Parameter(description = "The unique ID of the request", required = true) @PathVariable Long requestId) {
        try {
            Request request = requestService.getRequest(requestId);
            return ResponseEntity
                    .ok(createSuccessResponse(request));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.createFailureResponse("Failed to check Request status: " + e.getMessage()));
        }
    }

    @Operation(summary = "Get trip by request ID", description = "Retrieves a trip associated with a specific request ID")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Trip retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Failed to get trip by request ID")
    })
    @PreAuthorize("hasAnyRole('CLIENT', 'ADMIN')")
    @GetMapping("/by-request/{requestId}")
    public ResponseEntity<ApiResponse<Trip>> getTripByRequestId(
            @Parameter(description = "The unique ID of the request", required = true) @PathVariable Long requestId) {
        try {
            Trip trip = tripService.getTripByRequestId(requestId);
            return ResponseEntity
                    .ok(createSuccessResponse(trip));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.createFailureResponse("Failed to get Trip by requestId: " + e.getMessage()));
        }
    }

    @Operation(summary = "Get trip by ID", description = "Retrieves a specific trip by its database ID")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Trip retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Failed to get trip by ID")
    })
    @PreAuthorize("hasAnyRole('CLIENT', 'ADMIN')")
    @GetMapping("/{tripId}")
    public ResponseEntity<ApiResponse<Trip>> getTripById(
            @Parameter(description = "The unique database ID of the trip", required = true) @PathVariable Long tripId) {
        try {
            Trip trip = tripService.getTripById(tripId);
            return ResponseEntity
                    .ok(createSuccessResponse(trip));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.createFailureResponse("Failed to get Trip by id: " + e.getMessage()));
        }
    }

    @Operation(summary = "Delete a trip", description = "Deletes a trip from the system by its ID. Requires ADMIN role.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Trip deleted successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Failed to delete trip"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied - requires ADMIN role")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{tripId}")
    public ResponseEntity<ApiResponse<MessageResponse>> deleteTripById(
            @Parameter(description = "The unique database ID of the trip to delete", required = true) @PathVariable Long tripId) {
        try {
            tripService.deleteTripById(tripId);
            return ResponseEntity
                    .ok(createSuccessResponse(new MessageResponse("Trip deleted successfully")));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.createFailureResponse("Failed to delete Trip by id: " + e.getMessage()));
        }
    }

    @Operation(summary = "Get all trips", description = "Retrieves a list of all trips in the system")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Trips retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Failed to get trips")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping()
    public ResponseEntity<ApiResponse<List<Trip>>> getAllTrips() {
        try {
            List<Trip> trips = tripService.getAllTrips();
            return ResponseEntity
                    .ok(createSuccessResponse(trips));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.createFailureResponse("Failed to get all Trips: " + e.getMessage()));
        }
    }

    @Operation(summary = "Get trips for current user", description = "Retrieves all trips for the currently authenticated user")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "User trips retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Failed to get user trips")
    })
    @PreAuthorize("hasAnyRole('CLIENT', 'ADMIN')")
    @GetMapping("/user")
    public ResponseEntity<ApiResponse<List<Trip>>> getTripsByUserId(@AuthenticationPrincipal User user) {
        try {
            List<Trip> trips = tripService.getTripsByUserId(user.getId());
            return ResponseEntity
                    .ok(createSuccessResponse(trips));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.createFailureResponse("Failed to get Trips by userId: " + e.getMessage()));
        }
    }

    @Operation(summary = "Get last N trips for current user", description = "Retrieves the last N trips for the currently authenticated user")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Last N trips retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Failed to get last N trips")
    })
    @PreAuthorize("hasAnyRole('CLIENT', 'ADMIN')")
    @GetMapping("/last/{n}")
    public ResponseEntity<ApiResponse<List<Trip>>> getLastNTrips(
            @Parameter(description = "Number of recent trips to retrieve", required = true, example = "5") @PathVariable int n,
            @AuthenticationPrincipal User user) {
        try {
            List<Trip> trips = tripService.getUserLastNTrips(n, user.getId());
            return ResponseEntity
                    .ok(createSuccessResponse(trips));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.createFailureResponse("Failed to get last N Trips: " + e.getMessage()));
        }
    }

    @Operation(summary = "Get trips by vehicle VIN", description = "Retrieves all trips associated with a specific vehicle by its VIN number")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Trips retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Failed to get trips by VIN number")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/vehicle/{vinNumber}")
    public ResponseEntity<ApiResponse<List<Trip>>> getTripsByVinNumber(
            @Parameter(description = "The Vehicle Identification Number (VIN)", required = true, example = "1HGBH41JXMN109186") @PathVariable String vinNumber) {
        try {
            List<Trip> trips = tripService.getTripsByVinNumber(vinNumber);
            return ResponseEntity
                    .ok(createSuccessResponse(trips));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.createFailureResponse("Failed to get Trips by VIN number: " + e.getMessage()));
        }
    }

    @Operation(summary = "Decline a trip request", description = "Declines a pending trip request. Requires CLIENT role.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Request declined successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Failed to decline request"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied - requires CLIENT role")
    })
    @PreAuthorize("hasRole('CLIENT')")
    @PostMapping("/request/{requestId}/decline")
    public ResponseEntity<ApiResponse<MessageResponse>> declineRequest(
            @Parameter(description = "The unique ID of the request to decline", required = true) @PathVariable Long requestId,
            @AuthenticationPrincipal User user) {
        try {
            requestService.declineRequestById(requestId, user.getId());
            return ResponseEntity
                    .ok(createSuccessResponse(new MessageResponse("Request declined successfully")));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.createFailureResponse("Failed to decline Request: " + e.getMessage()));
        }
    }

    @Operation(summary = "Accept a trip request", description = "Accepts a pending trip request. Requires CLIENT role.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Request accepted successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Failed to accept request"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied - requires CLIENT role")
    })
    @PreAuthorize("hasRole('CLIENT')")
    @PostMapping("/request/{requestId}/accept")
    public ResponseEntity<ApiResponse<Request>> acceptRequest(
            @Parameter(description = "The unique ID of the request to accept", required = true) @PathVariable Long requestId,
            @AuthenticationPrincipal User user) {
        try {

            Request request = requestService.acceptRequestById(requestId, user.getId());
            return ResponseEntity
                    .ok(createSuccessResponse(request));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.createFailureResponse("Failed to accept Request: " + e.getMessage()));
        }
    }

    @Operation(summary = "Start a trip", description = "Moves vehicle and starts trip.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Trip started successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Failed to start trip"),
    })
    @PreAuthorize("hasAnyRole('CLIENT', 'ADMIN', 'VEHICLE')")
    @PostMapping("/request/{requestId}/start")
    public ResponseEntity<ApiResponse<MessageResponse>> startTrip(
            @Parameter(description = "The unique ID of the request assigned with trip", required = true) @PathVariable Long requestId) {
        try {
            tripService.startTrip(requestId);
            return ResponseEntity
                    .ok(ApiResponse.createSuccessResponse(new MessageResponse("Trip started successfully!")));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.createFailureResponse("Failed to start trip: " + e.getMessage()));
        }
    }

    @Operation(summary = "End a trip", description = "Ends a trip and updates vehicle status.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Trip ended successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Failed to end trip"),
    })
    @PreAuthorize("hasAnyRole('CLIENT', 'ADMIN', 'VEHICLE')")
    @PostMapping("/request/{requestId}/end")
    public ResponseEntity<ApiResponse<MessageResponse>> endTrip(
            @Parameter(description = "The unique ID of the request assigned with trip", required = true) @PathVariable Long requestId) {
        try {
            tripService.endTrip(requestId);
            return ResponseEntity
                    .ok(ApiResponse.createSuccessResponse(new MessageResponse("Trip ended successfully!")));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.createFailureResponse("Failed to end trip: " + e.getMessage()));
        }
    }

}

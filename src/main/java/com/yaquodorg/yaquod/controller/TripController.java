package com.yaquodorg.yaquod.controller;

import static com.yaquodorg.yaquod.response.ApiResponse.createSuccessResponse;

import com.yaquodorg.yaquod.dtos.admin.RequestDto;
import com.yaquodorg.yaquod.dtos.trip.TripDto;
import com.yaquodorg.yaquod.dtos.trip.TripRequestDto;
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
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/trips")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Trips", description = "Trip management and request APIs")
public class TripController {

    private final RequestService requestService;
    private final TripService tripService;

    @Operation(
            summary = "Create a new trip request",
            description =
                    "Creates a new trip request with start and end coordinates for the"
                            + " authenticated user")
    @ApiResponses(
            value = {
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "200",
                        description = "Trip request created successfully"),
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "400",
                        description = "Failed to create trip request"),
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "503",
                        description = "Service unavailable - no vehicles available")
            })
    @PreAuthorize("hasAnyRole('CLIENT', 'ADMIN')")
    @PostMapping("/request")
    public ResponseEntity<ApiResponse<Request>> createRequest(
            @RequestBody TripRequestDto tripRequestDto, @AuthenticationPrincipal User user) {
        Request request =
                requestService.createRequest(
                        user.getId(),
                        tripRequestDto.getStartLong(),
                        tripRequestDto.getStartLat(),
                        tripRequestDto.getEndLong(),
                        tripRequestDto.getEndLat());

        return ResponseEntity.ok(createSuccessResponse(request));
    }

    @Operation(
            summary = "Get request status",
            description = "Retrieves the status of a trip request by its ID")
    @ApiResponses(
            value = {
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "200",
                        description = "Request status retrieved successfully"),
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "404",
                        description = "Request not found")
            })
    @PreAuthorize("hasAnyRole('CLIENT', 'ADMIN')")
    @GetMapping("/request/status/{requestId}")
    public ResponseEntity<ApiResponse<Request>> getRequest(
            @Parameter(description = "The unique ID of the request", required = true) @PathVariable
                    Long requestId) {
        Request request = requestService.getRequest(requestId);
        return ResponseEntity.ok(createSuccessResponse(request));
    }

    @Operation(
            summary = "Cancel Request",
            description = "Cancels the current request and orders vehicle to stop ETA calculations")
    @ApiResponses(
            value = {
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "200",
                        description = "Request canceled successfully"),
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "404",
                        description = "Request not found")
            })
    @PreAuthorize("hasAnyRole('CLIENT', 'ADMIN')")
    @DeleteMapping("/request/{requestId}")
    public ResponseEntity<ApiResponse<MessageResponse>> cancelRequest(
            @Parameter(description = "The unique ID of the request", required = true) @PathVariable
                    Long requestId,
            @AuthenticationPrincipal User user) {
        requestService.cancelRequest(requestId, user.getId());
        return ResponseEntity.ok(
                createSuccessResponse(new MessageResponse("Request canceled successfully!")));
    }

    @Operation(
            summary = "Get trip by request ID",
            description = "Retrieves a trip associated with a specific request ID")
    @ApiResponses(
            value = {
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "200",
                        description = "Trip retrieved successfully"),
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "404",
                        description = "Trip not found")
            })
    @PreAuthorize("hasAnyRole('CLIENT', 'ADMIN')")
    @GetMapping("/by-request/{requestId}")
    public ResponseEntity<ApiResponse<Trip>> getTripByRequestId(
            @Parameter(description = "The unique ID of the request", required = true) @PathVariable
                    Long requestId) {
        Trip trip = tripService.getTripByRequestId(requestId);
        return ResponseEntity.ok(createSuccessResponse(trip));
    }

    @Operation(
            summary = "Get trip by ID",
            description = "Retrieves a specific trip by its database ID")
    @ApiResponses(
            value = {
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "200",
                        description = "Trip retrieved successfully"),
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "404",
                        description = "Trip not found")
            })
    @PreAuthorize("hasAnyRole('CLIENT', 'ADMIN')")
    @GetMapping("/{tripId}")
    public ResponseEntity<ApiResponse<Trip>> getTripById(
            @Parameter(description = "The unique database ID of the trip", required = true)
                    @PathVariable
                    Long tripId) {
        Trip trip = tripService.getTripById(tripId);
        return ResponseEntity.ok(createSuccessResponse(trip));
    }

    @Operation(
            summary = "Delete a trip",
            description = "Deletes a trip from the system by its ID. Requires ADMIN role.")
    @ApiResponses(
            value = {
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "200",
                        description = "Trip deleted successfully"),
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "403",
                        description = "Access denied - requires ADMIN role")
            })
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{tripId}")
    public ResponseEntity<ApiResponse<MessageResponse>> deleteTripById(
            @Parameter(
                            description = "The unique database ID of the trip to delete",
                            required = true)
                    @PathVariable
                    Long tripId) {
        tripService.deleteTripById(tripId);
        return ResponseEntity.ok(
                createSuccessResponse(new MessageResponse("Trip deleted successfully")));
    }

    @Operation(
            summary = "Get all trips",
            description = "Retrieves a list of all trips in the system")
    @ApiResponses(
            value = {
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "200",
                        description = "Trips retrieved successfully")
            })
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping()
    public ResponseEntity<ApiResponse<List<Trip>>> getAllTrips() {
        List<Trip> trips = tripService.getAllTrips();
        return ResponseEntity.ok(createSuccessResponse(trips));
    }

    @Operation(
            summary = "Get trips for current user",
            description = "Retrieves all trips for the currently authenticated user")
    @ApiResponses(
            value = {
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "200",
                        description = "User trips retrieved successfully")
            })
    @PreAuthorize("hasAnyRole('CLIENT', 'ADMIN')")
    @GetMapping("/user")
    public ResponseEntity<ApiResponse<List<Trip>>> getTripsByUserId(
            @AuthenticationPrincipal User user) {
        List<Trip> trips = tripService.getTripsByUserId(user.getId());
        return ResponseEntity.ok(createSuccessResponse(trips));
    }

    @Operation(
            summary = "Get paginated trips for current user",
            description =
                    "Retrieves paginated trips for the currently authenticated user, sorted by"
                            + " started date descending")
    @ApiResponses(
            value = {
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "200",
                        description = "User trips retrieved successfully")
            })
    @PreAuthorize("hasAnyRole('CLIENT', 'ADMIN')")
    @GetMapping("/last")
    public ResponseEntity<ApiResponse<Page<TripDto>>> getLastNTrips(
            @Parameter(description = "Page number (zero-based)", example = "0")
                    @RequestParam(defaultValue = "0")
                    int page,
            @Parameter(description = "Page size", example = "10") @RequestParam(defaultValue = "10")
                    int size,
            @AuthenticationPrincipal User user) {
        Page<TripDto> trips =
                tripService.getUserTripsPaginated(
                        PageRequest.of(page, size, Sort.by("startedAt").descending()),
                        user.getId());
        return ResponseEntity.ok(createSuccessResponse(trips));
    }

    @Operation(
            summary = "Get paginated requests for current user",
            description =
                    "Retrieves paginated trip requests for the currently authenticated user,"
                            + " sorted by creation date descending")
    @ApiResponses(
            value = {
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "200",
                        description = "User requests retrieved successfully")
            })
    @PreAuthorize("hasAnyRole('CLIENT', 'ADMIN')")
    @GetMapping("/requests")
    public ResponseEntity<ApiResponse<Page<RequestDto>>> getUserRequests(
            @Parameter(description = "Page number (zero-based)", example = "0")
                    @RequestParam(defaultValue = "0")
                    int page,
            @Parameter(description = "Page size", example = "10")
                    @RequestParam(defaultValue = "10")
                    int size,
            @AuthenticationPrincipal User user) {
        Page<RequestDto> requests =
                requestService.getUserRequestsPaginated(
                        PageRequest.of(page, size, Sort.by("createdAt").descending()),
                        user.getId());
        return ResponseEntity.ok(createSuccessResponse(requests));
    }

    @Operation(
            summary = "Get trips by vehicle VIN",
            description =
                    "Retrieves all trips associated with a specific vehicle by its VIN number")
    @ApiResponses(
            value = {
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "200",
                        description = "Trips retrieved successfully"),
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "404",
                        description = "Vehicle not found")
            })
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/vehicle/{vinNumber}")
    public ResponseEntity<ApiResponse<List<Trip>>> getTripsByVinNumber(
            @Parameter(
                            description = "The Vehicle Identification Number (VIN)",
                            required = true,
                            example = "1HGBH41JXMN109186")
                    @PathVariable
                    String vinNumber) {
        List<Trip> trips = tripService.getTripsByVinNumber(vinNumber);
        return ResponseEntity.ok(createSuccessResponse(trips));
    }

    @Operation(
            summary = "Decline a trip request",
            description = "Declines a pending trip request. Requires CLIENT role.")
    @ApiResponses(
            value = {
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "200",
                        description = "Request declined successfully"),
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "403",
                        description = "Access denied"),
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "404",
                        description = "Request not found"),
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "409",
                        description = "Invalid request state for decline")
            })
    @PreAuthorize("hasAnyRole('CLIENT', 'ADMIN')")
    @PostMapping("/request/{requestId}/decline")
    public ResponseEntity<ApiResponse<MessageResponse>> declineRequest(
            @Parameter(description = "The unique ID of the request to decline", required = true)
                    @PathVariable
                    Long requestId,
            @AuthenticationPrincipal User user) {
        requestService.declineRequestById(requestId, user.getId());
        return ResponseEntity.ok(
                createSuccessResponse(new MessageResponse("Request declined successfully")));
    }

    @Operation(
            summary = "Accept a trip request",
            description = "Accepts a pending trip request. Requires CLIENT role.")
    @ApiResponses(
            value = {
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "200",
                        description = "Request accepted successfully"),
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "403",
                        description = "Access denied"),
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "404",
                        description = "Request not found"),
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "409",
                        description = "Invalid request state for acceptance")
            })
    @PreAuthorize("hasAnyRole('CLIENT', 'ADMIN')")
    @PostMapping("/request/{requestId}/accept")
    public ResponseEntity<ApiResponse<Request>> acceptRequest(
            @Parameter(description = "The unique ID of the request to accept", required = true)
                    @PathVariable
                    Long requestId,
            @AuthenticationPrincipal User user) {
        Request request = requestService.acceptRequestById(requestId, user.getId());
        return ResponseEntity.ok(createSuccessResponse(request));
    }

    @Operation(
            summary = "Track vehicle location streaming",
            description = "SSE endpoint to keep receiving the vehicle location update stream.")
    @ApiResponses(
            value = {
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "200",
                        description = "Ok"),
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "403",
                        description = "Access denied"),
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "409",
                        description = "Invalid request state for acceptance")
            })
    @PreAuthorize("hasAnyRole('CLIENT', 'ADMIN')")
    @GetMapping("/{tripId}/location/stream")
    public SseEmitter streamLocation(@PathVariable Long tripId) {
        return tripService.subscribeToLocationStream(tripId);
    }

    @Operation(summary = "Start a trip", description = "Moves vehicle and starts trip.")
    @ApiResponses(
            value = {
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "200",
                        description = "Trip started successfully"),
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "404",
                        description = "Trip not found"),
            })
    @PreAuthorize("hasAnyRole('CLIENT', 'ADMIN', 'VEHICLE')")
    @PostMapping("/request/{requestId}/start")
    public ResponseEntity<ApiResponse<MessageResponse>> startTrip(
            @Parameter(
                            description = "The unique ID of the request assigned with trip",
                            required = true)
                    @PathVariable
                    Long requestId) {
        tripService.startTrip(requestId);
        return ResponseEntity.ok(
                createSuccessResponse(new MessageResponse("Trip started successfully!")));
    }

    @Operation(summary = "End a trip", description = "Ends a trip and updates vehicle status.")
    @ApiResponses(
            value = {
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "200",
                        description = "Trip ended successfully"),
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "404",
                        description = "Trip not found"),
            })
    @PreAuthorize("hasAnyRole('CLIENT', 'ADMIN', 'VEHICLE')")
    @PostMapping("/request/{requestId}/end")
    public ResponseEntity<ApiResponse<MessageResponse>> endTrip(
            @Parameter(
                            description = "The unique ID of the request assigned with trip",
                            required = true)
                    @PathVariable
                    Long requestId) {
        tripService.endTrip(requestId);
        return ResponseEntity.ok(
                createSuccessResponse(new MessageResponse("Trip ended successfully!")));
    }
}

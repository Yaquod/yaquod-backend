package com.yaquodorg.yaquod.controller;

import com.yaquodorg.yaquod.dtos.TripRequestDto;
import com.yaquodorg.yaquod.entity.Request;
import com.yaquodorg.yaquod.entity.Trip;
import com.yaquodorg.yaquod.entity.User;
import com.yaquodorg.yaquod.response.ApiResponse;
import com.yaquodorg.yaquod.response.MessageResponse;
import com.yaquodorg.yaquod.service.request.RequestService;
import com.yaquodorg.yaquod.service.trip.TripService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.yaquodorg.yaquod.response.ApiResponse.createSuccessResponse;

@RestController
@RequestMapping("/api/trips")
@RequiredArgsConstructor
@Slf4j
public class TripController {

    private final RequestService requestService;
    private final TripService tripService;

    @PostMapping("/request")
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

    @GetMapping("/request/status/{requestId}")
    public ResponseEntity<ApiResponse<Request>> getRequest(@PathVariable Long requestId) {
        try {
            Request request = requestService.getRequest(requestId);
            return ResponseEntity
                    .ok(createSuccessResponse(request));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.createFailureResponse("Failed to check Request status: " + e.getMessage()));
        }

    }


    @GetMapping("/by-request/{requestId}")
    public ResponseEntity<ApiResponse<Trip>> getTripByRequestId(@PathVariable Long requestId) {
        try {
            Trip trip = tripService.getTripByRequestId(requestId);
            return ResponseEntity
                    .ok(createSuccessResponse(trip));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.createFailureResponse("Failed to get Trip by requestId: " + e.getMessage()));
        }
    }

    @GetMapping("/{tripId}")
    public ResponseEntity<ApiResponse<Trip>> getTripById(@PathVariable Long tripId) {
        try {
            Trip trip = tripService.getTripById(tripId);
            return ResponseEntity
                    .ok(createSuccessResponse(trip));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.createFailureResponse("Failed to get Trip by id: " + e.getMessage()));
        }

    }


    @DeleteMapping("/{tripId}")
    public ResponseEntity<ApiResponse<MessageResponse>> deleteTripById(@PathVariable Long tripId) {
        try {
            tripService.deleteTripById(tripId);
            return ResponseEntity
                    .ok(createSuccessResponse(new MessageResponse("Trip deleted successfully")));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.createFailureResponse("Failed to delete Trip by id: " + e.getMessage()));
        }

    }


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

    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<Trip>>> getTripsByUserId(@PathVariable Long userId) {
        try {
            List<Trip> trips = tripService.getTripsByUserId(userId);
            return ResponseEntity
                    .ok(createSuccessResponse(trips));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.createFailureResponse("Failed to get Trips by userId: " + e.getMessage()));
        }
    }


    @GetMapping("/last/{n}")
    public ResponseEntity<ApiResponse<List<Trip>>> getLastNTrips(@PathVariable int n) {
        try {
            List<Trip> trips = tripService.getLastNTrips(n);
            return ResponseEntity
                    .ok(createSuccessResponse(trips));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.createFailureResponse("Failed to get last N Trips: " + e.getMessage()));
        }
    }


    @GetMapping("/vehicle/{vinNumber}")
    public ResponseEntity<ApiResponse<List<Trip>>> getTripsByVinNumber(@PathVariable String vinNumber) {
        try {
            List<Trip> trips = tripService.getTripsByVinNumber(vinNumber);
            return ResponseEntity
                    .ok(createSuccessResponse(trips));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.createFailureResponse("Failed to get Trips by VIN number: " + e.getMessage()));
        }
    }


}

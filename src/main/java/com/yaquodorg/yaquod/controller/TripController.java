package com.yaquodorg.yaquod.controller;

import com.yaquodorg.yaquod.dtos.TripRequestDto;
import com.yaquodorg.yaquod.entity.RequestStatus;
import com.yaquodorg.yaquod.response.ApiResponse;
import com.yaquodorg.yaquod.service.jwt.JwtService;
import com.yaquodorg.yaquod.service.trip.TripService;
import com.yaquodorg.yaquod.service.user.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static com.yaquodorg.yaquod.response.ApiResponse.createSuccessResponse;

@RestController
@RequestMapping("/api/trip")
@RequiredArgsConstructor
@Slf4j
public class TripController {

    private final TripService tripService;
    private final JwtService jwtService;
    private final UserService userService;

    @PostMapping("/request")
    public ResponseEntity<ApiResponse<TripRequestDto>> initTrip(@RequestBody TripRequestDto tripRequestDto,
                                                                @RequestHeader("Authorization") String token) {
        try {
            String userEmail = jwtService.getEmailFromToken(token.substring(7));
            Long userId = userService.getUser(userEmail).orElseThrow().getId();
            tripService.createRequest(userId,
                    tripRequestDto.getStartLong(),
                    tripRequestDto.getStartLat(),
                    tripRequestDto.getEndLong(),
                    tripRequestDto.getEndLat());
            return ResponseEntity
                    .ok(createSuccessResponse(tripRequestDto));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.createFailureResponse("Failed to create trip request: " + e.getMessage()));
        }

    }

    @GetMapping("/status/{requestId}")
    public ResponseEntity<ApiResponse<?>> checkTripStatus(@PathVariable Long requestId) {
        try {
            RequestStatus etaStatus = tripService.getRequestStatusByRequestId(requestId);
            return ResponseEntity
                    .ok(createSuccessResponse(etaStatus));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.createFailureResponse("Failed to check Request status: " + e.getMessage()));
        }

    }


}

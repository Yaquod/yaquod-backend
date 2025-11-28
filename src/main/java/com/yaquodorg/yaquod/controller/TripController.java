package com.yaquodorg.yaquod.controller;

import static com.yaquodorg.yaquod.response.ApiResponse.createSuccessResponse;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.yaquodorg.yaquod.dtos.TripRequestDto;
import com.yaquodorg.yaquod.entity.Request;
import com.yaquodorg.yaquod.entity.User;
import com.yaquodorg.yaquod.response.ApiResponse;
import com.yaquodorg.yaquod.service.request.RequestService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/trips")
@RequiredArgsConstructor
@Slf4j
public class TripController {

    private final RequestService requestService;

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
}

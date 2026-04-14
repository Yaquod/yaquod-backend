package com.yaquodorg.yaquod.controller;

import com.yaquodorg.yaquod.response.ApiResponse;
import com.yaquodorg.yaquod.service.idempotency.IdempotencyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

//TODO: This is just for manual testing of the idempotency service
//TODO: MUST BE DELETED LATER
@RestController
@RequestMapping("/api/test/idempotency")
@RequiredArgsConstructor
@Tag(name = "Idempotency Test", description = "Temporary endpoints for testing idempotency service")
public class IdempotencyTestController {

    private final IdempotencyService idempotencyService;

    @Operation(
            summary = "Validate idempotency key",
            description = "Creates a new idempotency key in Redis")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Key created successfully")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "409",
            description = "Duplicate key detected")
    @PostMapping("/validate")
    public ResponseEntity<ApiResponse<String>> validate(
            @RequestParam String key, @RequestParam String paymentId) {
        idempotencyService.validate(key, paymentId);
        return ResponseEntity.ok(ApiResponse.createSuccessResponse("Idempotency key validated"));
    }

    @Operation(
            summary = "Invalidate idempotency key",
            description = "Deletes an idempotency key from Redis")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Key deleted successfully")
    @DeleteMapping("/invalidate")
    public ResponseEntity<ApiResponse<String>> invalidate(
            @RequestParam String key, @RequestParam String paymentId) {
        idempotencyService.invalidate(key, paymentId);
        return ResponseEntity.ok(ApiResponse.createSuccessResponse("Idempotency key invalidated"));
    }

    @Operation(
            summary = "Find existing key",
            description = "Gets the value of an idempotency key from Redis")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Key found")
    @GetMapping("/find")
    public ResponseEntity<ApiResponse<String>> find(
            @RequestParam String key, @RequestParam String paymentId) {
        String value = idempotencyService.findExistingKey(key, paymentId);
        return ResponseEntity.ok(ApiResponse.createSuccessResponse(value));
    }
}

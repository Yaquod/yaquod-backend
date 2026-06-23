package com.yaquodorg.yaquod.controller;

import static com.yaquodorg.yaquod.response.ApiResponse.createSuccessResponse;
import static com.yaquodorg.yaquod.service.rating.RatingServiceImpl.toRatingResponse;

import com.yaquodorg.yaquod.dtos.CreateRatingDto;
import com.yaquodorg.yaquod.dtos.UpdateRatingCommentDto;
import com.yaquodorg.yaquod.dtos.UpdateRatingValueDto;
import com.yaquodorg.yaquod.entity.Rating;
import com.yaquodorg.yaquod.entity.User;
import com.yaquodorg.yaquod.response.ApiResponse;
import com.yaquodorg.yaquod.response.RatingResponse;
import com.yaquodorg.yaquod.service.rating.RatingService;
import com.yaquodorg.yaquod.service.rating.RatingServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ratings")
@Tag(name = "Ratings", description = "Trip rating management APIs")
public class RatingController {

    private final RatingService ratingService;

    public RatingController(RatingService ratingService) {
        this.ratingService = ratingService;
    }

    @Operation(summary = "Create a rating", description = "Creates a rating for a completed trip")
    @ApiResponses(
            value = {
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "200",
                        description = "Rating created successfully"),
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "403",
                        description = "Access denied"),
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "409",
                        description = "Rating already exists or invalid trip state")
            })
    @PreAuthorize("hasAnyRole('CLIENT', 'ADMIN')")
    @PostMapping
    public ResponseEntity<ApiResponse<RatingResponse>> createRating(
            @Valid @RequestBody CreateRatingDto dto, @AuthenticationPrincipal User user) {
        Rating rating =
                ratingService.createRating(
                        user.getId(), dto.getTripId(), dto.getRatingValue(), dto.getComment());
        return ResponseEntity.ok(createSuccessResponse(toRatingResponse(rating)));
    }

    @Operation(summary = "Get rating by ID", description = "Retrieves a rating by its ID")
    @PreAuthorize("hasAnyRole('CLIENT', 'ADMIN')")
    @GetMapping("/{ratingId}")
    public ResponseEntity<ApiResponse<RatingResponse>> getRatingById(@PathVariable Long ratingId) {
        Rating rating = ratingService.getRatingById(ratingId);
        return ResponseEntity.ok(createSuccessResponse(toRatingResponse(rating)));
    }

    @Operation(summary = "Get all ratings", description = "Retrieves all ratings")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<ApiResponse<List<RatingResponse>>> getAllRatings() {
        List<RatingResponse> ratings =
                ratingService.getAllRatings().stream()
                        .map(RatingServiceImpl::toRatingResponse)
                        .toList();
        return ResponseEntity.ok(createSuccessResponse(ratings));
    }

    @Operation(
            summary = "Get current user ratings",
            description = "Retrieves all ratings created by current authenticated user")
    @PreAuthorize("hasAnyRole('CLIENT', 'ADMIN')")
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<List<RatingResponse>>> getMyRatings(
            @AuthenticationPrincipal User user) {
        List<RatingResponse> ratings =
                ratingService.getRatingsByUserId(user.getId()).stream()
                        .map(RatingServiceImpl::toRatingResponse)
                        .toList();
        return ResponseEntity.ok(createSuccessResponse(ratings));
    }

    @Operation(summary = "Get ratings by user ID", description = "Retrieves ratings by user ID")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<RatingResponse>>> getRatingsByUserId(
            @PathVariable Long userId) {
        List<RatingResponse> ratings =
                ratingService.getRatingsByUserId(userId).stream()
                        .map(RatingServiceImpl::toRatingResponse)
                        .toList();
        return ResponseEntity.ok(createSuccessResponse(ratings));
    }

    @Operation(
            summary = "Get ratings by vehicle ID",
            description = "Retrieves ratings for a specific vehicle")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/vehicle/{vehicleId}")
    public ResponseEntity<ApiResponse<List<RatingResponse>>> getRatingsByVehicleId(
            @PathVariable Long vehicleId) {
        List<RatingResponse> ratings =
                ratingService.getRatingsByVehicleId(vehicleId).stream()
                        .map(RatingServiceImpl::toRatingResponse)
                        .toList();
        return ResponseEntity.ok(createSuccessResponse(ratings));
    }

    @Operation(summary = "Update rating value", description = "Updates only the rating value")
    @PreAuthorize("hasAnyRole('CLIENT', 'ADMIN')")
    @PatchMapping("/{ratingId}/value")
    public ResponseEntity<ApiResponse<RatingResponse>> updateRatingValue(
            @PathVariable Long ratingId,
            @Valid @RequestBody UpdateRatingValueDto dto,
            @AuthenticationPrincipal User user) {
        Rating rating =
                ratingService.updateRatingValue(ratingId, user.getId(), dto.getRatingValue());
        return ResponseEntity.ok(createSuccessResponse(toRatingResponse(rating)));
    }

    @Operation(summary = "Update rating comment", description = "Updates only the rating comment")
    @PreAuthorize("hasAnyRole('CLIENT', 'ADMIN')")
    @PatchMapping("/{ratingId}/comment")
    public ResponseEntity<ApiResponse<RatingResponse>> updateRatingComment(
            @PathVariable Long ratingId,
            @Valid @RequestBody UpdateRatingCommentDto dto,
            @AuthenticationPrincipal User user) {
        Rating rating = ratingService.updateRatingComment(ratingId, user.getId(), dto.getComment());
        return ResponseEntity.ok(createSuccessResponse(toRatingResponse(rating)));
    }

    @Operation(summary = "Delete a rating", description = "Deletes a rating by ID")
    @PreAuthorize("hasAnyRole('CLIENT', 'ADMIN')")
    @DeleteMapping("/{ratingId}")
    public ResponseEntity<ApiResponse<String>> deleteRating(
            @PathVariable Long ratingId, @AuthenticationPrincipal User user) {
        ratingService.deleteRating(ratingId, user.getId());
        return ResponseEntity.ok(createSuccessResponse("Rating deleted successfully"));
    }
}

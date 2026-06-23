package com.yaquodorg.yaquod.controller;

import static com.yaquodorg.yaquod.response.ApiResponse.createSuccessResponse;

import com.yaquodorg.yaquod.dtos.CreateRatingDto;
import com.yaquodorg.yaquod.dtos.UpdateRatingCommentDto;
import com.yaquodorg.yaquod.dtos.UpdateRatingValueDto;
import com.yaquodorg.yaquod.entity.Rating;
import com.yaquodorg.yaquod.entity.User;
import com.yaquodorg.yaquod.response.ApiResponse;
import com.yaquodorg.yaquod.service.rating.RatingService;
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
    public ResponseEntity<ApiResponse<Rating>> createRating(
            @Valid @RequestBody CreateRatingDto createRatingDto,
            @AuthenticationPrincipal User user) {
        Rating rating =
                ratingService.createRating(
                        user.getId(),
                        createRatingDto.getTripId(),
                        createRatingDto.getRatingValue(),
                        createRatingDto.getComment());
        return ResponseEntity.ok(createSuccessResponse(rating));
    }

    @Operation(summary = "Get rating by ID", description = "Retrieves a rating by its ID")
    @PreAuthorize("hasAnyRole('CLIENT', 'ADMIN')")
    @GetMapping("/{ratingId}")
    public ResponseEntity<ApiResponse<Rating>> getRatingById(@PathVariable Long ratingId) {
        Rating rating = ratingService.getRatingById(ratingId);
        return ResponseEntity.ok(createSuccessResponse(rating));
    }

    @Operation(summary = "Get all ratings", description = "Retrieves all ratings")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<ApiResponse<List<Rating>>> getAllRatings() {
        return ResponseEntity.ok(createSuccessResponse(ratingService.getAllRatings()));
    }

    @Operation(
            summary = "Get current user ratings",
            description = "Retrieves all ratings created by current authenticated user")
    @PreAuthorize("hasAnyRole('CLIENT', 'ADMIN')")
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<List<Rating>>> getMyRatings(
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(
                createSuccessResponse(ratingService.getRatingsByUserId(user.getId())));
    }

    @Operation(summary = "Get ratings by user ID", description = "Retrieves ratings by user ID")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<Rating>>> getRatingsByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(createSuccessResponse(ratingService.getRatingsByUserId(userId)));
    }

    @Operation(
            summary = "Get ratings by vehicle ID",
            description = "Retrieves ratings for a specific vehicle")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/vehicle/{vehicleId}")
    public ResponseEntity<ApiResponse<List<Rating>>> getRatingsByVehicleId(
            @PathVariable Long vehicleId) {
        return ResponseEntity.ok(
                createSuccessResponse(ratingService.getRatingsByVehicleId(vehicleId)));
    }

    @Operation(summary = "Update rating value", description = "Updates only the rating value")
    @PreAuthorize("hasAnyRole('CLIENT', 'ADMIN')")
    @PatchMapping("/{ratingId}/value")
    public ResponseEntity<ApiResponse<Rating>> updateRatingValue(
            @PathVariable Long ratingId,
            @Valid @RequestBody UpdateRatingValueDto updateRatingValueDto,
            @AuthenticationPrincipal User user) {
        Rating rating =
                ratingService.updateRatingValue(
                        ratingId, user.getId(), updateRatingValueDto.getRatingValue());
        return ResponseEntity.ok(createSuccessResponse(rating));
    }

    @Operation(summary = "Update rating comment", description = "Updates only the rating comment")
    @PreAuthorize("hasAnyRole('CLIENT', 'ADMIN')")
    @PatchMapping("/{ratingId}/comment")
    public ResponseEntity<ApiResponse<Rating>> updateRatingComment(
            @PathVariable Long ratingId,
            @Valid @RequestBody UpdateRatingCommentDto updateRatingCommentDto,
            @AuthenticationPrincipal User user) {
        Rating rating =
                ratingService.updateRatingComment(
                        ratingId, user.getId(), updateRatingCommentDto.getComment());
        return ResponseEntity.ok(createSuccessResponse(rating));
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

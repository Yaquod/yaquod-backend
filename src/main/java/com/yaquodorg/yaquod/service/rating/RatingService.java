package com.yaquodorg.yaquod.service.rating;

import com.yaquodorg.yaquod.entity.Rating;
import com.yaquodorg.yaquod.response.RatingResponse;
import java.util.List;

public interface RatingService {
    Rating createRating(Long userId, Long tripId, Integer ratingValue, String comment);

    Rating getRatingById(Long id);

    List<Rating> getAllRatings();

    List<Rating> getRatingsByUserId(Long userId);

    List<Rating> getRatingsByVehicleId(Long vehicleId);

    Rating updateRatingValue(Long id, Long actorUserId, Integer ratingValue);

    Rating updateRatingComment(Long id, Long actorUserId, String comment);

    void deleteRating(Long id, Long actorUserId);

    long countRatings();

    double averageRating();

    static RatingResponse toRatingResponse(Rating rating) {
        return RatingResponse.builder()
                .id(rating.getId())
                .ratingValue(rating.getRatingValue())
                .comment(rating.getComment())
                .userId(rating.getUser() != null ? rating.getUser().getId() : null)
                .tripId(rating.getTrip() != null ? rating.getTrip().getId() : null)
                .vehicleId(rating.getVehicle() != null ? rating.getVehicle().getId() : null)
                .build();
    }
}

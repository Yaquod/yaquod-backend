package com.yaquodorg.yaquod.service.rating;

import com.yaquodorg.yaquod.entity.Rating;
import java.util.List;

public interface RatingService {
    Rating createRating(Long userId, Long tripId, Integer ratingValue, String comment);

    Rating getRatingById(Long id);

    Rating updateRatingValue(Long id, Long actorUserId, Integer ratingValue);

    Rating updateRatingComment(Long id, Long actorUserId, String comment);

    void deleteRating(Long id, Long actorUserId);

    List<Rating> getAllRatings();

    List<Rating> getRatingsByUserId(Long userId);

    List<Rating> getRatingsByVehicleId(Long vehicleId);
}

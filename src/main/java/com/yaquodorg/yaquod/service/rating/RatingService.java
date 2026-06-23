package com.yaquodorg.yaquod.service.rating;

import com.yaquodorg.yaquod.entity.Rating;
import java.util.List;

public interface RatingService {
    Rating createRating(Rating rating);

    Rating getRatingById(Long id);

    Rating updateRatingValue(Long id, Integer ratingValue);

    Rating updateRatingComment(Long id, String comment);

    void deleteRating(Long id);

    List<Rating> getAllRatings();

    List<Rating> getRatingsByUserId(Long userId);

    List<Rating> getRatingsByVehicleId(Long vehicleId);
}

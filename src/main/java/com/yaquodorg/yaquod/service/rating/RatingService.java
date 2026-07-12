package com.yaquodorg.yaquod.service.rating;

import com.yaquodorg.yaquod.entity.Rating;
import com.yaquodorg.yaquod.response.RatingResponse;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface RatingService {
    Rating createRating(Long userId, Long tripId, Integer ratingValue, String comment);

    Rating getRatingById(Long id);

    List<Rating> getAllRatings();

    Page<RatingResponse> getMyRatingsPaginated(Pageable pageable, Long userId);

    List<Rating> getRatingsByUserId(Long userId);

    List<Rating> getRatingsByVehicleId(Long vehicleId);

    Rating updateRatingValue(Long id, Long actorUserId, Integer ratingValue);

    Rating updateRatingComment(Long id, Long actorUserId, String comment);

    void deleteRating(Long id, Long actorUserId);

    long countRatings();

    double averageRating();
}

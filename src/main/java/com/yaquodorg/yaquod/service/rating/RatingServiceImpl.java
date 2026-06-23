package com.yaquodorg.yaquod.service.rating;

import com.yaquodorg.yaquod.entity.Rating;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class RatingServiceImpl implements RatingService {
    @Override
    public Rating createRating(Rating rating) {
        return null;
    }

    @Override
    public Rating getRatingById(Long id) {
        return null;
    }

    @Override
    @Transactional
    public Rating updateRatingValue(Long id, Integer ratingValue) {
        return null;
    }

    @Override
    @Transactional
    public Rating updateRatingComment(Long id, String comment) {
        return null;
    }

    @Override
    public void deleteRating(Long id) {}

    @Override
    public List<Rating> getAllRatings() {
        return List.of();
    }

    @Override
    public List<Rating> getRatingsByUserId(Long userId) {
        return List.of();
    }

    @Override
    public List<Rating> getRatingsByVehicleId(Long vehicleId) {
        return List.of();
    }
}

package com.yaquodorg.yaquod.service.rating;

import com.yaquodorg.yaquod.entity.*;
import com.yaquodorg.yaquod.exception.ResourceAlreadyExistsException;
import com.yaquodorg.yaquod.exception.ResourceNotFoundException;
import com.yaquodorg.yaquod.repository.RatingRepository;
import com.yaquodorg.yaquod.response.RatingResponse;
import com.yaquodorg.yaquod.service.trip.TripService;
import com.yaquodorg.yaquod.service.user.UserService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class RatingServiceImpl implements RatingService {
    private final RatingRepository ratingRepository;
    private final UserService userService;
    private final TripService tripService;

    @Override
    @Transactional
    public Rating createRating(Long userId, Long tripId, Integer ratingValue, String comment) {
        log.info("Creating rating for trip id: {} by user id: {}", tripId, userId);
        User user = userService.getUserById(userId);
        Trip trip = tripService.getTripById(tripId);

        if (!trip.getUser().getId().equals(userId)) {
            log.warn("User id: {} is not allowed to rate trip id: {}", userId, tripId);
            throw new AccessDeniedException("Unauthorized to rate this trip");
        }

        if (trip.getStatus() != TripStatus.COMPLETED) {
            log.warn("Trip id: {} is not completed, current status: {}", tripId, trip.getStatus());
            throw new IllegalStateException("Trip must be completed before rating");
        }

        if (ratingRepository.existsByTripId(tripId)) {
            log.warn("Rating already exists for trip id: {}", tripId);
            throw new ResourceAlreadyExistsException("Rating already exists for this trip");
        }

        Vehicle vehicle = trip.getVehicle();
        if (vehicle == null) {
            throw new IllegalStateException("Trip has no associated vehicle");
        }

        Rating rating =
                Rating.builder()
                        .ratingValue(ratingValue)
                        .comment(comment)
                        .user(user)
                        .trip(trip)
                        .vehicle(vehicle)
                        .build();

        Rating savedRating = ratingRepository.save(rating);
        log.info("Rating created successfully with id: {}", savedRating.getId());
        return savedRating;
    }

    @Override
    public Rating getRatingById(Long id) {
        Rating rating =
                ratingRepository
                        .findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException("Rating not found!"));
        log.info("Rating found with id: {}", id);
        return rating;
    }

    @Override
    @Transactional
    public Rating updateRatingValue(Long id, Long actorUserId, Integer ratingValue) {
        Rating rating = getRatingById(id);
        validateOwnershipOrAdmin(rating, actorUserId);
        rating.setRatingValue(ratingValue);
        log.info("Rating value updated successfully for id: {}", id);
        return rating;
    }

    @Override
    @Transactional
    public Rating updateRatingComment(Long id, Long actorUserId, String comment) {
        Rating rating = getRatingById(id);
        validateOwnershipOrAdmin(rating, actorUserId);
        rating.setComment(comment);
        log.info("Rating comment updated successfully for id: {}", id);
        return rating;
    }

    @Override
    @Transactional
    public void deleteRating(Long id, Long actorUserId) {
        Rating rating = getRatingById(id);
        validateOwnershipOrAdmin(rating, actorUserId);
        ratingRepository.deleteById(id);
        log.info("Rating deleted successfully for id: {}", id);
    }

    @Override
    public List<Rating> getAllRatings() {
        return ratingRepository.findAll();
    }

    @Override
    public List<Rating> getRatingsByUserId(Long userId) {
        return ratingRepository.findByUserId(userId);
    }

    @Override
    public List<Rating> getRatingsByVehicleId(Long vehicleId) {
        return ratingRepository.findByVehicleId(vehicleId);
    }

    private void validateOwnershipOrAdmin(Rating rating, Long actorUserId) {
        User actor = userService.getUserById(actorUserId);
        boolean isOwner = rating.getUser() != null && rating.getUser().getId().equals(actorUserId);
        if (!isOwner && actor.getRole() != Role.ADMIN) {
            throw new AccessDeniedException("Unauthorized to manage this rating");
        }
        log.debug("User id: {} is authorized to manage rating id: {}", actorUserId, rating.getId());
    }

    public static RatingResponse toRatingResponse(Rating rating) {
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

package com.yaquodorg.yaquod.repository;

import com.yaquodorg.yaquod.entity.Rating;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RatingRepository extends JpaRepository<Rating, Long> {
    List<Rating> findByUserId(Long userId);

    List<Rating> findByVehicleId(Long vehicleId);

    boolean existsByTripId(Long tripId);
}

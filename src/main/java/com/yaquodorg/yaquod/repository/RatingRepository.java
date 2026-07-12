package com.yaquodorg.yaquod.repository;

import com.yaquodorg.yaquod.entity.Rating;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface RatingRepository extends JpaRepository<Rating, Long> {
    Page<Rating> findByUserId(Long userId, Pageable pageable);

    List<Rating> findByUserId(Long userId);

    List<Rating> findByVehicleId(Long vehicleId);

    boolean existsByTripId(Long tripId);

    @Modifying
    @Query("DELETE FROM Rating r WHERE r.id = :id")
    void deleteRatingById(@Param("id") Long id);

    @Query("SELECT AVG(r.ratingValue) FROM Rating r")
    Optional<Double> avgRatingValue();
}

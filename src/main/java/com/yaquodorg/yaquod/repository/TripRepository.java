package com.yaquodorg.yaquod.repository;

import com.yaquodorg.yaquod.entity.Trip;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TripRepository extends JpaRepository<Trip, Long> {
  Trip findByRequestId(Long requestId);

  List<Trip> findByUserId(Long userId);

  List<Trip> findByUserIdOrderByStartedAtDesc(Long userId, Pageable pageable);

  List<Trip> findByVehicleVinNumber(String vehicleVinNumber);
}

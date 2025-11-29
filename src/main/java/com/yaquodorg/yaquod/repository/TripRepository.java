package com.yaquodorg.yaquod.repository;

import com.yaquodorg.yaquod.entity.Trip;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TripRepository extends JpaRepository<Trip, Long> {
    Trip findByRequestId(Long requestId);

    List<Trip> findByUserId(Long userId);


    List<Trip> findNTopNByOrderByStartedAtDesc(int n);

    List<Trip> findByVehicleVinNumber(String vehicleVinNumber);
}

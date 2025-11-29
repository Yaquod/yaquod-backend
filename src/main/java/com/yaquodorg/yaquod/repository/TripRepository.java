package com.yaquodorg.yaquod.repository;

import com.yaquodorg.yaquod.entity.Trip;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TripRepository extends JpaRepository<Trip, Long> {
    Trip findByRequestId(Long requestId);

    List<Trip> findByUserId(Long userId);


    @Query("SELECT t FROM Trip t ORDER BY t.startedAt DESC")
    List<Trip> findTopN(Pageable pageable);


    List<Trip> findByVehicleVinNumber(String vehicleVinNumber);
}

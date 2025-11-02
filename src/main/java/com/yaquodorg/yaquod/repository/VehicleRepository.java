package com.yaquodorg.yaquod.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.yaquodorg.yaquod.entity.Vehicle;

public interface VehicleRepository extends JpaRepository<Vehicle, Long> {
    Optional<Vehicle> findByVinNumber(String vinNumber);
}

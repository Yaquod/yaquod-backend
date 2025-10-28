package com.yaquodorg.yaquod.repository;

import com.yaquodorg.yaquod.entity.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VehicleRepository extends JpaRepository<Vehicle, Long> {
    Optional<Vehicle> findByVehicleUUID(String vehicleUUID);
}

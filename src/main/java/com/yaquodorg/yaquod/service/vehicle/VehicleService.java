package com.yaquodorg.yaquod.service.vehicle;

import com.yaquodorg.yaquod.dtos.CreateVehicleDto;
import com.yaquodorg.yaquod.entity.Vehicle;
import com.yaquodorg.yaquod.entity.VehicleStatus;

import java.util.List;
import java.util.Optional;

public interface VehicleService {
    Vehicle createVehicle(CreateVehicleDto createVehicleDto);

    List<Vehicle> getVehicles();

    Vehicle getVehicle(Long id);

    Optional<Vehicle> getVehicleByUUID(String vehicleUUID);

    Vehicle updateVehicle(CreateVehicleDto createVehicleDto);

    void updateVehicleLocation(String vehicleUUID, double longitude, double latitude);

    void updateVehicleStatus(String vehicleUUID, VehicleStatus status);

    void deleteVehicle(Long id);
}

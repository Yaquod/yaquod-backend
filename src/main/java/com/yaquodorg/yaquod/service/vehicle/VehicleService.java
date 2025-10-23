package com.yaquodorg.yaquod.service.vehicle;

import java.util.List;
import java.util.Optional;

import com.yaquodorg.yaquod.dtos.CreateVehicleDto;
import com.yaquodorg.yaquod.entity.Vehicle;

public interface VehicleService {
    Vehicle createVehicle(CreateVehicleDto createVehicleDto);

    List<Vehicle> getVehicles();

    Vehicle getVehicle(Long id);

    Optional<Vehicle> getVehicleByUUID(String vehicleUUID);

    Vehicle updateVehicle(CreateVehicleDto createVehicleDto);

    void deleteVehicle(Long id);
}

package com.yaquodorg.yaquod.service.vehicle;

import java.util.List;
import java.util.Optional;

import com.yaquodorg.yaquod.dtos.CreateVehicleDto;
import com.yaquodorg.yaquod.entity.Vehicle;
import com.yaquodorg.yaquod.entity.VehicleStatus;

public interface VehicleService {
    Vehicle createVehicle(CreateVehicleDto createVehicleDto);

    List<Vehicle> getVehicles();

    Vehicle getVehicle(Long id);

    Optional<Vehicle> getVehicleByVinNumber(String vinNumber);

    Vehicle updateVehicle(CreateVehicleDto createVehicleDto);

    void updateVehicleLocation(String vinNumber, double longitude, double latitude);

    void updateVehicleStatus(String vinNumber, VehicleStatus status);

    void deleteVehicle(Long id);
}

package com.yaquodorg.yaquod.service.vehicle;

import com.yaquodorg.yaquod.dtos.CreateVehicleDto;
import com.yaquodorg.yaquod.entity.User;
import com.yaquodorg.yaquod.entity.Vehicle;
import com.yaquodorg.yaquod.entity.VehicleStatus;
import com.yaquodorg.yaquod.response.CreateVehicleResponse;
import java.util.List;
import java.util.Optional;

public interface VehicleService {
    CreateVehicleResponse createVehicle(CreateVehicleDto createVehicleDto, User user);

    List<Vehicle> getVehicles();

    Vehicle getVehicle(Long id);

    Vehicle getVehicleByApiKey(String apiKey);

    Optional<Vehicle> getVehicleByVinNumber(String vinNumber);

    Vehicle updateVehicle(CreateVehicleDto createVehicleDto);

    void updateVehicleLocation(String vinNumber, double longitude, double latitude);

    void updateVehicleStatus(String vinNumber, VehicleStatus status);

    void deleteVehicle(Long id);

    List<Vehicle> findKNearestVehicles(double longitude, double latitude, int k);

    List<Vehicle> findKNearestVehiclesWithinDistance(double longitude, double latitude, double maxDistanceMeters,
            int k);
}

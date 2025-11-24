package com.yaquodorg.yaquod.service.vehicle;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.yaquodorg.yaquod.dtos.CreateVehicleDto;
import com.yaquodorg.yaquod.entity.Vehicle;
import com.yaquodorg.yaquod.entity.VehicleStatus;
import com.yaquodorg.yaquod.repository.VehicleRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class VehicleServiceImpl implements VehicleService {

    private final VehicleRepository vehicleRepository;

    @Override
    public Vehicle createVehicle(CreateVehicleDto createVehicleDto) {
        Optional<Vehicle> vehicleOptional = vehicleRepository.findByVinNumber(createVehicleDto.getVinNumber());
        if (vehicleOptional.isPresent()) {
            throw new RuntimeException("Vehicle already exists!");
        }

        Vehicle vehicle = new Vehicle();
        buildVehicleFromDto(vehicle, createVehicleDto);

        return vehicleRepository.save(vehicle);
    }

    @Override
    public List<Vehicle> getVehicles() {
        return vehicleRepository.findAll();
    }

    @Override
    public Vehicle getVehicle(Long id) {
        return vehicleRepository.findById(id).orElseThrow(() -> new RuntimeException("Vehicle not found!"));
    }

    @Override
    public Optional<Vehicle> getVehicleByVinNumber(String vinNumber) {
        return vehicleRepository.findByVinNumber(vinNumber);
    }

    @Override
    @Transactional
    public Vehicle updateVehicle(CreateVehicleDto createVehicleDto) {
        Vehicle vehicle = vehicleRepository.findByVinNumber(createVehicleDto.getVinNumber())
                .orElseThrow(() -> new RuntimeException(
                        "Vehicle with VIN " + createVehicleDto.getVinNumber() + " not found!"));

        buildVehicleFromDto(vehicle, createVehicleDto);
        return vehicle;
    }

    @Override
    @Transactional
    public void updateVehicleLocation(String vinNumber, double longitude, double latitude) {
        Date now = new Date();
        Vehicle vehicle = vehicleRepository.findByVinNumber(vinNumber)
                .orElseThrow(() -> new RuntimeException("Vehicle not found with VIN: " + vinNumber));

        GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);
        Point point = geometryFactory.createPoint(new Coordinate(longitude, latitude));

        vehicle.setLastUpdatedLocation(point);
        vehicle.setLastUpdatedLong(longitude);
        vehicle.setLastUpdatedLat(latitude);
        vehicle.setLastUpdatedLocationAt(new Timestamp(now.getTime()));
    }

    @Override
    @Transactional
    public void updateVehicleStatus(String vinNumber, VehicleStatus status) {
        Date now = new Date();
        Vehicle vehicle = vehicleRepository.findByVinNumber(vinNumber)
                .orElseThrow(() -> new RuntimeException("Vehicle not found with VIN: " + vinNumber));
        vehicle.setStatus(status);
        vehicle.setLastUpdatedStatusAt(new Timestamp(now.getTime()));
    }

    @Override
    public void deleteVehicle(Long id) {
        vehicleRepository.deleteById(id);
    }

    @Override
    public List<Vehicle> findKNearestVehicles(double longitude, double latitude, int k) {
        Point point = createPoint(longitude, latitude);
        log.info("Finding {} nearest vehicles to location: ({}, {})", k, latitude, longitude);
        return vehicleRepository.findKNearestVehicles(point, k);
    }

    @Override
    public List<Vehicle> findKNearestVehiclesWithinDistance(double longitude, double latitude, double maxDistanceMeters,
            int k) {
        Point point = createPoint(longitude, latitude);
        log.info("Finding {} nearest vehicles within {} meters of location: ({}, {})",
                k, maxDistanceMeters, latitude, longitude);
        return vehicleRepository.findKNearestVehiclesWithinDistance(point, maxDistanceMeters, k);
    }

    private Point createPoint(double longitude, double latitude) {
        GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);
        return geometryFactory.createPoint(new Coordinate(longitude, latitude));
    }

    private Vehicle buildVehicleFromDto(Vehicle vehicle, CreateVehicleDto dto) {
        vehicle.setVinNumber(dto.getVinNumber());
        vehicle.setPlateNo(dto.getPlateNo());
        vehicle.setColor(dto.getColor());
        vehicle.setCarCompany(dto.getCarCompany());
        vehicle.setModel(dto.getModel());
        vehicle.setSeats(dto.getSeats());
        return vehicle;
    }
}

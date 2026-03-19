package com.yaquodorg.yaquod.service.vehicle;

import com.yaquodorg.yaquod.dtos.CreateVehicleDto;
import com.yaquodorg.yaquod.entity.User;
import com.yaquodorg.yaquod.entity.Vehicle;
import com.yaquodorg.yaquod.entity.VehicleStatus;
import com.yaquodorg.yaquod.exception.ResourceAlreadyExistsException;
import com.yaquodorg.yaquod.exception.ResourceNotFoundException;
import com.yaquodorg.yaquod.repository.VehicleRepository;
import com.yaquodorg.yaquod.response.CreateVehicleResponse;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class VehicleServiceImpl implements VehicleService {

    private final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);

    private final VehicleRepository vehicleRepository;

    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public CreateVehicleResponse createVehicle(CreateVehicleDto createVehicleDto, User user) {
        Optional<Vehicle> vehicleOptional =
                vehicleRepository.findByVinNumber(createVehicleDto.getVinNumber());
        if (vehicleOptional.isPresent()) {
            log.warn(
                    "Attempted to create vehicle with existing VIN: {}",
                    createVehicleDto.getVinNumber());
            throw new ResourceAlreadyExistsException("Vehicle already exists!");
        }

        Vehicle vehicle = new Vehicle();
        CreateVehicleResponse response = buildVehicleFromDto(vehicle, user, createVehicleDto);
        log.info("Creating new vehicle with VIN: {}", createVehicleDto.getVinNumber());
        vehicleRepository.save(vehicle);

        return response;
    }

    @Override
    public List<Vehicle> getVehicles() {
        log.debug("Fetching all vehicles");
        List<Vehicle> vehicles = vehicleRepository.findAll();
        log.debug("Found {} vehicles", vehicles.size());
        return vehicles;
    }

    @Override
    public Vehicle getVehicle(Long id) {
        log.info("Retrieving vehicle with ID: {}", id);
        return vehicleRepository
                .findById(id)
                .orElseThrow(
                        () -> {
                            log.warn("Vehicle not found with ID: {}", id);
                            return new ResourceNotFoundException("Vehicle not found!");
                        });
    }

    @Override
    public Vehicle getVehicleByApiKey(String apiKey) {
        log.info("Retrieving vehicle with apiKey: {}", apiKey);
        return vehicleRepository
                .findByApiKey(apiKey)
                .orElseThrow(
                        () -> {
                            log.warn("Vehicle not found with apiKey: {}", apiKey);
                            return new ResourceNotFoundException("Vehicle not found!");
                        });
    }

    @Override
    public Optional<Vehicle> getVehicleByVinNumber(String vinNumber) {
        log.info("Retrieving vehicle with VIN: {}", vinNumber);
        return vehicleRepository.findByVinNumber(vinNumber);
    }

    @Override
    @Transactional
    public Vehicle updateVehicle(CreateVehicleDto createVehicleDto) {
        Vehicle vehicle =
                vehicleRepository
                        .findByVinNumber(createVehicleDto.getVinNumber())
                        .orElseThrow(
                                () -> {
                                    log.warn(
                                            "Vehicle not found with VIN: {}",
                                            createVehicleDto.getVinNumber());
                                    return new ResourceNotFoundException(
                                            "Vehicle with VIN "
                                                    + createVehicleDto.getVinNumber()
                                                    + " not found!");
                                });

        vehicle.setVinNumber(createVehicleDto.getVinNumber());
        vehicle.setPlateNo(createVehicleDto.getPlateNo());
        vehicle.setColor(createVehicleDto.getColor());
        vehicle.setCarCompany(createVehicleDto.getCarCompany());
        vehicle.setModel(createVehicleDto.getModel());
        vehicle.setSeats(createVehicleDto.getSeats());

        log.info("Updating vehicle with VIN: {}", createVehicleDto.getVinNumber());
        return vehicle;
    }

    @Override
    @Transactional
    public void updateVehicleLocation(String vinNumber, double longitude, double latitude) {
        log.info(
                "Updating location for vehicle VIN: {} to ({}, {})",
                vinNumber,
                longitude,
                latitude);
        Date now = new Date();
        Vehicle vehicle =
                vehicleRepository
                        .findByVinNumber(vinNumber)
                        .orElseThrow(
                                () -> {
                                    log.warn("Vehicle not found with VIN: {}", vinNumber);
                                    return new ResourceNotFoundException(
                                            "Vehicle not found with VIN: " + vinNumber);
                                });

        Point point = geometryFactory.createPoint(new Coordinate(longitude, latitude));

        vehicle.setLastUpdatedLocation(point);
        vehicle.setLastUpdatedLong(longitude);
        vehicle.setLastUpdatedLat(latitude);
        vehicle.setLastUpdatedLocationAt(new Timestamp(now.getTime()));
        log.debug("Location updated successfully for vehicle VIN: {}", vinNumber);
    }

    @Override
    @Transactional
    public void updateVehicleStatus(String vinNumber, VehicleStatus status) {
        log.info("Updating status for vehicle VIN: {} to {}", vinNumber, status);
        Date now = new Date();
        Vehicle vehicle =
                vehicleRepository
                        .findByVinNumber(vinNumber)
                        .orElseThrow(
                                () -> {
                                    log.warn("Vehicle not found with VIN: {}", vinNumber);
                                    return new ResourceNotFoundException(
                                            "Vehicle not found with VIN: " + vinNumber);
                                });
        vehicle.setStatus(status);
        vehicle.setLastUpdatedStatusAt(new Timestamp(now.getTime()));
        log.debug("Status updated successfully for vehicle VIN: {}", vinNumber);
    }

    @Override
    public void deleteVehicle(Long id) {
        vehicleRepository.deleteById(id);
        log.debug("Vehicle deleted successfully with ID: {}", id);
    }

    @Override
    public List<Vehicle> findKNearestVehicles(double longitude, double latitude, int k) {
        log.info("Finding {} nearest vehicles to location: ({}, {})", k, longitude, latitude);
        Point point = createPoint(longitude, latitude);
        List<Vehicle> vehicles = vehicleRepository.findKNearestVehicles(point, k);
        log.debug("Found {} nearest vehicles", vehicles.size());
        return vehicles;
    }

    @Override
    public List<Vehicle> findKNearestVehiclesWithinDistance(
            double longitude, double latitude, double maxDistanceMeters, int k) {
        log.info(
                "Finding {} nearest vehicles within {} meters of location: ({}, {})",
                k,
                maxDistanceMeters,
                longitude,
                latitude);
        Point point = createPoint(longitude, latitude);
        List<Vehicle> vehicles =
                vehicleRepository.findKNearestVehiclesWithinDistance(point, maxDistanceMeters, k);
        log.debug("Found {} vehicles within distance", vehicles.size());
        return vehicles;
    }

    private Point createPoint(double longitude, double latitude) {
        return geometryFactory.createPoint(new Coordinate(longitude, latitude));
    }

    private CreateVehicleResponse buildVehicleFromDto(
            Vehicle vehicle, User user, CreateVehicleDto dto) {
        Date now = new Date();

        vehicle.setVinNumber(dto.getVinNumber());
        vehicle.setPlateNo(dto.getPlateNo());
        vehicle.setColor(dto.getColor());
        vehicle.setCarCompany(dto.getCarCompany());
        vehicle.setModel(dto.getModel());
        vehicle.setSeats(dto.getSeats());
        vehicle.setCreatedAt(new Timestamp(now.getTime()));
        vehicle.setCreatedByAdmin(user);
        CreateVehicleResponse credentials = generateCredentials(vehicle);

        return credentials;
    }

    private CreateVehicleResponse generateCredentials(Vehicle vehicle) {
        String apiKey = "VEH_" + UUID.randomUUID().toString();
        String apiSecret = generateSecureSecret();
        String apiSecretHash = passwordEncoder.encode(apiSecret);

        vehicle.setApiKey(apiKey);
        vehicle.setApiSecretHash(apiSecretHash);

        return CreateVehicleResponse.builder()
                .vehicle(vehicle)
                .apiKey(apiKey)
                .apiSecret(apiSecret)
                .build();
    }

    private String generateSecureSecret() {
        String randomString = UUID.randomUUID().toString() + System.currentTimeMillis();
        return DigestUtils.sha256Hex(randomString);
    }
}

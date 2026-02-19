package com.yaquodorg.yaquod.service.trip;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

import org.locationtech.jts.geom.Point;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.yaquodorg.yaquod.dtos.InitTripDto;
import com.yaquodorg.yaquod.dtos.MoveVehicleDto;
import com.yaquodorg.yaquod.entity.Request;
import com.yaquodorg.yaquod.entity.Trip;
import com.yaquodorg.yaquod.entity.TripStatus;
import com.yaquodorg.yaquod.entity.User;
import com.yaquodorg.yaquod.entity.Vehicle;
import com.yaquodorg.yaquod.entity.VehicleStatus;
import com.yaquodorg.yaquod.repository.TripRepository;
import com.yaquodorg.yaquod.service.user.UserService;
import com.yaquodorg.yaquod.service.vehicle.VehicleService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class TripServiceImpl implements TripService {

    private final TripRepository tripRepository;

    private final VehicleService vehicleService;

    private final UserService userService;

    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    @Override
    public void createTrip(Request request, double startLong, double startLat, double endLong, double endLat) {
        // get request's user
        User user = request.getUser();

        // match vehicle
        List<Vehicle> vehicles = vehicleService.findKNearestVehicles(startLong, startLat, 1);
        if (vehicles.isEmpty()) {
            throw new RuntimeException("No vehicles available for the requested location");
        }

        // get vin number and set vehicle state as processing
        Vehicle vehicle = vehicles.getFirst();
        String vinNumber = vehicle.getVinNumber();
        vehicleService.updateVehicleStatus(vinNumber, VehicleStatus.PROCESSING);

        // build dto
        InitTripDto initTripDto = InitTripDto.builder()
                .vinNumber(vinNumber)
                .requestId(request.getId())
                .startLong(startLong)
                .startLat(startLat)
                .endLong(endLong)
                .endLat(endLat)
                .build();

        // save the trip to the database
        tripRepository.save(Trip.builder()
                .request(request)
                .vehicle(vehicle)
                .user(user)
                .status(TripStatus.INITIATED)
                .startedAt(new Timestamp(new Date().getTime()))
                .build());

        // publish to broker
        eventPublisher.publishEvent(initTripDto);
    }

    @Override
    public Trip getTripByRequestId(Long requestId) {
        Trip trip = tripRepository.findByRequestId(requestId);
        if (trip == null) {
            log.error("Trip not found for requestId: {}", requestId);
            throw new RuntimeException("Trip not found for requestId: " + requestId);
        }

        return trip;
    }

    @Override
    public Trip getTripById(Long id) {
        return tripRepository.findById(id).orElseThrow(() -> {
            log.error("Trip not found for id: {}", id);
            return new RuntimeException("Trip not found for id: " + id);
        });
    }

    @Override
    @Transactional
    public void updateTripStatus(Long id, TripStatus tripStatus) {
        Date now = new Date();
        Trip trip = tripRepository.findById(id).orElseThrow(() -> new RuntimeException("Trip not found!"));
        trip.setStatus(tripStatus);
        trip.setUpdatedAt(new Timestamp(now.getTime()));
    }

    @Override
    public void deleteTripById(Long id) {
        tripRepository.deleteById(id);
    }

    @Override
    public List<Trip> getAllTrips() {
        return tripRepository.findAll();
    }

    @Override
    public List<Trip> getTripsByUserId(Long userId) {
        User user = userService.getUserById(userId);
        if (user == null) {
            log.error("User not found for id: {}", userId);
            throw new RuntimeException("User not found for id: " + userId);
        }

        return tripRepository.findByUserId(userId);
    }

    @Override
    public List<Trip> getUserLastNTrips(int n, Long userId) {
        return tripRepository.findByUserIdOrderByStartedAtDesc(userId, PageRequest.of(0, n));
    }

    @Override
    public List<Trip> getTripsByVinNumber(String vinNumber) {
        Vehicle vehicle = vehicleService.getVehicleByVinNumber(vinNumber).orElseThrow(
                () -> new RuntimeException("Vehicle not found for vin number: " + vinNumber));

        return tripRepository.findByVehicleVinNumber(vehicle.getVinNumber());
    }

    @Transactional
    @Override
    public void startTrip(Long requestId) {
        Trip trip = getTripByRequestId(requestId);
        Long tripId = trip.getId();

        Vehicle vehicle = trip.getVehicle();
        if (vehicle == null) {
            log.error("No vehicle was matched with trip: {}", tripId);
            throw new RuntimeException("No vehicle was matched with trip: " + tripId);
        }
        String vinNumber = vehicle.getVinNumber();

        Request request = trip.getRequest();
        if (request == null) {
            log.error("Trip: {} was not assigned with a request", tripId);
            throw new RuntimeException("Trip: " + tripId + " was not assigned with a request");
        }
        Point destinationLocation = request.getDestinationLocation();

        // TODO: I think we should validate the current states of both the trip and the
        // vehicle before ordering the vehicle to move and update their statuses

        // Send moving signal to the vehicle with the destination location
        MoveVehicleDto moveVehicleDto = buildMoveVehicleDto(vinNumber, tripId, destinationLocation);
        eventPublisher.publishEvent(moveVehicleDto);

        // Update vehicle and trip statuses
        vehicleService.updateVehicleStatus(vinNumber, VehicleStatus.IN_USE);
        updateTripStatus(tripId, TripStatus.IN_PROGRESS);
    }

    private MoveVehicleDto buildMoveVehicleDto(String vinNumber, Long tripId, Point destinationLocation) {
        double destinationLat = destinationLocation.getY();
        double destinationLong = destinationLocation.getX();

        return MoveVehicleDto.builder()
                .vinNumber(vinNumber)
                .tripId(tripId)
                .latitude(destinationLat)
                .longitude(destinationLong)
                .build();
    }
}

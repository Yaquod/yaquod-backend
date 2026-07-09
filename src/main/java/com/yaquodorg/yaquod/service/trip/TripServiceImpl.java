package com.yaquodorg.yaquod.service.trip;

import com.yaquodorg.yaquod.dtos.trip.InitTripDto;
import com.yaquodorg.yaquod.dtos.vehicle.MoveVehicleDto;
import com.yaquodorg.yaquod.dtos.vehicle.VehicleDto;
import com.yaquodorg.yaquod.entity.*;
import com.yaquodorg.yaquod.exception.ResourceNotFoundException;
import com.yaquodorg.yaquod.exception.ServiceUnavailableException;
import com.yaquodorg.yaquod.repository.TripRepository;
import com.yaquodorg.yaquod.service.user.UserService;
import com.yaquodorg.yaquod.service.vehicle.VehicleService;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Point;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Service
@RequiredArgsConstructor
@Slf4j
public class TripServiceImpl implements TripService {

    private final TripRepository tripRepository;

    private final VehicleService vehicleService;

    private final UserService userService;

    private final ApplicationEventPublisher eventPublisher;

    private final Map<Long, SseEmitter> emitters = new ConcurrentHashMap<>();

    @Transactional
    @Override
    public void createTrip(
            Request request, double startLong, double startLat, double endLong, double endLat) {
        log.info(
                "Creating trip for request id: {}, start: ({}, {}), end: ({}, {})",
                request.getId(),
                startLong,
                startLat,
                endLong,
                endLat);

        // get request's user
        User user = request.getUser();
        log.debug("Trip user id: {}", user.getId());

        // match vehicle
        List<Vehicle> vehicles = vehicleService.findKNearestVehicles(startLong, startLat, 1);
        if (vehicles.isEmpty()) {
            log.error("No vehicles available for location: ({}, {})", startLong, startLat);
            throw new ServiceUnavailableException(
                    "No vehicles available for the requested location");
        }

        // get vin number and set vehicle state as processing
        Vehicle vehicle = vehicles.getFirst();
        String vinNumber = vehicle.getVinNumber();
        log.debug("Matched vehicle with VIN: {}", vinNumber);
        vehicleService.updateVehicleStatus(vinNumber, VehicleStatus.PROCESSING);

        // build dto
        InitTripDto initTripDto =
                InitTripDto.builder()
                        .vinNumber(vinNumber)
                        .requestId(request.getId())
                        .startLong(startLong)
                        .startLat(startLat)
                        .endLong(endLong)
                        .endLat(endLat)
                        .build();

        // save the trip to the database
        Trip savedTrip =
                tripRepository.save(
                        Trip.builder()
                                .request(request)
                                .vehicle(vehicle)
                                .user(user)
                                .status(TripStatus.INITIATED)
                                .startedAt(new Timestamp(new Date().getTime()))
                                .build());
        log.info("Trip created successfully with id: {}", savedTrip.getId());

        // publish to broker
        eventPublisher.publishEvent(initTripDto);
        log.debug("Published InitTripDto event for request id: {}", request.getId());
    }

    @Override
    public Trip getTripByRequestId(Long requestId) {
        log.debug("Fetching trip by request id: {}", requestId);
        Trip trip = tripRepository.findByRequestId(requestId);
        if (trip == null) {
            log.error("Trip not found for requestId: {}", requestId);
            throw new ResourceNotFoundException("Trip not found for requestId: " + requestId);
        }
        log.debug("Found trip id: {} for request id: {}", trip.getId(), requestId);
        return trip;
    }

    @Override
    public Trip getTripById(Long id) {
        log.debug("Fetching trip by id: {}", id);
        return tripRepository
                .findById(id)
                .orElseThrow(
                        () -> {
                            log.error("Trip not found for id: {}", id);
                            return new ResourceNotFoundException("Trip not found for id: " + id);
                        });
    }

    @Override
    @Transactional
    public void updateTripStatus(Long id, TripStatus tripStatus) {
        log.info("Updating trip status for trip id: {} to {}", id, tripStatus);
        Date now = new Date();
        Trip trip =
                tripRepository
                        .findById(id)
                        .orElseThrow(
                                () -> {
                                    log.error("Trip not found for id: {}", id);
                                    return new ResourceNotFoundException("Trip not found!");
                                });
        trip.setStatus(tripStatus);
        trip.setUpdatedAt(new Timestamp(now.getTime()));
        log.debug("Trip status updated successfully for trip id: {}", id);
    }

    @Override
    public void deleteTripById(Long id) {
        log.info("Deleting trip with id: {}", id);
        tripRepository.deleteById(id);
        log.debug("Trip deleted successfully with id: {}", id);
    }

    @Override
    public List<Trip> getAllTrips() {
        log.debug("Fetching all trips");
        List<Trip> trips = tripRepository.findAll();
        log.debug("Found {} trips", trips.size());
        return trips;
    }

    @Override
    public List<Trip> getTripsByUserId(Long userId) {
        log.debug("Fetching trips for user id: {}", userId);
        User user = userService.getUserById(userId);

        List<Trip> trips = tripRepository.findByUserId(userId);
        log.debug("Found {} trips for user id: {}", trips.size(), userId);
        return trips;
    }

    @Override
    public List<Trip> getUserLastNTrips(int n, Long userId) {
        log.debug("Fetching last {} trips for user id: {}", n, userId);
        List<Trip> trips =
                tripRepository.findByUserIdOrderByStartedAtDesc(userId, PageRequest.of(0, n));
        log.debug("Found {} trips for user id: {}", trips.size(), userId);
        return trips;
    }

    @Override
    public List<Trip> getTripsByVinNumber(String vinNumber) {
        log.debug("Fetching trips for vehicle VIN: {}", vinNumber);
        Vehicle vehicle =
                vehicleService
                        .getVehicleByVinNumber(vinNumber)
                        .orElseThrow(
                                () -> {
                                    log.error("Vehicle not found for VIN: {}", vinNumber);
                                    return new ResourceNotFoundException(
                                            "Vehicle not found for vin number: " + vinNumber);
                                });

        List<Trip> trips = tripRepository.findByVehicleVinNumber(vehicle.getVinNumber());
        log.debug("Found {} trips for vehicle VIN: {}", trips.size(), vinNumber);
        return trips;
    }

    @Override
    public long countTrips() {
        return tripRepository.count();
    }

    @Override
    public long countTripsByStatusIn(List<TripStatus> statuses) {
        return tripRepository.countByStatusIn(statuses);
    }

    @Override
    public SseEmitter subscribeToLocationStream(Long tripId) {
        log.info("A user has subscribed to vehicle location stream assigned to trip: {}", tripId);
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
        emitters.put(tripId, emitter);
        emitter.onCompletion(() -> emitters.remove(tripId));
        emitter.onTimeout(() -> emitters.remove(tripId));
        return emitter;
    }

    @Override
    public void unsubscribeToLocationStream(Long tripId) {
        SseEmitter emitter = emitters.get(tripId);
        if (emitter != null) {
            emitter.complete();
            emitters.remove(tripId);
        }

        log.info("A user has unsubscribed to vehicle location stream assigned to trip: {}", tripId);
    }

    @Override
    public void broadcastLocationStream(Long tripId, double latitude, double longitude) {
        SseEmitter emitter = emitters.get(tripId);
        if (emitter != null) {
            try {
                emitter.send(Map.of("lat", latitude, "lon", longitude));
                log.info("location streamed: {}, {} for trip: {} ", latitude, longitude, tripId);
            } catch (IOException e) {
                emitters.remove(tripId);
                log.error("Exception thrown while trying to stream location: {}", e);
            }
        }
    }

    @Transactional
    @Override
    public void startTrip(Long requestId) {
        log.info("Starting trip for request id: {}", requestId);
        Trip trip = getValidatedTrip(requestId);
        Vehicle vehicle = trip.getVehicle();
        Long tripId = trip.getId();
        String vinNumber = trip.getVehicle().getVinNumber();
        Point destinationLocation = trip.getRequest().getDestinationLocation();
        log.debug(
                "Trip id: {}, VIN: {}, destination: ({}, {})",
                tripId,
                vinNumber,
                destinationLocation.getX(),
                destinationLocation.getY());

        if (trip.getStatus() != TripStatus.ARRIVED_AT_PICKUP
                && vehicle.getStatus() != VehicleStatus.WAITING_PASSENGER) {
            log.error("Trip id: {} status was not in ARRIVED_AT_PICKUP state.", tripId);
            throw new RuntimeException(
                    "Trip id: " + tripId + " status was not in ARRIVED_AT_PICKUP state.");
        }

        // Send moving signal to the vehicle with the destination location
        MoveVehicleDto moveVehicleDto = buildMoveVehicleDto(vinNumber, tripId, destinationLocation);
        eventPublisher.publishEvent(moveVehicleDto);
        log.debug("Published MoveVehicleDto event for trip id: {}", tripId);

        // Update vehicle and trip statuses
        vehicleService.updateVehicleStatus(vinNumber, VehicleStatus.IN_USE);
        updateTripStatus(tripId, TripStatus.IN_PROGRESS);
        log.info("Trip started successfully for request id: {}", requestId);
    }

    @Override
    @Transactional
    public void endTrip(Long requestId) {
        log.info("Ending trip for request id: {}", requestId);
        Trip trip = getValidatedTrip(requestId);
        Long tripId = trip.getId();
        String vinNumber = trip.getVehicle().getVinNumber();
        log.debug("Trip id: {}, VIN: {}", tripId, vinNumber);

        VehicleDto vehicleDto = VehicleDto.builder().vinNumber(vinNumber).build();

        eventPublisher.publishEvent(vehicleDto);
        log.debug("Published VehicleDto event for trip id: {}", tripId);

        // Update vehicle and trip statuses
        vehicleService.updateVehicleStatus(vinNumber, VehicleStatus.IDLE);
        updateTripStatus(tripId, TripStatus.COMPLETED);
        log.info("Trip ended successfully for request id: {}", requestId);
    }

    private Trip getValidatedTrip(Long requestId) {
        log.debug("Validating trip for request id: {}", requestId);
        Trip trip = getTripByRequestId(requestId);
        Long tripId = trip.getId();

        Vehicle vehicle = trip.getVehicle();
        if (vehicle == null) {
            log.error("No vehicle was matched with trip: {}", tripId);
            throw new IllegalStateException("No vehicle was matched with trip: " + tripId);
        }

        Request request = trip.getRequest();
        if (request == null) {
            log.error("Trip: {} was not assigned with a request", tripId);
            throw new IllegalStateException("Trip: " + tripId + " was not assigned with a request");
        }

        log.debug("Trip validated successfully for trip id: {}", tripId);
        return trip;
    }

    private MoveVehicleDto buildMoveVehicleDto(
            String vinNumber, Long tripId, Point destinationLocation) {
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

package com.yaquodorg.yaquod.service.request;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.yaquodorg.yaquod.dtos.MoveVehicleDto;
import com.yaquodorg.yaquod.entity.Request;
import com.yaquodorg.yaquod.entity.RequestStatus;
import com.yaquodorg.yaquod.entity.Trip;
import com.yaquodorg.yaquod.entity.TripStatus;
import com.yaquodorg.yaquod.entity.User;
import com.yaquodorg.yaquod.entity.Vehicle;
import com.yaquodorg.yaquod.entity.VehicleStatus;
import com.yaquodorg.yaquod.repository.RequestRepository;
import com.yaquodorg.yaquod.service.trip.TripService;
import com.yaquodorg.yaquod.service.user.UserService;
import com.yaquodorg.yaquod.service.vehicle.VehicleService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class RequestServiceImpl implements RequestService {

    private final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);
    private final ApplicationEventPublisher eventPublisher;

    private final RequestRepository requestRepository;

    private final UserService userService;
    private final TripService tripService;
    private final VehicleService vehicleService;

    @Transactional
    @Override
    public Request createRequest(Long userId, double startLong, double startLat, double endLong, double endLat) {
        User user = userService.getUserById(userId);

        Point startPoint = geometryFactory.createPoint(new Coordinate(startLong, startLat));
        Point endPoint = geometryFactory.createPoint(new Coordinate(endLong, endLat));

        Request request = Request.builder().user(user).startLocation(startPoint).destinationLocation(endPoint)
                .status(RequestStatus.PENDING).createdAt(new Timestamp(new Date().getTime())).build();

        Request savedRequest = requestRepository.save(request);
        tripService.createTrip(savedRequest, startLong, startLat, endLong, endLat);

        return savedRequest;
    }

    @Override
    public List<Request> getRequests() {
        return requestRepository.findAll();
    }

    @Override
    public List<Request> getUserRequests(Long userId) {
        User user = userService.getUserById(userId);
        return user.getRequests();
    }

    @Override
    public Request getRequest(Long requestId) {
        return requestRepository.findById(requestId).orElseThrow(() -> new RuntimeException("Request not found!"));
    }

    @Transactional
    @Override
    public void updateRequest(Long requestId, RequestStatus requestStatus, double estimatedTime, double estimatedFare) {
        Request request = requestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Request not found!"));

        request.setStatus(requestStatus);
        request.setEstimatedTime(estimatedTime);
        request.setEstimatedFare(estimatedFare);
    }

    @Override
    @Transactional
    public void updateRequestStatus(Long requestId, RequestStatus requestStatus) {
        Request request = requestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Request not found!"));

        request.setStatus(requestStatus);
    }

    @Override
    public void deleteRequest(Long requestId) {
        requestRepository.deleteById(requestId);
    }

    @Override
    @Transactional
    public void declineRequestById(Long id, Long userId) {
        Request request = getRequest(id);
        if (!request.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("Unauthorized to decline this request");
        }

        Trip trip = request.getTrip();
        if (trip == null) {
            throw new RuntimeException("No trip associated with request " + id);
        }
        long tripId = trip.getId();

        Vehicle vehicle = trip.getVehicle();
        if (vehicle == null) {
            throw new RuntimeException("No vehicle associated with trip of request " + id);
        }
        String vinNumber = vehicle.getVinNumber();

        if (request.getStatus() != RequestStatus.COMPLETED) {
            throw new IllegalStateException("Request is not in COMPLETED state");
        }
        if (trip.getStatus() != TripStatus.INITIATED) {
            throw new IllegalStateException("Trip is not in INITIATED state");
        }
        if (vehicle.getStatus() != VehicleStatus.ON_HOLD) {
            throw new IllegalStateException("Vehicle is not in ON_HOLD state");
        }

        updateRequestStatus(id, RequestStatus.DECLINED);
        log.info("Request with id {} has changed to DECLINED.", id);

        tripService.updateTripStatus(tripId, TripStatus.CANCELLED_BY_PASSENGER);
        log.info("Trip with id {} has changed to CANCELLED_BY_PASSENGER", tripId);

        vehicleService.updateVehicleStatus(vinNumber, VehicleStatus.IDLE);
        log.info("Vehicle with vinNumber {} has been changed to IDLE", vinNumber);
    }

    @Override
    @Transactional
    public Request acceptRequestById(Long id, Long userId) {
        Request request = getRequest(id);
        if (!request.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("Unauthorized to accept this request");
        }

        Trip trip = request.getTrip();
        if (trip == null) {
            throw new RuntimeException("No trip associated with request " + id);
        }
        long tripId = trip.getId();

        Vehicle vehicle = trip.getVehicle();
        if (vehicle == null) {
            throw new RuntimeException("No vehicle associated with trip of request " + id);
        }
        String vinNumber = vehicle.getVinNumber();

        Point startLocation = request.getStartLocation();

        if (request.getStatus() != RequestStatus.COMPLETED) {
            throw new IllegalStateException("Request is not in COMPLETED state");
        }
        if (trip.getStatus() != TripStatus.INITIATED) {
            throw new IllegalStateException("Trip is not in INITIATED state");
        }
        if (vehicle.getStatus() != VehicleStatus.ON_HOLD) {
            throw new IllegalStateException("Vehicle is not in ON_HOLD state");
        }

        // publish to broker
        MoveVehicleDto moveVehicleDto = generateVehicleMovementDto(startLocation, tripId, vinNumber);
        eventPublisher.publishEvent(moveVehicleDto);

        // Update statuses
        updateRequestStatus(id, RequestStatus.ACCEPTED);
        log.info("Request with id {} has changed to ACCEPTED.", id);

        tripService.updateTripStatus(tripId, TripStatus.VEHICLE_ON_WAY);
        log.info("Trip with id {} has changed to VEHICLE_ON_WAY", tripId);

        vehicleService.updateVehicleStatus(vinNumber, VehicleStatus.ON_WAY);
        log.info("Vehicle with vinNumber {} has been changed to ON_WAY", vinNumber);

        return request;
    }

    private MoveVehicleDto generateVehicleMovementDto(Point startLocation, Long tripId, String vinNumber) {
        double startLat = startLocation.getY();
        double startLong = startLocation.getX();

        return MoveVehicleDto.builder()
                .vinNumber(vinNumber)
                .tripId(tripId)
                .latitude(startLat)
                .longitude(startLong)
                .build();
    }
}

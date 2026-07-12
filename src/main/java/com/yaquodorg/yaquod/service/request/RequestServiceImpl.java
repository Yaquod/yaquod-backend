package com.yaquodorg.yaquod.service.request;

import com.yaquodorg.yaquod.dtos.admin.RequestDto;
import com.yaquodorg.yaquod.dtos.trip.TripCancelDto;
import com.yaquodorg.yaquod.dtos.vehicle.MoveVehicleDto;
import com.yaquodorg.yaquod.entity.*;
import com.yaquodorg.yaquod.exception.ResourceNotFoundException;
import com.yaquodorg.yaquod.repository.RequestRepository;
import com.yaquodorg.yaquod.service.redis.RedisService;
import com.yaquodorg.yaquod.service.trip.TripService;
import com.yaquodorg.yaquod.service.user.UserService;
import com.yaquodorg.yaquod.service.vehicle.VehicleService;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private final RedisService redisService;

    @Value("${app.eta.timeout-prefix}")
    private String ETA_TIMEOUT_PREFIX;

    @Value("${app.request.timeout-prefix}")
    private String REQUEST_TIMEOUT_PREFIX;

    @Transactional
    @Override
    public Request createRequest(
            Long userId, double startLong, double startLat, double endLong, double endLat) {
        log.info(
                "Creating request for user id: {}, start: ({}, {}), end: ({}, {})",
                userId,
                startLong,
                startLat,
                endLong,
                endLat);
        User user = userService.getUserById(userId);

        Point startPoint = geometryFactory.createPoint(new Coordinate(startLong, startLat));
        Point endPoint = geometryFactory.createPoint(new Coordinate(endLong, endLat));

        Request request =
                Request.builder()
                        .user(user)
                        .startLocation(startPoint)
                        .destinationLocation(endPoint)
                        .status(RequestStatus.PENDING)
                        .createdAt(new Timestamp(new Date().getTime()))
                        .build();

        Request savedRequest = requestRepository.save(request);
        log.info("Request created successfully with id: {}", savedRequest.getId());

        tripService.createTrip(savedRequest, startLong, startLat, endLong, endLat);
        log.debug("Trip created for request id: {}", savedRequest.getId());

        return savedRequest;
    }

    @Override
    public List<Request> getRequests() {
        log.debug("Fetching all requests");
        List<Request> requests = requestRepository.findAll();
        log.debug("Found {} requests", requests.size());
        return requests;
    }

    @Override
    public List<Request> getRequestsWithTripAndVehicle() {
        log.debug("Fetching all requests with trip and vehicle");
        List<Request> requests = requestRepository.findAllWithTripAndVehicle();
        log.debug("Found {} requests", requests.size());
        return requests;
    }

    @Override
    public Page<RequestDto> getUserRequestsPaginated(Pageable pageable, Long userId) {
        log.debug("Fetching paginated requests for user id: {}", userId);
        return requestRepository.findByUserId(userId, pageable).map(RequestDto::fromEntity);
    }

    @Override
    public List<Request> getUserRequests(Long userId) {
        log.debug("Fetching requests for user id: {}", userId);
        User user = userService.getUserById(userId);
        List<Request> requests = user.getRequests();
        log.debug("Found {} requests for user id: {}", requests.size(), userId);
        return requests;
    }

    @Override
    public Request getRequest(Long requestId) {
        log.debug("Fetching request by id: {}", requestId);
        return requestRepository
                .findById(requestId)
                .orElseThrow(
                        () -> {
                            log.error("Request not found for id: {}", requestId);
                            return new ResourceNotFoundException("Request not found!");
                        });
    }

    @Transactional
    @Override
    public void updateRequest(
            Long requestId,
            RequestStatus requestStatus,
            double estimatedTime,
            double estimatedFare) {
        log.info(
                "Updating request id: {} with status: {}, estimatedTime: {}, estimatedFare: {}",
                requestId,
                requestStatus,
                estimatedTime,
                estimatedFare);
        Request request =
                requestRepository
                        .findById(requestId)
                        .orElseThrow(
                                () -> {
                                    log.error("Request not found for id: {}", requestId);
                                    return new ResourceNotFoundException("Request not found!");
                                });

        request.setStatus(requestStatus);
        request.setEstimatedTime(estimatedTime);
        request.setEstimatedFare(estimatedFare);
        log.debug("Request updated successfully for id: {}", requestId);
    }

    @Override
    @Transactional
    public void updateRequestStatus(Long requestId, RequestStatus requestStatus) {
        log.info("Updating request status for id: {} to {}", requestId, requestStatus);
        Request request =
                requestRepository
                        .findById(requestId)
                        .orElseThrow(
                                () -> {
                                    log.error("Request not found for id: {}", requestId);
                                    return new ResourceNotFoundException("Request not found!");
                                });

        request.setStatus(requestStatus);
        log.debug("Request status updated successfully for id: {}", requestId);
    }

    @Override
    public long countRequestsByStatusIn(List<RequestStatus> statuses) {
        return requestRepository.countByStatusIn(statuses);
    }

    @Override
    public void deleteRequest(Long requestId) {
        log.info("Deleting request with id: {}", requestId);
        requestRepository.deleteById(requestId);
        log.debug("Request deleted successfully with id: {}", requestId);
    }

    @Override
    @Transactional
    public void cancelRequest(Long id, Long userId) {
        log.info("Attempting to cancel request id: {} by user id: {}", id, userId);
        Request request = getRequest(id);
        User actor = userService.getUserById(userId);
        if (actor.getRole() != Role.ADMIN && !request.getUser().getId().equals(userId)) {
            log.warn("Unauthorized attempt to cancel request id: {} by user id: {}", id, userId);
            throw new AccessDeniedException("Unauthorized to cancel this request");
        }

        Trip trip = request.getTrip();
        if (trip == null) {
            log.error("No trip associated with request id: {}", id);
            throw new IllegalStateException("No trip associated with request " + id);
        }
        long tripId = trip.getId();

        Vehicle vehicle = trip.getVehicle();
        if (vehicle == null) {
            log.error("No vehicle associated with trip of request id: {}", id);
            throw new IllegalStateException("No vehicle associated with trip of request " + id);
        }
        String vinNumber = vehicle.getVinNumber();

        if (request.getStatus() != RequestStatus.PENDING) {
            log.warn(
                    "Request id: {} is not in PENDING state, current status: {}",
                    id,
                    request.getStatus());
            throw new IllegalStateException("Request is not in PENDING state");
        }
        if (trip.getStatus() != TripStatus.INITIATED) {
            log.warn(
                    "Trip id: {} is not in INITIATED state, current status: {}",
                    tripId,
                    trip.getStatus());
            throw new IllegalStateException("Trip is not in INITIATED state");
        }
        if (vehicle.getStatus() != VehicleStatus.PROCESSING) {
            log.warn(
                    "Vehicle VIN: {} is not in PROCESSING state, current status: {}",
                    vinNumber,
                    vehicle.getStatus());
            throw new IllegalStateException("Vehicle is not in PROCESSING state");
        }

        redisService.delete(ETA_TIMEOUT_PREFIX + id);

        TripCancelDto tripCancelDto =
                TripCancelDto.builder().vinNumber(vinNumber).requestId(id).build();

        eventPublisher.publishEvent(tripCancelDto);
        log.info("Published TripCancelDto event for request id: {}", id);

        updateRequestStatus(id, RequestStatus.CANCELLED);
        log.info("Request with id {} has changed to CANCELLED.", id);

        TripStatus status =
                actor.getRole() == Role.ADMIN
                        ? TripStatus.CANCELLED_BY_SYSTEM
                        : TripStatus.CANCELLED_BY_PASSENGER;
        tripService.updateTripStatus(tripId, status);
        log.info("Trip with id {} has changed to {}", tripId, status);

        vehicleService.updateVehicleStatus(vinNumber, VehicleStatus.IDLE);
        log.info("Vehicle with vinNumber {} has been changed to IDLE", vinNumber);
    }

    @Override
    @Transactional
    public void declineRequestById(Long id, Long userId) {
        log.info("Attempting to decline request id: {} by user id: {}", id, userId);
        Request request = getRequest(id);
        if (!request.getUser().getId().equals(userId)) {
            log.warn("Unauthorized attempt to decline request id: {} by user id: {}", id, userId);
            throw new AccessDeniedException("Unauthorized to decline this request");
        }

        Trip trip = request.getTrip();
        if (trip == null) {
            log.error("No trip associated with request id: {}", id);
            throw new IllegalStateException("No trip associated with request " + id);
        }
        long tripId = trip.getId();

        Vehicle vehicle = trip.getVehicle();
        if (vehicle == null) {
            log.error("No vehicle associated with trip of request id: {}", id);
            throw new IllegalStateException("No vehicle associated with trip of request " + id);
        }
        String vinNumber = vehicle.getVinNumber();

        if (request.getStatus() != RequestStatus.COMPLETED) {
            log.warn(
                    "Request id: {} is not in COMPLETED state, current status: {}",
                    id,
                    request.getStatus());
            throw new IllegalStateException("Request is not in COMPLETED state");
        }
        if (trip.getStatus() != TripStatus.INITIATED) {
            log.warn(
                    "Trip id: {} is not in INITIATED state, current status: {}",
                    tripId,
                    trip.getStatus());
            throw new IllegalStateException("Trip is not in INITIATED state");
        }
        if (vehicle.getStatus() != VehicleStatus.ON_HOLD) {
            log.warn(
                    "Vehicle VIN: {} is not in ON_HOLD state, current status: {}",
                    vinNumber,
                    vehicle.getStatus());
            throw new IllegalStateException("Vehicle is not in ON_HOLD state");
        }

        redisService.delete(REQUEST_TIMEOUT_PREFIX + id);

        TripCancelDto tripCancelDto =
                TripCancelDto.builder().vinNumber(vinNumber).requestId(id).build();

        eventPublisher.publishEvent(tripCancelDto);
        log.info("Published TripCancelDto event for request id: {}", id);

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
        log.info("Attempting to accept request id: {} by user id: {}", id, userId);
        Request request = getRequest(id);
        if (!request.getUser().getId().equals(userId)) {
            log.warn("Unauthorized attempt to accept request id: {} by user id: {}", id, userId);
            throw new AccessDeniedException("Unauthorized to accept this request");
        }

        Trip trip = request.getTrip();
        if (trip == null) {
            log.error("No trip associated with request id: {}", id);
            throw new IllegalStateException("No trip associated with request " + id);
        }
        long tripId = trip.getId();

        Vehicle vehicle = trip.getVehicle();
        if (vehicle == null) {
            log.error("No vehicle associated with trip of request id: {}", id);
            throw new IllegalStateException("No vehicle associated with trip of request " + id);
        }
        String vinNumber = vehicle.getVinNumber();

        Point startLocation = request.getStartLocation();

        if (request.getStatus() != RequestStatus.COMPLETED) {
            log.warn(
                    "Request id: {} is not in COMPLETED state, current status: {}",
                    id,
                    request.getStatus());
            throw new IllegalStateException("Request is not in COMPLETED state");
        }
        if (trip.getStatus() != TripStatus.INITIATED) {
            log.warn(
                    "Trip id: {} is not in INITIATED state, current status: {}",
                    tripId,
                    trip.getStatus());
            throw new IllegalStateException("Trip is not in INITIATED state");
        }
        if (vehicle.getStatus() != VehicleStatus.ON_HOLD) {
            log.warn(
                    "Vehicle VIN: {} is not in ON_HOLD state, current status: {}",
                    vinNumber,
                    vehicle.getStatus());
            throw new IllegalStateException("Vehicle is not in ON_HOLD state");
        }

        redisService.delete(REQUEST_TIMEOUT_PREFIX + id);

        // publish to broker
        MoveVehicleDto moveVehicleDto =
                generateVehicleMovementDto(startLocation, tripId, vinNumber);
        eventPublisher.publishEvent(moveVehicleDto);
        log.debug("Published MoveVehicleDto event for trip id: {}", tripId);

        // Update statuses
        updateRequestStatus(id, RequestStatus.ACCEPTED);
        log.info("Request with id {} has changed to ACCEPTED.", id);

        tripService.updateTripStatus(tripId, TripStatus.VEHICLE_ON_WAY);
        log.info("Trip with id {} has changed to VEHICLE_ON_WAY", tripId);

        vehicleService.updateVehicleStatus(vinNumber, VehicleStatus.ON_WAY);
        log.info("Vehicle with vinNumber {} has been changed to ON_WAY", vinNumber);

        return request;
    }

    private MoveVehicleDto generateVehicleMovementDto(
            Point startLocation, Long tripId, String vinNumber) {
        log.debug("Generating MoveVehicleDto for trip id: {}, VIN: {}", tripId, vinNumber);
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

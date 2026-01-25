package com.yaquodorg.yaquod.service.request;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.context.ApplicationEventPublisher;
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
    public void declineRequestById(Long id, String token) {
        User user = userService.getUserByJwt(token);
        Request request = getRequest(id);
        if (!request.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized to decline this request");
        } else {
            updateRequestStatus(id, RequestStatus.DECLINED);
            log.info("Request with id {} has been declined.", id);

            Trip trip = request.getTrip();
            long tripId = trip.getId();
            tripService.updateTripStatus(tripId, TripStatus.CANCELLED_BY_PASSENGER);
            log.info("Declining associated trip with id {}.", tripId);

            Vehicle vehicle = trip.getVehicle();
            if (vehicle != null) {
                String vinNumber = vehicle.getVinNumber();
                vehicleService.updateVehicleStatus(vinNumber, VehicleStatus.IDLE);
                log.info("Returning vehicle status with vin: {} to IDLE", vinNumber);
            }
        }
    }

    @Override
    @Transactional
    public Request acceptRequestById(Long id, String token) {
        User user = userService.getUserByJwt(token);
        Request request = getRequest(id);
        if (!request.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized to accept this request");
        } else {
            // publish to broker
            MoveVehicleDto moveVehicleDto = generateVehicleMovementDto(id);
            eventPublisher.publishEvent(moveVehicleDto);

            updateRequestStatus(id, RequestStatus.ACCEPTED);
            log.info("Request with id {} has been accepted.", id);

            Trip trip = request.getTrip();
            long tripId = trip.getId();
            tripService.updateTripStatus(tripId, TripStatus.VEHICLE_ON_WAY);
            log.info("Accepting associated trip with id {}.", tripId);

            Vehicle vehicle = trip.getVehicle();
            if (vehicle != null) {
                String vinNumber = vehicle.getVinNumber();
                vehicleService.updateVehicleStatus(vinNumber, VehicleStatus.ON_WAY);
                log.info("Returning vehicle status with vin: {} to ON_WAY", vinNumber);
            }

            return request;
        }

    }

    @Override
    public MoveVehicleDto generateVehicleMovementDto(Long requestId) {
        Request request = getRequest(requestId);
        Trip trip = request.getTrip();
        Vehicle vehicle = trip.getVehicle();

        String vinNumber = vehicle.getVinNumber();
        long tripId = trip.getId();
        Point startLocation = request.getStartLocation();
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

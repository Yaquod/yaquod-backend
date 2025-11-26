package com.yaquodorg.yaquod.service.trip;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.yaquodorg.yaquod.dtos.InitTripDto;
import com.yaquodorg.yaquod.entity.Request;
import com.yaquodorg.yaquod.entity.RequestStatus;
import com.yaquodorg.yaquod.entity.Trip;
import com.yaquodorg.yaquod.entity.TripStatus;
import com.yaquodorg.yaquod.entity.User;
import com.yaquodorg.yaquod.entity.Vehicle;
import com.yaquodorg.yaquod.repository.RequestRepository;
import com.yaquodorg.yaquod.repository.TripRepository;
import com.yaquodorg.yaquod.service.user.UserService;
import com.yaquodorg.yaquod.service.vehicle.VehicleService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class TripServiceImpl implements TripService {

    private final VehicleService vehicleService;
    private final UserService userService;

    private final RequestRepository requestRepository;
    private final TripRepository tripRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public void createTrip(Long requestId, double startLong, double startLat, double endLong, double endLat) {

        // match vehicle
        List<Vehicle> vehicles = vehicleService.findKNearestVehicles(startLong, startLat, 1);
        Vehicle vehicle = vehicles.get(0);
        String vinNumber = vehicle.getVinNumber();

        User user = requestRepository.findById(requestId).orElseThrow().getUser();
        // build dto
        InitTripDto initTripDto = InitTripDto.builder()
                .vinNumber(vinNumber)
                .requestId(requestId)
                .startLong(startLong)
                .startLat(startLat)
                .endLong(endLong)
                .endLat(endLat)
                .build();

        tripRepository.save(Trip.builder()
                .request(requestRepository.findById(requestId).orElseThrow(() -> new RuntimeException("Request not " +
                        "found!")))
                .vehicle(vehicle)
                .user(user)
                .status(TripStatus.INITIATED)
                .startedAt(new Timestamp(new Date().getTime()))
                .build());

        // publish to broker
        eventPublisher.publishEvent(initTripDto);
    }

    @Override
    public Request getRequest(Long requestId) {
        return requestRepository.findById(requestId).orElseThrow(() -> new RuntimeException("Request not found!"));
    }

    @Override
    public Request createRequest(Long userId, double startLong, double startLat, double endLong, double endLat) {
        User user = userService.getUserById(userId);

        GeometryFactory geometryFactory = new GeometryFactory();
        Point startPoint = geometryFactory.createPoint(new Coordinate(startLong, startLat));
        Point endPoint = geometryFactory.createPoint(new Coordinate(endLong, endLat));

        Request request = Request.builder()
                .user(user)
                .startLocation(startPoint)
                .destinationLocation(endPoint)
                .status(RequestStatus.PENDING)
                .createdAt(new Timestamp(new Date().getTime()))
                .build();

        Request savedRequest = requestRepository.save(request);
        createTrip(savedRequest.getId(), startLong, startLat, endLong, endLat);

        return savedRequest;
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

}

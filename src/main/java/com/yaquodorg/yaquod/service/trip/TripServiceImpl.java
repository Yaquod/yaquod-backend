package com.yaquodorg.yaquod.service.trip;

import com.yaquodorg.yaquod.dtos.EtaStatusDto;
import com.yaquodorg.yaquod.dtos.InitTripDto;
import com.yaquodorg.yaquod.entity.*;
import com.yaquodorg.yaquod.repository.RequestRepository;
import com.yaquodorg.yaquod.repository.TripRepository;
import com.yaquodorg.yaquod.repository.UserRepository;
import com.yaquodorg.yaquod.service.mqtt.MqttService;
import com.yaquodorg.yaquod.service.vehicle.VehicleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TripServiceImpl implements TripService {

    private static final String TOPIC_INIT_TRIP = "topic/trip/init";
    private static final String TOPIC_ETA_TRIP = "topic/trip/eta";
    private final MqttService mqttService;
    private final VehicleService vehicleService;
    private final RequestRepository requestRepository;
    private final UserRepository userRepository;
    private final TripRepository tripRepository;

    @Override
    public void initTrip(Long requestId, double startLong, double startLat, double endLong, double endLat) {

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
        mqttService.publish(TOPIC_INIT_TRIP, initTripDto);
    }

    @Override
    public EtaStatusDto checkStatus(Long requestId) {
        // TODO: Complete implementation
        return null;
    }

    @Override
    public Request createRequest(Long userId, double startLong, double startLat, double endLong, double endLat) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found!"));

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
        initTrip(savedRequest.getId(), startLong, startLat, endLong, endLat);
        return savedRequest;
    }

    @Override
    public RequestStatus getRequestStatusByRequestId(Long requestId) {
        Request request = requestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Request not found!"));
        return request.getStatus();
    }


}

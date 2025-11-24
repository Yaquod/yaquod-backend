package com.yaquodorg.yaquod.service.trip;

import java.sql.Time;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

import com.yaquodorg.yaquod.entity.Request;
import com.yaquodorg.yaquod.entity.RequestStatus;
import com.yaquodorg.yaquod.entity.User;
import com.yaquodorg.yaquod.repository.RequestRepository;
import com.yaquodorg.yaquod.repository.UserRepository;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.springframework.stereotype.Service;

import com.yaquodorg.yaquod.dtos.EtaStatusDto;
import com.yaquodorg.yaquod.dtos.InitTripDto;
import com.yaquodorg.yaquod.entity.Vehicle;
import com.yaquodorg.yaquod.service.mqtt.MqttService;
import com.yaquodorg.yaquod.service.vehicle.VehicleService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class TripServiceImpl implements TripService {

    private final MqttService mqttService;
    private final VehicleService vehicleService;
    private  final RequestRepository requestRepository;
    private final UserRepository userRepository;

    private static final String TOPIC_INIT_TRIP = "topic/trip/init";
    private static final String TOPIC_ETA_TRIP = "topic/trip/eta";

    @Override
    public void initTrip(Long requestId, double startLong, double startLat, double endLong, double endLat) {

        // match vehicle
        List<Vehicle> vehicles = vehicleService.findKNearestVehicles(startLong, startLat, 1);
        Vehicle vehicle = vehicles.get(0);
        String vinNumber = vehicle.getVinNumber();

        // build dto
        InitTripDto initTripDto = InitTripDto.builder()
                .vinNumber(vinNumber)
                .requestId(requestId)
                .startLong(startLong)
                .startLat(startLat)
                .endLong(endLong)
                .endLat(endLat)
                .build();

        // publish to broker
        mqttService.publish(TOPIC_INIT_TRIP, initTripDto);
    }

    @Override
    public EtaStatusDto checkStatus(long requestId) {
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

        initTrip(request.getId(),  startLong,  startLat,  endLong,  endLat);
        return requestRepository.save(request);
    }

}

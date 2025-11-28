package com.yaquodorg.yaquod.service.trip;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import com.yaquodorg.yaquod.dtos.InitTripDto;
import com.yaquodorg.yaquod.entity.Request;
import com.yaquodorg.yaquod.entity.Trip;
import com.yaquodorg.yaquod.entity.TripStatus;
import com.yaquodorg.yaquod.entity.User;
import com.yaquodorg.yaquod.entity.Vehicle;
import com.yaquodorg.yaquod.repository.TripRepository;
import com.yaquodorg.yaquod.service.vehicle.VehicleService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class TripServiceImpl implements TripService {

    private final TripRepository tripRepository;

    private final VehicleService vehicleService;

    private final ApplicationEventPublisher eventPublisher;

    @Override
    public void createTrip(Request request, double startLong, double startLat, double endLong, double endLat) {
        // get request's user
        User user = request.getUser();

        // match vehicle
        List<Vehicle> vehicles = vehicleService.findKNearestVehicles(startLong, startLat, 1);
        Vehicle vehicle = vehicles.get(0);
        String vinNumber = vehicle.getVinNumber();

        // build dto
        InitTripDto initTripDto = InitTripDto.builder()
                .vinNumber(vinNumber)
                .requestId(request.getId())
                .startLong(startLong)
                .startLat(startLat)
                .endLong(endLong)
                .endLat(endLat)
                .build();

        // publish to broker
        eventPublisher.publishEvent(initTripDto);

        // finally save the trip to the database
        tripRepository.save(Trip.builder()
                .request(request)
                .vehicle(vehicle)
                .user(user)
                .status(TripStatus.INITIATED)
                .startedAt(new Timestamp(new Date().getTime()))
                .build());
    }
}

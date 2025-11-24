package com.yaquodorg.yaquod.service.trip;

import java.util.List;

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

    private static final String TOPIC_INIT_TRIP = "topic/trip/init";
    private static final String TOPIC_ETA_TRIP = "topic/trip/eta";

    @Override
    public void initTrip(long requestId, long startLong, long startLat, long endLong, long endLat) {

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

}

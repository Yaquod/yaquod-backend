package com.yaquodorg.yaquod.service.mqtt;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yaquodorg.yaquod.dtos.*;
import com.yaquodorg.yaquod.entity.*;
import com.yaquodorg.yaquod.service.messaging.FirebaseMessagingService;
import com.yaquodorg.yaquod.service.request.RequestService;
import com.yaquodorg.yaquod.service.trip.TripService;
import com.yaquodorg.yaquod.service.vehicle.VehicleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Point;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.mqtt.support.MqttHeaders;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Service
@RequiredArgsConstructor
public class MqttService {

    private static final String TOPIC_UPDATE_LOCATION = "topic/update_location";
    private static final String TOPIC_UPDATE_STATUS = "topic/update_status";
    private static final String TOPIC_INIT_TRIP = "topic/trip/init";
    private static final String TOPIC_ETA_TRIP = "topic/trip/eta";
    private static final String TOPIC_TRIP_MOVE = "topic/trip/move";
    private static final String TOPIC_TRIP_ARRIVE = "topic/trip/arrive";
    private static final String TOPIC_TRIP_STATUS = "topic/trip/status";
    private static final String TOPIC_ORDER_UPDATE_LOCATION = "topic/update_location/order";
    private static final String TOPIC_ORDER_UPDATE_STATUS = "topic/update_status/order";

    private final MqttGateway mqttGateway;
    private final ObjectMapper objectMapper;

    private final VehicleService vehicleService;
    private final RequestService requestService;
    private final TripService tripService;
    private final FirebaseMessagingService firebaseMessagingService;

    @ServiceActivator(inputChannel = "mqttInputChannel")
    public void handleIncomingMessage(Message<?> message) {
        String topic = (String) message.getHeaders().get(MqttHeaders.RECEIVED_TOPIC);
        String payload = message.getPayload().toString();

        if (TOPIC_UPDATE_LOCATION.equals(topic)) {
            handleVehicleUpdateLocation(payload);
        } else if (TOPIC_UPDATE_STATUS.equals(topic)) {
            handleVehicleUpdateStatus(payload);
        } else if (TOPIC_ETA_TRIP.equals(topic)) {
            handleVehicleUpdateEta(payload);
        } else if (TOPIC_TRIP_ARRIVE.equals(topic)) {
            handleVehicleArrival(payload);
        } else if (TOPIC_TRIP_STATUS.equals(topic)) {
            handleVehicleUpdateTripStatus(payload);
        } else {
            log.warn("Unhandled topic: {}", topic);
        }

        log.info("Received message from topic '{}': {}", topic, payload);
    }

    private void handleVehicleUpdateLocation(String payload) {
        try {
            UpdateVehicleLocationDto dto = objectMapper.readValue(payload, UpdateVehicleLocationDto.class);
            log.info("Vehicle with VIN: {}, updated their long to: {}, and lat to: {}", dto.getVinNumber(),
                    dto.getLongitude(), dto.getLatitude());
            vehicleService.updateVehicleLocation(dto.getVinNumber(), dto.getLongitude(), dto.getLatitude());
        } catch (JsonProcessingException e) {
            log.error("Failed to parse vehicle location update payload: {}", payload, e);
        }
    }

    private void handleVehicleUpdateStatus(String payload) {
        try {
            UpdateVehicleStatusDto dto = objectMapper.readValue(payload, UpdateVehicleStatusDto.class);
            log.info("Vehicle with VIN: {}, updated their status to: {}", dto.getVinNumber(),
                    dto.getStatus());
            vehicleService.updateVehicleStatus(dto.getVinNumber(), dto.getStatus());
        } catch (JsonProcessingException e) {
            log.error("Failed to parse vehicle status update payload: {}", payload, e);
        }
    }

    private void handleVehicleUpdateEta(String payload) {
        try {
            EtaStatusDto dto = objectMapper.readValue(payload, EtaStatusDto.class);
            log.info("Request with ID: {}, status updated to {}", dto.getRequestId(),
                    dto.getStatus());
            requestService.updateRequest(dto.getRequestId(), dto.getStatus(), dto.getEstimatedTime(),
                    dto.getEstimatedFare());
            vehicleService.updateVehicleStatus(dto.getVinNumber(), VehicleStatus.ON_HOLD);
        } catch (JsonProcessingException e) {
            log.error("Failed to parse request status update payload: {}", payload, e);
        }
    }

    private void handleVehicleArrival(String payload) {
        try {
            VehicleArrivalDto dto = objectMapper.readValue(payload, VehicleArrivalDto.class);
            log.info("Vehicle with vin: {} arrived at long, lat: {}, {} for trip with id: {}",
                    dto.getVinNumber(),
                    dto.getLongitude(),
                    dto.getLatitude(),
                    dto.getTripId());

            Trip trip = tripService.getTripById(dto.getTripId());
            Vehicle vehicle = trip.getVehicle();
            Request request = trip.getRequest();
            User user = trip.getUser();

            String carInfo = String.format("%s %s (%s) - Plate: %s",
                    vehicle.getCarCompany(),
                    vehicle.getModel(),
                    vehicle.getColor(),
                    vehicle.getPlateNo());

            Point startLocation = request.getStartLocation();
            Point destinationLocation = request.getDestinationLocation();

            double startLat = startLocation.getY();
            double startLong = startLocation.getX();
            double destinationLat = destinationLocation.getY();
            double destinationLong = destinationLocation.getX();

            String message;
            if (isNearLocation(dto.getLatitude(), dto.getLongitude(), startLat, startLong)) {
                message = carInfo + " has arrived at your pickup location.";
                tripService.updateTripStatus(dto.getTripId(), TripStatus.ARRIVED_AT_DESTINATION);
                vehicleService.updateVehicleStatus(dto.getVinNumber(), VehicleStatus.WAITING_PASSENGER);
            } else if (isNearLocation(dto.getLatitude(), dto.getLongitude(), destinationLat, destinationLong)) {
                message = carInfo + " has arrived at your destination.";
                tripService.updateTripStatus(dto.getTripId(), TripStatus.ARRIVED_AT_PICKUP);
            } else {
                message = String.format("%s is at location: %.6f, %.6f",
                        carInfo, dto.getLatitude(), dto.getLongitude());
                tripService.updateTripStatus(dto.getTripId(), TripStatus.ARRIVED_AT_DESTINATION);
                vehicleService.updateVehicleStatus(dto.getVinNumber(), VehicleStatus.WAITING_PASSENGER);
            }

            log.info("Sending notification to user {}: {}", user.getId(), message);
            firebaseMessagingService.sendTextNotificationByToken(user.getFirebaseToken(), "Vehicle Arrived!", message);
        } catch (Exception e) {
            log.error("Failed to parse vehicle arrival payload: {}", payload, e);
        }
    }

    private void handleVehicleUpdateTripStatus(String payload) {
        try {
            UpdateTripStatusDto dto = objectMapper.readValue(payload, UpdateTripStatusDto.class);

            try {
                TripStatus status = TripStatus.valueOf(dto.getTripStatus());
                tripService.updateTripStatus(dto.getTripId(), status);
                log.info("Trip status with id: {} updated to {}", dto.getTripId(), status);
            } catch (IllegalArgumentException e) {
                log.error("Trip status with id: {} updated to invalid status: {}",
                        dto.getTripId(), dto.getTripStatus());
            }

        } catch (Exception e) {
            log.error("Failed to parse vehicle update status payload: {}", payload, e);
        }

    }

    private boolean isNearLocation(double lat1, double lon1, double lat2, double lon2) {
        double LOCATION_THRESHOLD = 0.0001;
        return Math.abs(lat1 - lat2) < LOCATION_THRESHOLD &&
                Math.abs(lon1 - lon2) < LOCATION_THRESHOLD;
    }

    public void publish(String topic, Object data) {
        try {
            String payload = objectMapper.writeValueAsString(data);
            mqttGateway.sendToMqtt(payload, topic);
            log.info("Published message to topic {}", topic);
        } catch (JsonProcessingException e) {
            log.error("Error publishing to topic {}", topic, e);
            throw new RuntimeException("Failed to publish to topic: " + topic, e);
        }
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleTripInitiated(InitTripDto event) {
        publish(TOPIC_INIT_TRIP, event);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleMoveVehicleOrder(MoveVehicleDto event) {
        publish(TOPIC_TRIP_MOVE, event);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleOrderVehicleUpdateOrder(VehicleDto event) {
        publish(TOPIC_ORDER_UPDATE_LOCATION, event);
        publish(TOPIC_ORDER_UPDATE_STATUS, event);
    }

}

package com.yaquodorg.yaquod.service.mqtt;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yaquodorg.yaquod.dtos.EtaStatusDto;
import com.yaquodorg.yaquod.dtos.InitTripDto;
import com.yaquodorg.yaquod.dtos.UpdateVehicleLocationDto;
import com.yaquodorg.yaquod.dtos.UpdateVehicleStatusDto;
import com.yaquodorg.yaquod.entity.VehicleStatus;
import com.yaquodorg.yaquod.service.request.RequestService;
import com.yaquodorg.yaquod.service.vehicle.VehicleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.mqtt.support.MqttHeaders;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class MqttService {

    private static final String TOPIC_UPDATE_LOCATION = "topic/update_location";
    private static final String TOPIC_UPDATE_STATUS = "topic/update_status";
    private static final String TOPIC_INIT_TRIP = "topic/trip/init";
    private static final String TOPIC_ETA_TRIP = "topic/trip/eta";


    private final MqttGateway mqttGateway;
    private final ObjectMapper objectMapper;

    private final VehicleService vehicleService;
    private final RequestService requestService;

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


    @EventListener
    public void handleTripInitiated(InitTripDto event) {
        publish(TOPIC_INIT_TRIP, event);
    }
}

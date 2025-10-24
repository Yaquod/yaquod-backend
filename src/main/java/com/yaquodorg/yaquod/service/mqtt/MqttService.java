package com.yaquodorg.yaquod.service.mqtt;

import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.mqtt.support.MqttHeaders;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yaquodorg.yaquod.dtos.UpdateVehicleLocationDto;
import com.yaquodorg.yaquod.service.vehicle.VehicleService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class MqttService {

    private final MqttGateway mqttGateway;
    private final ObjectMapper objectMapper;
    private final VehicleService vehicleService;
    private static final String TOPIC_UPDATE_LOCATION = "topic/update_location";

    @ServiceActivator(inputChannel = "mqttInputChannel")
    public void handleIncomingMessage(Message<?> message) {
        String topic = (String) message.getHeaders().get(MqttHeaders.RECEIVED_TOPIC);
        String payload = message.getPayload().toString();

        if (TOPIC_UPDATE_LOCATION.equals(topic)) {
            handleVehicleUpdateLocation(payload);
        }

        log.info("Received message from topic '{}': {}", topic, payload);
    }

    private void handleVehicleUpdateLocation(String payload) {
        try {
            UpdateVehicleLocationDto dto = objectMapper.readValue(payload, UpdateVehicleLocationDto.class);
            log.info("Vehicle with UUID: {}, updated their long to: {}, and lat to: {}", dto.getVehicleUUID(),
                    dto.getLongitude(), dto.getLatitude());
            vehicleService.updateVehicleLocation(dto.getVehicleUUID(), dto.getLongitude(), dto.getLatitude());
        } catch (JsonProcessingException e) {
            log.error("Failed to parse vehicle location update payload: {}", payload, e);
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
}

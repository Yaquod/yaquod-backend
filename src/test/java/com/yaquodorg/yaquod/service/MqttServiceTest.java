package com.yaquodorg.yaquod.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yaquodorg.yaquod.dtos.UpdateVehicleLocationDto;
import com.yaquodorg.yaquod.dtos.UpdateVehicleStatusDto;
import com.yaquodorg.yaquod.dtos.VehicleDto;
import com.yaquodorg.yaquod.entity.VehicleStatus;
import com.yaquodorg.yaquod.service.mqtt.MqttGateway;
import com.yaquodorg.yaquod.service.mqtt.MqttService;
import com.yaquodorg.yaquod.service.vehicle.VehicleService;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.integration.mqtt.support.MqttHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;

/**
 * NOTE: ALL THOSE TESTS ARE AI-GENERATED AND REVIEWED MANUALLY
 *
 * <p>Unit tests for MqttService
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("MqttService Unit Tests")
class MqttServiceTest {

  private final String VinNumber1 = "1HGCM82633A004352";
  @Mock private MqttGateway mqttGateway;
  @Mock private ObjectMapper objectMapper;
  @Mock private VehicleService vehicleService;
  @Mock private Message<String> message;
  @InjectMocks private MqttService mqttService;
  private Map<String, Object> headers;

  @BeforeEach
  void setUp() {
    headers = new HashMap<>();
  }

  /** HANDLE INCOMING MESSAGE TESTS */
  @Test
  @DisplayName("Should handle location update message successfully")
  void shouldHandleLocationUpdateMessage() throws Exception {
    // Arrange
    String topic = "topic/update_location";
    String payload =
        """
                {
                    "vinNumber": "1HGCM82633A004352",
                    "longitude": 40.7128,
                    "latitude": -74.0060
                }
                """;

    UpdateVehicleLocationDto dto = new UpdateVehicleLocationDto();
    dto.setVinNumber(VinNumber1);
    dto.setLongitude(40.7128);
    dto.setLatitude(-74.0060);

    headers.put(MqttHeaders.RECEIVED_TOPIC, topic);
    when(message.getHeaders()).thenReturn(new MessageHeaders(headers));
    when(message.getPayload()).thenReturn(payload);
    when(objectMapper.readValue(payload, UpdateVehicleLocationDto.class)).thenReturn(dto);
    doNothing().when(vehicleService).updateVehicleLocation(anyString(), anyDouble(), anyDouble());

    // Act
    mqttService.handleIncomingMessage(message);

    // Assert
    verify(objectMapper).readValue(payload, UpdateVehicleLocationDto.class);
    verify(vehicleService).updateVehicleLocation(VinNumber1, 40.7128, -74.0060);
  }

  @Test
  @DisplayName("Should handle status update message successfully")
  void shouldHandleStatusUpdateMessage() throws Exception {
    // Arrange
    String topic = "topic/update_status";
    String payload =
        """
                {
                    "vinNumber": "1M8GDM9AXKP042788",
                    "status": "IN_USE"
                }
                """;

    UpdateVehicleStatusDto dto = new UpdateVehicleStatusDto();
    String vinNumber2 = "1M8GDM9AXKP042788";
    dto.setVinNumber(vinNumber2);
    dto.setStatus(VehicleStatus.IN_USE);

    headers.put(MqttHeaders.RECEIVED_TOPIC, topic);
    when(message.getHeaders()).thenReturn(new MessageHeaders(headers));
    when(message.getPayload()).thenReturn(payload);
    when(objectMapper.readValue(payload, UpdateVehicleStatusDto.class)).thenReturn(dto);
    doNothing().when(vehicleService).updateVehicleStatus(anyString(), any(VehicleStatus.class));

    // Act
    mqttService.handleIncomingMessage(message);

    // Assert
    verify(objectMapper).readValue(payload, UpdateVehicleStatusDto.class);
    verify(vehicleService).updateVehicleStatus(vinNumber2, VehicleStatus.IN_USE);
  }

  @Test
  @DisplayName("Should log warning for unhandled topic")
  void shouldLogWarningForUnhandledTopic() {
    // Arrange
    String topic = "unknown/topic";
    String payload = "some data";

    headers.put(MqttHeaders.RECEIVED_TOPIC, topic);
    when(message.getHeaders()).thenReturn(new MessageHeaders(headers));
    when(message.getPayload()).thenReturn(payload);

    // Act
    mqttService.handleIncomingMessage(message);

    // Assert
    try {
      verify(objectMapper, never()).readValue(anyString(), any(Class.class));
    } catch (JsonMappingException e) {
      e.printStackTrace();
    } catch (JsonProcessingException e) {
      e.printStackTrace();
    }
    verify(vehicleService, never()).updateVehicleLocation(anyString(), anyDouble(), anyDouble());
    verify(vehicleService, never()).updateVehicleStatus(anyString(), any(VehicleStatus.class));
  }

  @Test
  @DisplayName("Should handle JSON parsing error for location update")
  void shouldHandleJsonParsingErrorForLocationUpdate() throws Exception {
    // Arrange
    String topic = "topic/update_location";
    String invalidPayload = "invalid json";

    headers.put(MqttHeaders.RECEIVED_TOPIC, topic);
    when(message.getHeaders()).thenReturn(new MessageHeaders(headers));
    when(message.getPayload()).thenReturn(invalidPayload);
    when(objectMapper.readValue(invalidPayload, UpdateVehicleLocationDto.class))
        .thenThrow(new JsonProcessingException("Invalid JSON") {});

    // Act
    mqttService.handleIncomingMessage(message);

    // Assert
    verify(objectMapper).readValue(invalidPayload, UpdateVehicleLocationDto.class);
    verify(vehicleService, never()).updateVehicleLocation(anyString(), anyDouble(), anyDouble());
  }

  @Test
  @DisplayName("Should handle JSON parsing error for status update")
  void shouldHandleJsonParsingErrorForStatusUpdate() throws Exception {
    // Arrange
    String topic = "topic/update_status";
    String invalidPayload = "invalid json";

    headers.put(MqttHeaders.RECEIVED_TOPIC, topic);
    when(message.getHeaders()).thenReturn(new MessageHeaders(headers));
    when(message.getPayload()).thenReturn(invalidPayload);
    when(objectMapper.readValue(invalidPayload, UpdateVehicleStatusDto.class))
        .thenThrow(new JsonProcessingException("Invalid JSON") {});

    // Act
    mqttService.handleIncomingMessage(message);

    // Assert
    verify(objectMapper).readValue(invalidPayload, UpdateVehicleStatusDto.class);
    verify(vehicleService, never()).updateVehicleStatus(anyString(), any(VehicleStatus.class));
  }

  /** PUBLISH TESTS */
  @Test
  @DisplayName("Should publish message successfully")
  void shouldPublishMessageSuccessfully() throws Exception {
    // Arrange
    String topic = "test/topic";
    VehicleDto data = new VehicleDto("5GZCZ43D13S812715");
    String jsonPayload = "{\"vinNumber\":\"5GZCZ43D13S812715\"}";

    when(objectMapper.writeValueAsString(data)).thenReturn(jsonPayload);
    doNothing().when(mqttGateway).sendToMqtt(anyString(), anyString());

    // Act
    mqttService.publish(topic, data);

    // Assert
    verify(objectMapper).writeValueAsString(data);
    verify(mqttGateway).sendToMqtt(jsonPayload, topic);
  }

  @Test
  @DisplayName("Should throw exception when publish fails due to JSON error")
  void shouldThrowExceptionWhenPublishFailsDueToJsonError() throws Exception {
    // Arrange
    String topic = "test/topic";
    Object data = new Object();

    when(objectMapper.writeValueAsString(data))
        .thenThrow(new JsonProcessingException("Serialization error") {});

    // Act & Assert
    assertThatThrownBy(() -> mqttService.publish(topic, data))
        .isInstanceOf(RuntimeException.class)
        .hasMessageContaining("Failed to publish to topic: test/topic")
        .hasCauseInstanceOf(JsonProcessingException.class);

    verify(objectMapper).writeValueAsString(data);
    verify(mqttGateway, never()).sendToMqtt(anyString(), anyString());
  }

  @Test
  @DisplayName("Should publish with correct topic and payload")
  void shouldPublishWithCorrectTopicAndPayload() throws Exception {
    // Arrange
    String topic = "custom/topic";
    VehicleDto data = new VehicleDto("3VWFE21C04M000001");
    String expectedJson = "{\"vinNumber\":\"3VWFE21C04M000001\"}";

    when(objectMapper.writeValueAsString(data)).thenReturn(expectedJson);

    // Act
    mqttService.publish(topic, data);

    // Assert
    ArgumentCaptor<String> payloadCaptor = ArgumentCaptor.forClass(String.class);
    ArgumentCaptor<String> topicCaptor = ArgumentCaptor.forClass(String.class);

    verify(mqttGateway).sendToMqtt(payloadCaptor.capture(), topicCaptor.capture());

    assertThat(payloadCaptor.getValue()).isEqualTo(expectedJson);
    assertThat(topicCaptor.getValue()).isEqualTo(topic);
  }

  /** EDGE CASES */
  @Test
  @DisplayName("Should handle null topic in message")
  void shouldHandleNullTopicInMessage() {
    // Arrange
    headers.put(MqttHeaders.RECEIVED_TOPIC, null);
    when(message.getHeaders()).thenReturn(new MessageHeaders(headers));
    when(message.getPayload()).thenReturn("payload");

    // Act
    mqttService.handleIncomingMessage(message);

    // Assert
    verify(vehicleService, never()).updateVehicleLocation(anyString(), anyDouble(), anyDouble());
    verify(vehicleService, never()).updateVehicleStatus(anyString(), any(VehicleStatus.class));
  }

  @Test
  @DisplayName("Should handle empty payload")
  void shouldHandleEmptyPayload() throws Exception {
    // Arrange
    String topic = "topic/update_location";
    String emptyPayload = "";

    headers.put(MqttHeaders.RECEIVED_TOPIC, topic);
    when(message.getHeaders()).thenReturn(new MessageHeaders(headers));
    when(message.getPayload()).thenReturn(emptyPayload);
    when(objectMapper.readValue(emptyPayload, UpdateVehicleLocationDto.class))
        .thenThrow(new JsonProcessingException("Empty payload") {});

    // Act
    mqttService.handleIncomingMessage(message);

    // Assert
    verify(vehicleService, never()).updateVehicleLocation(anyString(), anyDouble(), anyDouble());
  }

  @Test
  @DisplayName("Should handle multiple location updates in sequence")
  void shouldHandleMultipleLocationUpdatesInSequence() throws Exception {
    // Arrange
    String topic = "topic/update_location";
    UpdateVehicleLocationDto dto1 = new UpdateVehicleLocationDto();
    dto1.setVinNumber("WAUZZZ8K9DA123456"); // VIN001 =WAUZZZ8K9DA123456
    dto1.setLongitude(10.0);
    dto1.setLatitude(20.0);

    UpdateVehicleLocationDto dto2 = new UpdateVehicleLocationDto();
    dto2.setVinNumber("WDBRF40JX3F376482"); // VIN002 = WDBRF40JX3F376482
    dto2.setLongitude(30.0);
    dto2.setLatitude(40.0);

    headers.put(MqttHeaders.RECEIVED_TOPIC, topic);
    when(message.getHeaders()).thenReturn(new MessageHeaders(headers));
    when(message.getPayload()).thenReturn("payload1", "payload2");
    when(objectMapper.readValue("payload1", UpdateVehicleLocationDto.class)).thenReturn(dto1);
    when(objectMapper.readValue("payload2", UpdateVehicleLocationDto.class)).thenReturn(dto2);

    // Act
    mqttService.handleIncomingMessage(message);
    mqttService.handleIncomingMessage(message);

    // Assert
    verify(vehicleService).updateVehicleLocation("WAUZZZ8K9DA123456", 10.0, 20.0);
    verify(vehicleService).updateVehicleLocation("WDBRF40JX3F376482", 30.0, 40.0);
  }

  @Test
  @DisplayName("Should handle negative coordinates")
  void shouldHandleNegativeCoordinates() throws Exception {
    // Arrange
    String topic = "topic/update_location";
    String payload =
        """
                {
                    "vinNumber": "1HGCM82633A004352",
                    "longitude": -118.2437,
                    "latitude": -34.0522
                }
                """;

    UpdateVehicleLocationDto dto = new UpdateVehicleLocationDto();
    dto.setVinNumber("1HGCM82633A004352");
    dto.setLongitude(-118.2437);
    dto.setLatitude(-34.0522);

    headers.put(MqttHeaders.RECEIVED_TOPIC, topic);
    when(message.getHeaders()).thenReturn(new MessageHeaders(headers));
    when(message.getPayload()).thenReturn(payload);
    when(objectMapper.readValue(payload, UpdateVehicleLocationDto.class)).thenReturn(dto);

    // Act
    mqttService.handleIncomingMessage(message);

    // Assert
    verify(vehicleService).updateVehicleLocation("1HGCM82633A004352", -118.2437, -34.0522);
  }

  @Test
  @DisplayName("Should handle all vehicle statuses")
  void shouldHandleAllVehicleStatuses() throws Exception {
    // Test each status
    VehicleStatus[] statuses = {
      VehicleStatus.IDLE, VehicleStatus.IN_USE, VehicleStatus.ON_WAY, VehicleStatus.OUT_OF_SERVICE
    };

    for (VehicleStatus status : statuses) {
      // Arrange
      String topic = "topic/update_status";
      UpdateVehicleStatusDto dto = new UpdateVehicleStatusDto();
      dto.setVinNumber("1HGCM82633A004352"); // Using same VIN for simplicity
      dto.setStatus(status);

      headers.put(MqttHeaders.RECEIVED_TOPIC, topic);
      when(message.getHeaders()).thenReturn(new MessageHeaders(headers));
      when(message.getPayload()).thenReturn("payload");
      when(objectMapper.readValue("payload", UpdateVehicleStatusDto.class)).thenReturn(dto);

      // Act
      mqttService.handleIncomingMessage(message);

      // Assert
      verify(vehicleService).updateVehicleStatus("1HGCM82633A004352", status);
    }
  }

  @Test
  @DisplayName("Should not call vehicle service when JSON parsing fails")
  void shouldNotCallVehicleServiceWhenJsonParsingFails() throws Exception {
    // Arrange
    String topic = "topic/update_location";
    headers.put(MqttHeaders.RECEIVED_TOPIC, topic);
    when(message.getHeaders()).thenReturn(new MessageHeaders(headers));
    when(message.getPayload()).thenReturn("bad json");
    when(objectMapper.readValue(anyString(), eq(UpdateVehicleLocationDto.class)))
        .thenThrow(new JsonProcessingException("Parse error") {});

    // Act
    mqttService.handleIncomingMessage(message);

    // Assert
    verify(vehicleService, never()).updateVehicleLocation(anyString(), anyDouble(), anyDouble());
    verify(vehicleService, never()).updateVehicleStatus(anyString(), any());
  }
}

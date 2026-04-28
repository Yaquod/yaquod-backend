package com.yaquodorg.yaquod.config;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.core.MessageProducer;
import org.springframework.integration.mqtt.core.DefaultMqttPahoClientFactory;
import org.springframework.integration.mqtt.core.MqttPahoClientFactory;
import org.springframework.integration.mqtt.inbound.MqttPahoMessageDrivenChannelAdapter;
import org.springframework.integration.mqtt.outbound.MqttPahoMessageHandler;
import org.springframework.integration.mqtt.support.DefaultPahoMessageConverter;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;

@Slf4j
@Configuration
@ConfigurationProperties(prefix = "mqtt")
@Getter
@Setter
public class MqttConfig {

    private String brokerUrl;
    private String clientId;
    private String[] topics;
    /*
    Use QoS 1 when:
    • You need to get every message and your use case can
    handle duplicates. QoS level 1 is the most frequently used
    service level because it guarantees the message arrives
    at least once but allows for multiple deliveries. Of course,
    your application must tolerate duplicates and be able to
    process them accordingly.
    • You can’t bear the overhead of QoS 2. QoS 1 delivers
    messages much faster than QoS 2.
    Use QoS 2 when:
    • It is critical to your application to receive all messages
    exactly once. This is often the case if a duplicate delivery
    can harm application users or subscribing clients. Be
    aware of the overhead and that the QoS 2 interaction
    takes more time to complete.

    source : hivemq-ebook-mqtt-essentials.pdf
     */
    private int qos = 2;
    private int connectionTimeout = 60;
    private int keepAliveInterval = 60;

    @Bean
    public MqttPahoClientFactory mqttClientFactory() {
        DefaultMqttPahoClientFactory factory = new DefaultMqttPahoClientFactory();
        MqttConnectOptions options = new MqttConnectOptions();

        String envBroker = System.getenv("MQTT_BROKER_URL");
        String resolvedUrl = (envBroker != null && !envBroker.isBlank()) ? envBroker : brokerUrl;

        log.info("Connecting to MQTT Broker at: {}", resolvedUrl);

        options.setServerURIs(new String[] {resolvedUrl});
        options.setConnectionTimeout(connectionTimeout);
        options.setKeepAliveInterval(keepAliveInterval);
        options.setCleanSession(true);
        options.setAutomaticReconnect(true);

        factory.setConnectionOptions(options);
        return factory;
    }

    @Bean
    public MessageChannel mqttInputChannel() {
        return new DirectChannel();
    }

    @Bean
    public MessageChannel mqttOutboundChannel() {
        return new DirectChannel();
    }

    @Bean
    public MessageProducer inbound() {
        MqttPahoMessageDrivenChannelAdapter adapter =
                new MqttPahoMessageDrivenChannelAdapter(
                        clientId + "-inbound", mqttClientFactory(), topics);

        adapter.setCompletionTimeout(5000);
        adapter.setConverter(new DefaultPahoMessageConverter());
        adapter.setQos(qos);
        adapter.setOutputChannel(mqttInputChannel());
        return adapter;
    }

    @Bean
    @ServiceActivator(inputChannel = "mqttOutboundChannel")
    public MessageHandler mqttOutbound() {
        MqttPahoMessageHandler messageHandler =
                new MqttPahoMessageHandler(clientId + "-outbound", mqttClientFactory());
        messageHandler.setAsync(true);
        messageHandler.setDefaultQos(qos);
        messageHandler.setDefaultTopic("testTopic");
        return messageHandler;
    }
}

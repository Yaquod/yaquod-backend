package com.yaquodorg.yaquod.config;

import com.yaquodorg.yaquod.utils.RedisExpiryListener;
import jakarta.annotation.PostConstruct;
import java.time.Duration;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
@Slf4j
public class RedisConfig {

    @Autowired private RedisConnectionFactory connectionFactory;

    @Bean
    public RedisTemplate<String, String> redisTemplate(
            RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, String> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new StringRedisSerializer());
        redisTemplate.afterPropertiesSet();
        return redisTemplate;
    }

    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        RedisCacheConfiguration defaultConfig =
                RedisCacheConfiguration.defaultCacheConfig()
                        .disableCachingNullValues()
                        .serializeValuesWith(
                                RedisSerializationContext.SerializationPair.fromSerializer(
                                        new GenericJackson2JsonRedisSerializer()));

        // TODO: Add specific cache configurations here if needed, e.g.:
        // "userCache", defaultConfig.entryTtl(Duration.ofMinutes(30)),
        Map<String, RedisCacheConfiguration> cacheConfigs = Map.of();

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig.entryTtl(Duration.ofMinutes(10))) // fallback TTL
                .withInitialCacheConfigurations(cacheConfigs)
                .build();
    }

    @ConditionalOnProperty(value = "app.redis.expiry-listener.enabled", havingValue = "true")
    @PostConstruct
    public void enableKeyspaceNotifications() {
        try {
            connectionFactory
                    .getConnection()
                    .serverCommands()
                    .setConfig("notify-keyspace-events", "KEx");
        } catch (Exception ex) {
            log.warn("Could not enable Redis keyspace notifications: {}", ex.getMessage());
        }
    }

    @Bean
    @ConditionalOnProperty(value = "app.redis.expiry-listener.enabled", havingValue = "true")
    public RedisMessageListenerContainer listenerContainer(
            RedisConnectionFactory factory, MessageListenerAdapter listenerAdapter) {

        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(factory);
        // Listen to all expired key events in Redis DB 0
        container.addMessageListener(listenerAdapter, new PatternTopic("__keyevent@0__:expired"));
        return container;
    }

    @Bean
    @ConditionalOnProperty(value = "app.redis.expiry-listener.enabled", havingValue = "true")
    public MessageListenerAdapter listenerAdapter(RedisExpiryListener listener) {
        return new MessageListenerAdapter(listener, "onMessage");
    }
}

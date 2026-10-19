package com.moviematch.backend.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.moviematch.backend.dto.PlaybackStateResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class PlaybackCacheService {

    private static final Logger logger =
            LoggerFactory.getLogger(PlaybackCacheService.class);

    private static final String KEY_PREFIX = "watch-room:playback:";

    private static final Duration CACHE_TTL =
            Duration.ofHours(2);

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    public PlaybackCacheService(
            StringRedisTemplate redisTemplate,
            ObjectMapper objectMapper) {

        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    public PlaybackStateResponse get(String roomId) {
        try {
            String key = KEY_PREFIX + roomId;

            String json = redisTemplate.opsForValue().get(key);

            if (json == null) {
                return null;
            }

            return objectMapper.readValue(
                    json,
                    PlaybackStateResponse.class
            );

        } catch (Exception exception) {
            logger.warn(
                    "Unable to read playback state from Redis",
                    exception
            );

            return null;
        }
    }

    public void put(PlaybackStateResponse state) {
        try {
            String key = KEY_PREFIX + state.getRoomId();

            String json = objectMapper.writeValueAsString(state);

            redisTemplate.opsForValue().set(
                    key,
                    json,
                    CACHE_TTL
            );

        } catch (JsonProcessingException exception) {
            logger.error(
                    "Unable to serialize playback state",
                    exception
            );

        } catch (Exception exception) {
            logger.warn(
                    "Unable to cache playback state in Redis",
                    exception
            );
        }
    }

    public void evict(String roomId) {
        try {
            redisTemplate.delete(KEY_PREFIX + roomId);

        } catch (Exception exception) {
            logger.warn(
                    "Unable to remove playback state from Redis",
                    exception
            );
        }
    }
}
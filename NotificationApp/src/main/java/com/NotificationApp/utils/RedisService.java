package com.NotificationApp.utils;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Service
public class RedisService {
    private final RedisTemplate<String,Object> redis;

    public RedisService(RedisTemplate<String,Object> redis) {
        this.redis = redis;
    }

    public boolean setIfAbsent(String key, String value, long ttlSeconds) {
        Boolean ok = redis.opsForValue().setIfAbsent(key, value, Duration.ofSeconds(ttlSeconds));
        return Boolean.TRUE.equals(ok);
    }

    public Object get(String key) {
        return redis.opsForValue().get(key);
    }

    public void incrementWithTtl(String key, long ttlSeconds) {
        Long v = redis.opsForValue().increment(key);
        if (v != null && v == 1) {
            redis.expire(key, ttlSeconds, TimeUnit.SECONDS);
        }
    }
}


package io.goorm.route33.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class RedisTtlService {

    private final StringRedisTemplate redisTemplate;

    public boolean isExecutable(Long userId, String action) {
        String key = "req:block:" + action + ":" + userId;

        return redisTemplate.opsForValue()
                .setIfAbsent(key, "1", Duration.ofSeconds(3));
    }
}

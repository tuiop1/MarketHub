package com.tuiop.markethub.ratelimiter;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class FixedWindowRateLimiter {

    private final StringRedisTemplate stringRedisTemplate;

    public boolean allowRequest(
            String userId,
            int limit,
            Duration windowSize
    ) {
            long windowIndex = System.currentTimeMillis() / windowSize.toMillis();

        String key = String.format("rate:%s:%s", userId, windowIndex);

        Long countHits = stringRedisTemplate.opsForValue().increment(key);

        if (countHits != null && countHits == 1L) {
            stringRedisTemplate.expire(key, windowSize);
        }



        return countHits != null && countHits <= limit;


    }
}

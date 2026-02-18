package org.middleware.pnr.aggregator.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.middleware.pnr.aggregator.config.RateLimiterProperties;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Service
@RequiredArgsConstructor
public class RateLimiterService {

    private final RateLimiterProperties rateLimiterProperties;
    
    private final Map<String, AtomicInteger> requestCounts = new ConcurrentHashMap<>();
    private final Map<String, Long> lastResetTime = new ConcurrentHashMap<>();


    public boolean isAllowed(String clientIp) {
        long currentTime = System.currentTimeMillis();
        long windowMs = rateLimiterProperties.getTimeWindowSeconds() * 1000L;
        
        Long lastReset = lastResetTime.get(clientIp);
        log.info("RateLimiter: resetting window for this client {}", clientIp);
        if (lastReset == null || (currentTime - lastReset) > windowMs) {
            requestCounts.put(clientIp, new AtomicInteger(0));
            lastResetTime.put(clientIp, currentTime);
        }
        
        AtomicInteger count = requestCounts.get(clientIp);
        if (count.get() >= rateLimiterProperties.getMaxRequests()) {
            log.error("Rate limit exceeded for this client {}, allowed {} and current is {}",
                    clientIp, rateLimiterProperties.getMaxRequests(), count);
            return false;
        }

        log.info("RateLimiter: allowing this request.");
        count.incrementAndGet();
        return true;
    }
}

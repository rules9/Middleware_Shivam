package org.middleware.pnr.aggregator.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "circuit-breaker")
public class CircuitBreakerProperties {
    private int maxFailures = 3;
    private int timeout = 2000;
    private int resetTimeout = 10000;
}

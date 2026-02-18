package org.middleware;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableAsync;
import org.middleware.pnr.aggregator.config.MongoProperties;
import org.middleware.pnr.aggregator.config.CircuitBreakerProperties;
import org.middleware.pnr.aggregator.config.RateLimiterProperties;

@SpringBootApplication()
@EnableAsync
@Slf4j
@EnableConfigurationProperties({
        MongoProperties.class,
        CircuitBreakerProperties.class,
        RateLimiterProperties.class
})

public class PnrAggregatorApplication {
    public static void main(String[] args) {
        log.info("Starting Application: PnrAggregatorApplication");
        SpringApplication.run(PnrAggregatorApplication.class, args);
    }
}

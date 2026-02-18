package org.middleware.pnr.aggregator.config;

import io.vertx.circuitbreaker.CircuitBreaker;
import io.vertx.circuitbreaker.CircuitBreakerOptions;
import io.vertx.core.Vertx;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class CircuitBreakerConfig {

    private final CircuitBreakerProperties circuitBreakerProperties;

    @Bean
    public CircuitBreaker eTicketServiceCircuitBreaker(Vertx vertx) {
        return CircuitBreaker.create(
                "eticket-service-circuit-breaker",
                vertx,
                new CircuitBreakerOptions()
                        .setMaxFailures(circuitBreakerProperties.getMaxFailures())
                        .setTimeout(circuitBreakerProperties.getTimeout())
                        .setResetTimeout(circuitBreakerProperties.getResetTimeout())
        );
    }

    @Bean
    public CircuitBreaker bookingServiceCircuitBreaker(Vertx vertx) {
        return CircuitBreaker.create(
                "booking-service-circuit-breaker",
                vertx,
                new CircuitBreakerOptions()
                        .setMaxFailures(circuitBreakerProperties.getMaxFailures())
                        .setTimeout(circuitBreakerProperties.getTimeout())
                        .setResetTimeout(circuitBreakerProperties.getResetTimeout())
        );
    }

    @Bean
    public CircuitBreaker baggageServiceCircuitBreaker(Vertx vertx) {
        return CircuitBreaker.create(
                "baggage-service-circuit-breaker",
                vertx,
                new CircuitBreakerOptions()
                        .setMaxFailures(circuitBreakerProperties.getMaxFailures())
                        .setTimeout(circuitBreakerProperties.getTimeout())
                        .setResetTimeout(circuitBreakerProperties.getResetTimeout())
        );
    }
}

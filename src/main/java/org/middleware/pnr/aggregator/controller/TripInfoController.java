package org.middleware.pnr.aggregator.controller;


import io.vertx.circuitbreaker.OpenCircuitException;
import io.vertx.core.Future;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.middleware.pnr.aggregator.dto.TripResponse;
import org.middleware.pnr.aggregator.service.RateLimiterService;
import org.middleware.pnr.aggregator.service.TripInfoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CompletableFuture;

import static org.middleware.pnr.aggregator.util.ControllerUtil.getClientIp;
import static org.middleware.pnr.aggregator.util.ControllerUtil.toCompletableFuture;

@RestController
@RequestMapping("v1/booking")
@RequiredArgsConstructor
@Slf4j
public class TripInfoController {

    private final TripInfoService tripInfoService;
    private final RateLimiterService rateLimiterService;


    @GetMapping("/{pnr}")
    public CompletableFuture<ResponseEntity<TripResponse>> getTrip(
            @PathVariable String pnr, HttpServletRequest request) {

        pnr = pnr.trim();
        String clientIp = getClientIp(request);
        if (!rateLimiterService.isAllowed(clientIp)) {
            log.error("Get request limit exceeded for this client.");
            return CompletableFuture.completedFuture(
                    ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build()
            );
        }

        if (pnr.isBlank() || pnr.contains(" ")) {
            return CompletableFuture.completedFuture(ResponseEntity.badRequest().build());
        }

        return toCompletableFuture(
                tripInfoService.getTripInfo(pnr)
                        .map(ResponseEntity::ok)
                        .recover(err -> {
                            if (err instanceof OpenCircuitException) {
                                return Future.succeededFuture(
                                        ResponseEntity.status(503).build()
                                );
                            }
                            return Future.succeededFuture(
                                    ResponseEntity.internalServerError().build());
                        })
        );

    }


    @GetMapping("/customer/{customerId}")
    public CompletableFuture<ResponseEntity<TripResponse>> getTripByCustomerId(
            @PathVariable String customerId,
            HttpServletRequest request) {

        customerId = customerId.trim();
        if (customerId.isBlank()) {
            return CompletableFuture.completedFuture(
                    ResponseEntity.badRequest().build()
            );
        }

        String clientIp = getClientIp(request);
        if (!rateLimiterService.isAllowed(clientIp)) {
            return CompletableFuture.completedFuture(
                    ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build()
            );
        }

        return toCompletableFuture(
                tripInfoService.getTripInfoByCustomerId(customerId)
                        .map(ResponseEntity::ok)
                        .recover(err -> {
                            if (err instanceof OpenCircuitException) {
                                return Future.succeededFuture(
                                        ResponseEntity.status(503).build()
                                );
                            }
                            return Future.succeededFuture(
                                    ResponseEntity.internalServerError().build());
                        })
        );
    }


}

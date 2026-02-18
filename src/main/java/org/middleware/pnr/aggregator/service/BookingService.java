package org.middleware.pnr.aggregator.service;

import io.vertx.circuitbreaker.CircuitBreaker;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.middleware.pnr.aggregator.constants.AggregatorConstants;
import org.middleware.pnr.aggregator.model.Booking;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingService {
    private final MongoClient mongoClient;
    private final CircuitBreaker bookingServiceCircuitBreaker;
    private static final int MAX_RETRIES = AggregatorConstants.MAX_RETRIES;

    public Future<Booking> getBookingByPnr(String pnr) {
        JsonObject query = new JsonObject().put("pnr", pnr);
        log.info("BookingService: Starting request for getting booking by PNR for pnr: {}", pnr);
        return getBookingByPnrWithRetry(pnr, query, 0);
    }

    private Future<Booking> getBookingByPnrWithRetry(String pnr, JsonObject query, int attempt) {
        log.info("Fetching booking for PNR: {}", pnr);

        return bookingServiceCircuitBreaker.execute((Promise<Booking> bookingPromise) -> {
                    log.info("Executing MongoDB query fetching booking by PNR: {}", pnr);
                    mongoClient.findOne(AggregatorConstants.COLLECTION_BOOKINGS, query, null, result -> {
                        if (result.succeeded()) {
                            log.info("Mongo Query executed successfully for collection : {} for PNR: {}",
                                    AggregatorConstants.COLLECTION_BOOKINGS, pnr);

                            JsonObject resultJson = result.result();
                            if (resultJson == null || resultJson.isEmpty()) {
                                log.warn("No booking found for PNR: {}", pnr);
                                bookingPromise.complete(new Booking());

                            } else {
                                log.info("Booking found for PNR: {}", pnr);
                                Booking booking = resultJson.mapTo(Booking.class);
                                bookingPromise.complete(booking);
                            }
                        } else {
                            log.error("MongoDB query failed for PNR: {}, error: {}", pnr, result.cause().getMessage());
                            bookingPromise.fail(result.cause());
                        }
                    });
                })
                .recover(err -> {
                    if (attempt < MAX_RETRIES - 1) {
                        log.warn("Retry attempt {} for getBookingByPnr: {}", attempt + 1, pnr);
                        return getBookingByPnrWithRetry(pnr, query, attempt + 1);
                    } else {
                        log.error("Error: Error while fetching booking details for pnr: {} due to: {}",
                                pnr, err.getMessage());
                        return Future.failedFuture(err);
                    }
                });
    }


    public Future<Booking> getBookingByCustomerId(String customerId) {
        log.info("BookingService: Starting request for getting booking by customerId for customer: {}", customerId);
        JsonObject query = new JsonObject().put("passengers.customerId", customerId);
        return getBookingByCustomerIdWithRetry(customerId, query, 0);
    }

    private Future<Booking> getBookingByCustomerIdWithRetry(String customerId, JsonObject query, int attempt) {
        log.info("Fetching booking for customerId: {}", customerId);

        return bookingServiceCircuitBreaker.execute((Promise<Booking> bookingPromise) -> {
                    log.info("Executing MongoDB query for fetching booking by customerId: {}", customerId);
                    mongoClient.findOne(AggregatorConstants.COLLECTION_BOOKINGS, query, null, result -> {
                        if (result.succeeded()) {
                            log.info("Mongo Query executed successfully for collection : {} for customerId : {}",
                                    AggregatorConstants.COLLECTION_BOOKINGS, customerId);
                            JsonObject resultJson = result.result();
                            if (resultJson == null || resultJson.isEmpty()) {
                                log.warn("No booking found for customer ID: {}", customerId);
                                bookingPromise.complete(new Booking());
                            } else {
                                log.info("Booking found for customer ID: {}", customerId);
                                Booking booking = resultJson.mapTo(Booking.class);
                                bookingPromise.complete(booking);
                            }
                        } else {
                            bookingPromise.fail(result.cause());
                        }
                    });
                })
                .recover(err -> {
                    if (attempt < MAX_RETRIES - 1) {
                        log.warn("Retry attempt {} for getBookingByCustomerId: {}", attempt + 1, customerId);
                        return getBookingByCustomerIdWithRetry(customerId, query, attempt + 1);
                    } else {
                        log.error("Error: Error while fetching booking details for customerId: {} due to: {}",
                                customerId, err.getMessage());
                        return Future.failedFuture(err);
                    }
                });
    }


}

package org.middleware.pnr.aggregator.service;


import io.vertx.circuitbreaker.CircuitBreaker;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.middleware.pnr.aggregator.constants.AggregatorConstants;
import org.middleware.pnr.aggregator.model.ETicket;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ETicketService {

    private final MongoClient mongoClient;
    private final CircuitBreaker eTicketServiceCircuitBreaker;
    private static final int MAX_RETRIES = AggregatorConstants.MAX_RETRIES;

    public Future<Optional<ETicket>> getETicket(String pnr, int passengerNumber) {
        JsonObject query = new JsonObject().put("pnr", pnr).put("passengerNumber", passengerNumber);
        return getETicketWithRetry(pnr, passengerNumber, query, 0);
    }

    private Future<Optional<ETicket>> getETicketWithRetry(String pnr, int passengerNumber, JsonObject query, int attempt) {

        log.info("ETicketService: Fetching eTicket for the prn: {} for passengerNumber {}", pnr, passengerNumber);
        return eTicketServiceCircuitBreaker.execute((Promise<Optional<ETicket>> eTicketPromise) ->
                        mongoClient.findOne(AggregatorConstants.COLLECTION_ETICKETS, query,
                                null, optionalResult -> {
                                    if (optionalResult.succeeded()) {
                                        log.info("Executing MongoDB query for fetching Eticket by passengerNumer: {} for PNR: {}",
                                                passengerNumber, pnr);
                                        JsonObject result = optionalResult.result();
                                        if (result == null || result.isEmpty()) {
                                            log.warn("No Eticket found for passengerID: {} for pnr: {}", passengerNumber, pnr);
                                            eTicketPromise.complete(Optional.empty());
                                        } else {
                                            log.info("Eticket found for passengerID: {} for pnr: {}", passengerNumber, pnr);
                                            ETicket eticketResult = result.mapTo(ETicket.class);
                                            eTicketPromise.complete(Optional.of(eticketResult));
                                        }
                                    } else
                                        eTicketPromise.fail(optionalResult.cause());
                                }))
                .recover(err -> {
                    if (attempt < MAX_RETRIES - 1) {
                        log.info("Fetch eticket DB Retry {} ", attempt);
                        return getETicketWithRetry(pnr, passengerNumber, query, attempt + 1);
                    } else {
                        return Future.succeededFuture(Optional.empty());
                    }
                });
    }


}

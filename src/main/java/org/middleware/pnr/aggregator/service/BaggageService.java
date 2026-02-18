package org.middleware.pnr.aggregator.service;

import io.vertx.circuitbreaker.CircuitBreaker;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.middleware.pnr.aggregator.constants.AggregatorConstants;
import org.middleware.pnr.aggregator.model.Baggage;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class BaggageService {

    private final MongoClient mongoClient;
    private final CircuitBreaker baggageServiceCircuitBreaker;
    private static final int MAX_RETRIES = AggregatorConstants.MAX_RETRIES;

    public Future<List<Baggage>> getBaggageByPNR(String pnr) {
        JsonObject query = new JsonObject().put("pnr", pnr);
        return getBaggageByPNRWithRetry(pnr, query, 0);
    }

    private Future<List<Baggage>> getBaggageByPNRWithRetry(String pnr, JsonObject query, int attempt) {
        log.info("BaggageService: Fetching the baggage info for pnr: {}", pnr);
        return baggageServiceCircuitBreaker.execute((Promise<List<Baggage>> baggagesPromise) ->
                        mongoClient.find(AggregatorConstants.COLLECTION_BAGGAGES, query, listResults -> {
                            if (listResults.succeeded()) {
                                log.info("BaggageService: DB call completed for baggage.");
                                List<JsonObject> results = listResults.result();
                                if (results.isEmpty()) {
                                    log.warn("BaggageService: No baggage found against: {}", pnr);
                                    baggagesPromise.fail("No baggage against this PNR: " + pnr);
                                } else {
                                    log.info("Baggage against this PNR: " + pnr);
                                    List<Baggage> baggages = results.stream()
                                            .map(json -> json.mapTo(Baggage.class)).toList();
                                    baggagesPromise.complete(baggages);
                                }
                            } else baggagesPromise.fail(listResults.cause());
                        }))
                .recover(err -> {
                    if (attempt < MAX_RETRIES - 1) {
                        log.warn("Retry attempt {} for getBaggageByPNR: {}", attempt + 1, pnr);
                        return getBaggageByPNRWithRetry(pnr, query, attempt + 1);
                    } else {
                        log.error("Error while fetching baggage for this pnr: {}  due to : {}",
                                pnr, err.getMessage());
                        return Future.failedFuture(err);
                    }
                });
    }

}

package org.middleware.pnr.aggregator.consumer;


import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.json.JsonObject;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.middleware.pnr.aggregator.constants.AggregatorConstants;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class PnrFetchConsumer {
    private final EventBus eventBus;
    private MessageConsumer<JsonObject> consumer;

    @PostConstruct
    public void register() {
        consumer = eventBus.consumer(AggregatorConstants.EVENT_BUS_EVENT_KEY, event -> {
            JsonObject eventJson = JsonObject.mapFrom(event.body());
            log.info("[EVENT BUS] -> [CONSUMED] -> [EVENT] event received for PNR {}: at time: {} ",
                    eventJson.getString(AggregatorConstants.PNR),
                    eventJson.getString(AggregatorConstants.TIMESTAMP));
        });
    }

    @PreDestroy
    public void shutdown() {
        if (consumer != null) {
            consumer.unregister();
        }
    }

}

package org.middleware.pnr.aggregator.websockets;

import io.vertx.core.eventbus.EventBus;
import io.vertx.core.http.ServerWebSocket;
import io.vertx.core.impl.ConcurrentHashSet;
import io.vertx.core.json.JsonObject;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.middleware.pnr.aggregator.constants.AggregatorConstants;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class TripWebSocketHandler {
    private final EventBus eventBus;


    private final Set<ServerWebSocket> clients = new ConcurrentHashSet<>();


    @PostConstruct
    public void init() {
        log.info("WS: Initiating web socket for key: {}", AggregatorConstants.EVENT_BUS_EVENT_KEY);
        eventBus.consumer(AggregatorConstants.EVENT_BUS_EVENT_KEY, message -> {
            JsonObject msg = JsonObject.mapFrom(message.body());
            broadCastMessage(msg);
        });
    }


    public void handle(ServerWebSocket webSocket) {
        log.info("WS: adding websocket.");
        clients.add(webSocket);
        webSocket.closeHandler(v -> clients.remove(webSocket));
        webSocket.exceptionHandler(err -> clients.remove(webSocket));
    }


    private void broadCastMessage(JsonObject message) {
        log.info("WS: broadcasting message: {} for clients", message);
        for (ServerWebSocket wsClient : clients) {
            log.info("WS:  broadcasting message: {} for client : {}", message, wsClient);
            if (!wsClient.isClosed()) {
                wsClient.writeTextMessage(message.encode());
            }
        }
    }

    @PreDestroy
    public void shutdown() {
        log.warn("WS: Shutting down all clients.");
        for (ServerWebSocket client : clients) {
            if (!client.isClosed()) {
                client.close();
            }
        }
        clients.clear();
    }

}
